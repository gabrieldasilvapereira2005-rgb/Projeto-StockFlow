# Mapa de funcionalidades - StockFlow_Java

A edição Java utiliza Swing. A janela, os formularios, as tabelas e a classe interna `StockService` estao reunidos em `StockFlowApp.java`.

| Aba | Acoes disponíveis | Informacoes principais |
|---|---|---|
| Visão geral | Atualizar e exportar CSV | Resumo dos produtos e indicadores |
| Produtos | Cadastrar, editar, reativar (5 segundos), desativar e atualizar | SKU, nome, categoria, unidade, capacidade, preço, estoque e status |
| Fornecedores | Cadastrar, editar, solicitar reposição e atualizar | Nome, documento, e-mail, telefone e status |
| Entradas e saídas | Registrar entrada, registrar saída e atualizar | Produto, quantidade, lote, datas, fornecedor, custo, reserva e observações |
| Reservas | Criar, confirmar saída de cliente, cancelar e atualizar | Tipo, quantidade, referência, expiração e status |
| Bloqueios operacionais | Criar, liberar e atualizar | INBOUND/OUTBOUND/BOTH, motivo e responsavel pela liberação |
| Bloqueios fisicos | Criar, liberar e atualizar | Quantidade, motivo e responsavel pela liberação |
| Estoque | Atualizar e exportar CSV | Quantidade em estoque, capacidade, percentual, classificação e disponibilidade |
| Movimentacoes | Atualizar e exportar CSV | Historico de entradas e saídas por lote |
| Lotes | Atualizar | Codigo, fornecedor, recebimento, validade, quantidade restante e custo |

## Arquivos e responsabilidades

- `src/main/java/stockflow/StockFlowApp.java`: interface Swing, servico de aplicação, regras e persistência JDBC.
- `pom.xml`: define Java 17, driver SQLite JDBC e plugin Maven de execução.
- `stockflow.db`: banco local aberto a partir do diretório de trabalho.
- `INICIAR_STOCKFLOW.bat`: inicializador Windows; muda para a pasta do arquivo antes de executar Maven.
