# Lógica por trás dos códigos - StockFlow_Java

## Resumo executivo

O StockFlow_Java e uma aplicação desktop escrita integralmente em Java. Swing cria as janelas, formularios, abas e tabelas; `StockService`, classe interna de `StockFlowApp`, executa as regras e transações; JDBC conversa com SQLite pelo driver Xerial. Maven compila a aplicação e baixa a dependência JDBC.

## 1. Linguagem e tecnologias

| Tecnologia | Funcao |
|---|---|
| Java 17+ | Linguagem e ambiente de execução da aplicação. |
| Swing | Componentes graficos de desktop, como JFrame, JTabbedPane, JTable, JButton e JDialog. |
| JDBC | API padrao Java para conexão e execução de SQL. |
| Xerial SQLite JDBC 3.46.1.3 | Driver JDBC utilizado para abrir `stockflow.db`. |
| Maven | Build, resolução de dependências e execução da classe principal. |
| BigDecimal | Calculos de quantidade, percentuais e valores. |
| java.time | Datas, validade e idade dos lotes. |
| PreparedStatement | Comandos SQL parametrizados. |

Swing e JDBC fazem parte do ecossistema Java; o driver SQLite JDBC e uma biblioteca externa declarada no Maven.

## 2. Arquivos e responsabilidades

- **`src/main/java/stockflow/StockFlowApp.java`:** classe principal, cria a janela Swing, monta as abas, formulários, tabelas, eventos, classe interna `StockService`, regras, esquema e consultas.
- **`pom.xml`:** define `maven.compiler.release=17`, dependência SQLite JDBC 3.46.1.3, plugin de compilação e plugin `exec-maven-plugin` para executar `stockflow.StockFlowApp`.
- **`stockflow.db`:** base SQLite. O código resolve esse arquivo pelo diretório de trabalho atual.
- **`INICIAR_STOCKFLOW.bat`:** muda para a pasta do arquivo e executa Maven.

## 3. Como o código funciona

1. O metodo `main()` agenda a construção de `StockFlowApp` na thread de eventos Swing.
2. O construtor cria as abas e registra os botoes com metodos de acao.
3. Formularios Swing recolhem os campos e convertem valores para `BigDecimal` ou tipos de data.
4. A classe interna `StockService` abre uma conexão JDBC e aplica as validações.
5. `PreparedStatement` envia SQL com parâmetros; as operações com varias atualizações desligam o auto-commit e confirmam com `commit()`.
6. `refreshAll()` recarrega tabelas e indicadores. O resultado de inventario e reutilizado para as visoes que mostram os mesmos dados.
7. A exportação usa um seletor de arquivo e grava CSV em UTF-8, com delimitador ponto e virgula.

## 4. Regras e metodos centrais

- `pct()` calcula percentual e `status()` classifica o estoque.
- `age()` e `quality()` calculam idade e qualidade.
- `lowStockRecencyBlock()` aplica a restricao de saída com estoque abaixo de 25% e última remessa com menos de 3 ou mais de 31 dias.
- `inboundCheck()` válida as condicoes de entrada, a exceção abaixo de 25%, a capacidade e a reserva de reposição quando informada.
- `eligible()` filtra lotes não vencidos e dentro dos limites por categoria.
- `free()` desconta reservas CUSTOMER e bloqueios fisicos ativos das quantidades dos lotes.
- `allocate()` distribui uma quantidade entre lotes livres na ordem FEFO.
- `receipt()`, `issue()`, `createReservation()`, `confirmReservation()`, `cancelReservation()`, `addBlock()`, `releaseBlock()`, `addHold()` e `releaseHold()` implementam as operações de negócio.

## 5. Modelo de dados

O esquema cria `products`, `suppliers`, `inventory_balances`, `inventory_lots`, `stock_operations`, `stock_movements`, `reservations`, `reservation_allocations`, `operation_blocks`, `stock_holds` e `stock_hold_allocations`.

As tabelas registram produto, fornecedor, saldo físico, lotes, cabecalhos de operação, movimentações, reservas, alocacoes por lote, bloqueios operacionais e bloqueios fisicos.

## 6. Tratamento de dados e transações

`BigDecimal` e utilizado para calculos centrais. `PreparedStatement` evita montar comandos de escrita concatenando campos do formulario. O uso de try-with-resources fecha conexões, statements e result sets ao final de cada operação. As transações agrupam a atualização de saldo, lote, reserva e histórico.

O metodo `free()` executa consultas agregadas sobre lotes, reservas e bloqueios fisicos de um produto. A rotina `inventory()` percorre os produtos e calcula disponibilidade por produto; para bases grandes, pode ser otimizada com consultas agregadas que processem varios produtos em conjunto.

## 7. Compilacao e validação

O código-fonte foi compilado com compatibilidade Java 17. A distribuição atual inclui uma suíte JUnit 5 com 19 testes de integração do serviço e da persistência SQLite. Em 9 de novembro de 2026, o usuário informou a execução de `mvn test` com 19 testes, zero falhas, zero erros, zero ignorados e `BUILD SUCCESS`. Esse resultado cobre os cenários implementados na suíte, mas não comprova teste ponta a ponta da interface Swing, concorrência multiusuário ou adequação para produção.

## 8. Limitações

O código de interface e servico esta concentrado em um arquivo Java principal, o que simplifica a distribuição, mas aumenta o acoplamento. Nao ha autenticacao, perfis, auditoria completa, worker de expiração de reservas, benchmark de carga ou homologacao multiusuario. A aplicação depende de JDK, Maven e do driver SQLite JDBC.
