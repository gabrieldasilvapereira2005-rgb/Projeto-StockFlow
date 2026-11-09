package stockflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração do serviço de estoque usando um SQLite temporário.
 * Nenhum teste utiliza o banco de dados real do usuário.
 */
class StockFlowServiceTest {
    @TempDir
    Path temporaryDirectory;

    private StockFlowApp.StockService service;
    private int productId;

    @BeforeEach
    void setUp() {
        service = new StockFlowApp.StockService(temporaryDirectory.resolve("stockflow-test.db"));
        service.addProduct("SKU-TESTE", "Produto de teste", "MEAT", "UN",
                bd("100"), bd("10"));
        productId = ((Number) service.products(false).get(0).get("id")).intValue();
    }

    @Test
    void deveCadastrarProdutoComSaldoInicialZerado() {
        Map<String, Object> row = inventory();

        assertEquals("SKU-TESTE", row.get("sku"));
        assertDecimal("0", row.get("Quantidade em estoque"));
        assertDecimal("0", row.get("Disponivel para saida"));
    }

    @Test
    void deveRegistrarEntradaEAtualizarEstoqueELote() {
        receive("40", 0, null);

        assertDecimal("40", inventory().get("Quantidade em estoque"));
        assertEquals(1, service.lots().size());
        assertEquals("INBOUND", service.movements().get(0).get("movement_type"));
    }

    @Test
    void naoDevePermitirEntradaAcimaDaCapacidade() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> receive("101", 0, null));

        assertTrue(error.getMessage().contains("capacidade"));
        assertDecimal("0", inventory().get("Quantidade em estoque"));
        assertTrue(service.movements().isEmpty());
    }

    @Test
    void deveRealizarSaidaQuandoExisteQuantidadeDisponivel() {
        receive("80", 0, null);

        service.issue(productId, bd("20"), "Venda de teste");

        assertDecimal("60", inventory().get("Quantidade em estoque"));
        assertEquals("OUTBOUND", service.movements().get(0).get("movement_type"));
        assertDecimal("20", service.movements().get(0).get("quantity"));
    }

    @Test
    void saidaAcimaDoDisponivelDeveFalharSemAlterarOEstoque() {
        receive("80", 0, null);
        int movementsBefore = service.movements().size();

        assertThrows(IllegalArgumentException.class,
                () -> service.issue(productId, bd("81"), "Tentativa acima do saldo"));

        assertDecimal("80", inventory().get("Quantidade em estoque"));
        assertEquals(movementsBefore, service.movements().size());
    }

    @Test
    void devePermitirMultiplasReservasEImpedirCompromissoAcimaDoDisponivel() {
        receive("80", 0, null);

        service.createReservation(productId, "CUSTOMER", bd("20"), "PEDIDO-001", "");
        service.createReservation(productId, "CUSTOMER", bd("30"), "PEDIDO-002", "");

        assertEquals(2, service.reservations().size());
        assertDecimal("50", inventory().get("Reservado cliente"));
        assertDecimal("30", inventory().get("Disponivel para saida"));

        assertThrows(IllegalArgumentException.class,
                () -> service.createReservation(productId, "CUSTOMER", bd("31"), "PEDIDO-003", ""));

        assertDecimal("50", inventory().get("Reservado cliente"));
        assertDecimal("30", inventory().get("Disponivel para saida"));
    }

    @Test
    void cancelarReservaDeveDevolverQuantidadeADisponibilidade() {
        receive("80", 0, null);
        service.createReservation(productId, "CUSTOMER", bd("25"), "PEDIDO-CANCELAR", "");
        int reservationId = reservationId();

        service.cancelReservation(reservationId);

        assertDecimal("0", inventory().get("Reservado cliente"));
        assertDecimal("80", inventory().get("Disponivel para saida"));
        assertEquals("CANCELLED", service.reservations().get(0).get("status"));
    }

    @Test
    void confirmarReservaDeClienteDeveEfetivarSaidaUmaUnicaVez() {
        receive("80", 0, null);
        service.createReservation(productId, "CUSTOMER", bd("20"), "PEDIDO-CONFIRMAR", "");
        int reservationId = reservationId();

        service.confirmReservation(reservationId);

        assertDecimal("60", inventory().get("Quantidade em estoque"));
        assertDecimal("0", inventory().get("Reservado cliente"));
        assertEquals("CONFIRMED", service.reservations().get(0).get("status"));
        assertEquals(2, service.movements().size());
        assertThrows(IllegalArgumentException.class, () -> service.confirmReservation(reservationId));
        assertDecimal("60", inventory().get("Quantidade em estoque"));
    }

    @Test
    void criarEBaixarBloqueioFisicoDeveAtualizarDisponibilidade() {
        receive("80", 0, null);
        service.addHold(productId, bd("25"), "Separacao para conferencia");

        assertDecimal("25", inventory().get("Bloqueado fisico"));
        assertDecimal("55", inventory().get("Disponivel para saida"));

        int holdId = ((Number) service.holds().get(0).get("id")).intValue();
        service.releaseHold(holdId, "Operador de teste");

        assertDecimal("0", inventory().get("Bloqueado fisico"));
        assertDecimal("80", inventory().get("Disponivel para saida"));
        assertEquals("RELEASED", service.holds().get(0).get("status"));
    }

    @Test
    void naoDevePermitirBloquearQuantidadeMaiorQueADisponivel() {
        receive("80", 0, null);
        service.createReservation(productId, "CUSTOMER", bd("50"), "PEDIDO-RESERVA", "");

        assertThrows(IllegalArgumentException.class,
                () -> service.addHold(productId, bd("31"), "Bloqueio acima do saldo livre"));

        assertDecimal("50", inventory().get("Reservado cliente"));
        assertDecimal("0", inventory().get("Bloqueado fisico"));
        assertDecimal("30", inventory().get("Disponivel para saida"));
    }

    @Test
    void bloqueioOperacionalDeSaidaDeveImpedirSaidaAteSerLiberado() {
        receive("80", 0, null);
        service.addBlock(productId, "OUTBOUND", "Auditoria de estoque");

        assertThrows(IllegalArgumentException.class,
                () -> service.issue(productId, bd("1"), "Tentativa bloqueada"));
        assertDecimal("80", inventory().get("Quantidade em estoque"));

        int blockId = ((Number) service.blocks().get(0).get("id")).intValue();
        service.releaseBlock(blockId, "Responsavel de teste");
        service.issue(productId, bd("1"), "Saida apos liberacao");

        assertDecimal("79", inventory().get("Quantidade em estoque"));
    }

    @Test
    void bloqueioOperacionalDeEntradaDeveImpedirRecebimentoAteSerLiberado() {
        service.addBlock(productId, "INBOUND", "Conferencia pendente");

        assertThrows(IllegalArgumentException.class, () -> receive("10", 0, null));
        assertDecimal("0", inventory().get("Quantidade em estoque"));

        int blockId = ((Number) service.blocks().get(0).get("id")).intValue();
        service.releaseBlock(blockId, "Responsavel de teste");
        receive("10", 0, null);

        assertDecimal("10", inventory().get("Quantidade em estoque"));
    }

    @Test
    void reservaDeReposicaoDeveSerBaixadaQuandoEntradaForRecebida() {
        service.createReservation(productId, "REPLENISHMENT", bd("30"), "Fornecedor de teste", "");
        int reservationId = reservationId();

        assertDecimal("30", inventory().get("Capacidade reservada"));
        receive("30", 0, reservationId);

        assertDecimal("30", inventory().get("Quantidade em estoque"));
        assertDecimal("0", inventory().get("Capacidade reservada"));
        assertEquals("CONFIRMED", service.reservations().get(0).get("status"));
    }

    @Test
    void estoqueCriticoAbaixoDeVinteECincoPorCentoPermiteNovaEntradaMesmoComIntervaloCurto() {
        receive("9", 1, null);

        receive("1", 1, null);

        assertDecimal("10", inventory().get("Quantidade em estoque"));
    }

    @Test
    void estoqueExatamenteVinteECincoPorCentoAindaRespeitaIntervaloMinimoDeEntrada() {
        receive("25", 1, null);

        assertThrows(IllegalArgumentException.class, () -> receive("1", 1, null));

        assertDecimal("25", inventory().get("Quantidade em estoque"));
    }

    @Test
    void estoqueAbaixoDeVinteECincoPorCentoBloqueiaSaidaAntesDeTresDias() {
        receive("20", 1, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.issue(productId, bd("1"), "Saida antes de tres dias"));

        assertTrue(error.getMessage().contains("menos de 3 dias"));
        assertDecimal("20", inventory().get("Quantidade em estoque"));
    }

    @Test
    void estoqueAbaixoDeVinteECincoPorCentoPermiteSaidaQuandoUltimaRemessaTemTresDias() {
        receive("20", 3, null);

        service.issue(productId, bd("1"), "Saida no limite de tres dias");

        assertDecimal("19", inventory().get("Quantidade em estoque"));
    }

    @Test
    void estoqueDeQuinzePorCentoNaoPermiteSaida() {
        receive("15", 3, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.issue(productId, bd("1"), "Saida abaixo do limite"));

        assertDecimal("15", inventory().get("Quantidade em estoque"));
    }

    @Test
    void loteComMaisDeTrintaDiasNaoPodeSerUtilizadoNaSaida() {
        receive("80", 31, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.issue(productId, bd("1"), "Tentativa com lote antigo"));

        assertDecimal("80", inventory().get("Quantidade em estoque"));
    }

    private void receive(String quantity, int daysAgo, Integer replenishmentReservationId) {
        LocalDate receivedAt = LocalDate.now().minusDays(daysAgo);
        LocalDate expiration = LocalDate.now().plusDays(30);
        String batch = "LOTE-" + System.nanoTime();
        service.receipt(productId, bd(quantity), batch, receivedAt.toString(),
                expiration.toString(), null, bd("0"), replenishmentReservationId);
    }

    private Map<String, Object> inventory() {
        return service.inventory(true).stream()
                .filter(row -> "SKU-TESTE".equals(row.get("sku")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Produto de teste nao encontrado no estoque."));
    }

    private int reservationId() {
        List<Map<String, Object>> reservations = service.reservations();
        return ((Number) reservations.get(0).get("id")).intValue();
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void assertDecimal(String expected, Object actual) {
        assertNotNull(actual, "O valor consultado nao deve ser nulo.");
        assertEquals(0, bd(expected).compareTo(new BigDecimal(actual.toString())),
                "Valor numerico diferente do esperado.");
    }
}
