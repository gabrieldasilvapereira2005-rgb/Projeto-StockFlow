# Projeto-StockFlow
Projeto de estocagem de produtos

# StockFlow - Edição Java

## 1. Visão geral da solução

O StockFlow_Java é uma aplicação desktop para controle de produtos, fornecedores, lotes, entradas, saídas, reservas, bloqueios, estoque, movimentações e exportação CSV. A aplicação é desenvolvida em Java, utiliza Swing para a interface e JDBC para armazenar os dados em um banco SQLite local.

## 2. Arquitetura e estrutura adotada

```text
Usuario
  |
  v
Swing - StockFlowApp (JFrame)
  |  abas, JTable, formularios e eventos
  v
StockService (classe interna)
  |  validacoes, politicas, consultas e transacoes
  v
JDBC - SQLite JDBC
  |
  v
SQLite - stockflow.db
  |
  v
CSV - exportacao de estoque e movimentacoes
```

### Componentes

- **Apresentacao:** `StockFlowApp`, janela principal Swing que cria abas, tabelas, formularios e dialogos.
- **Aplicacao e regras:** classe interna `StockService`, que calcula disponibilidade, válida operações, aloca lotes e coordena transações.
- **Persistencia:** JDBC, API padrao Java para conexões e comandos SQL; o driver concreto e `org.xerial:sqlite-jdbc`.
- **Build e dependência:** Maven, configurado por `pom.xml`, compila a aplicação e resolve o driver SQLite JDBC.
- **Exportacao:** geracao local dos arquivos CSV, com delimitador ponto e virgula e BOM UTF-8.

## 3. Principais decisões técnicas

- **Java 17 ou superior:** linguagem e ambiente de execução da aplicação.
- **Swing:** biblioteca grafica incluida no JDK para criar janelas, abas, botoes, tabelas e formularios.
- **JDBC:** camada padrao de acesso ao banco relacional.
- **Xerial SQLite JDBC 3.46.1.3:** driver que conecta Java ao banco SQLite.
- **Maven:** compila, gerencia dependências e inicia a classe principal.
- **BigDecimal:** utilizado para quantidades, valores e percentuais, evitando depender de ponto flutuante binario nos calculos centrais.
- **PreparedStatement:** comandos SQL parametrizados.
- **Transacoes JDBC:** agrupam atualizações de saldo, lote, reserva e movimentação.
- **FEFO:** prioriza lote com vencimento mais próximo.
- **SQLite local:** os dados são armazenados no arquivo `stockflow.db` utilizado pela aplicação.

## 4. Premissas consideradas

- O usuário possui JDK 17 ou superior.
- O usuário instala Apache Maven e o comando `mvn` deve estar no PATH.
- A primeira execução exige internet para Maven baixar o driver SQLite JDBC, se ele ainda não estiver em cache.
- O programa deve ser executado a partir da pasta `StockFlow_Java`, pois o caminho de `stockflow.db` e relativo ao diretório de trabalho.
- As categorias aceitas sao `MEAT`, `FRUIT`, `LEGUME`, `GREENS`, `DAIRY` e `CEREAL`.
- Datas de recebimento e validade usam `AAAA-MM-DD`.
- Solicitar reposição na aba Fornecedores cria reserva REPLENISHMENT e grava o nome do fornecedor na referência; não cria ordem de compra formal.
- Os limites de idade e percentual sao parâmetros do projeto e precisam de aprovacao especializada antes de uso real.

## 5. Regras de negócio adotadas

- Classificacao: ALTO a partir de 80%; ACEITAVEL a partir de 50%; BAIXO a partir de 25%; CRITICO abaixo de 25%.
- Qualidade por idade: OTIMA 0-10 dias; BOA 11-19; BAIXA 20-30; PROIBIDO_CONSUMO a partir de 31 dias.
- Entrada verifica bloqueio INBOUND/BOTH, limite de 85%, intervalo mínimo por categoria, regra-base de estoque abaixo de 60% ou idade da última remessa acima do limite e capacidade máxima.
- Abaixo de 25%, o intervalo mínimo de categoria não impede reposição; os outros bloqueios permanecem.
- Saida exige estoque acima de 15%; abaixo de 25%, última remessa com idade menor que 3 ou maior que 31 dias bloqueia; sao verificados bloqueio, disponibilidade, validade, idade/qualidade e categoria.
- A seleção e por FEFO.
- CUSTOMER reserva quantidade de lotes existentes e so efetiva a saída na confirmação. REPLENISHMENT reserva capacidade futura e exige recebimento real.
- Liberacao de bloqueio exige nome do responsavel.

Detalhes completos estao em `docs/REGRAS_DE_NEGOCIO.md`.

## 6. Limitações conhecidas

- Nao ha autenticacao, perfis de usuário nem auditoria completa.
- SQLite e destinado ao uso local desta demonstracao; não foi feita homologacao para varios operadores simultaneos.
- Nao ha tarefa automática para expirar reservas vencidas.
- Solicitacao de reposição não cria ordem de compra formal.
- Nao existe suite automatizada de testes Java nesta distribuição.
- O driver SQLite e baixado via Maven na primeira execução e depende de acesso ao repositorio de dependências.
- O pacote não inclui Maven Wrapper; Apache Maven precisa ser instalado separadamente e adicionado ao PATH.
- Nao ha benchmark de carga nem teste formal de concorrencia multiusuario.

## 7. Instruções para execução

### Requisitos

- JDK 17 ou superior: https://adoptium.net/temurin/releases/?version=17 .
- Apache Maven: https://maven.apache.org/download.cgi .
- Windows 10/11 para utilizar o arquivo `.bat`; Java e Maven também permitem execução manual em outros sistemas com ambiente grafico disponível.

### Primeira execução no Windows

1. Instale o JDK 17 ou superior.
2. Instale Apache Maven e adicione a pasta `bin` do Maven ao PATH do Windows. A documentação oficial de instalação explica a configuração: https://maven.apache.org/install.html .
3. Abra um novo PowerShell e confirme:

```powershell
java -version
javac -version
mvn -version
```

4. Extraia a pasta `StockFlow_Java`.
5. Abra PowerShell na pasta `StockFlow_Java` e execute:

```powershell
mvn -q compile exec:java
```

Ou execute `INICIAR_STOCKFLOW.bat` com dois cliques. O inicializador muda o diretório de trabalho para a própria pasta antes de chamar Maven.

### Compilar sem iniciar

```powershell
mvn -q compile
```

### Dados e backup

O programa abre `stockflow.db` no diretório de trabalho atual. Para utilizar sempre o banco distribuido, execute o programa a partir da pasta `StockFlow_Java` ou use o BAT. Feche o programa antes de copiar o arquivo para backup.

## 8. O que eu faria diferente em um ambiente real de produção

- Separaria a classe atual em pacotes de interface Swing, serviços de aplicação, domínio e repositórios JDBC, facilitando manutenção e testes.
- Criaria testes unitários com JUnit e testes de integração para persistência SQLite, regras de reserva, bloqueio e recebimento.
- Executaria consultas demoradas fora da Event Dispatch Thread com `SwingWorker`, mantendo atualizações de componentes na EDT.
- Adicionaria autenticação, perfis, trilha de auditoria, logs estruturados, backup automático e testes de restauração.
- Para operação simultânea em várias estações, avaliaria banco servidor e controle transacional adequado, em vez de compartilhar um arquivo SQLite.
- Incluiria Maven Wrapper para padronizar a compilação e prepararia um pacote de instalação/execução para o Windows.

## 9. Arquivos e documentação desta edição

| Caminho relativo | Conteudo |
|---|---|
| `src/main/java/stockflow/StockFlowApp.java` | Interface Swing, servico interno, regras, JDBC e exportação CSV. |
| `pom.xml` | Versao Java, driver SQLite JDBC, plugin de compilação e plugin de execução Maven. |
| `stockflow.db` | Banco SQLite independente desta edição. |
| `INICIAR_STOCKFLOW.bat` | Inicializador Windows que executa Maven a partir da pasta do projeto. |
| `docs/REGRAS_DE_NEGOCIO.md` | Politicas percentuais, de categoria, entrada, saída, reservas e bloqueios. |
| `docs/MAPA_DE_FUNCIONALIDADES.md` | Abas, botoes, campos e arquivos Java. |
| `docs/MANUAL_PARA_UTILIZACAO_DA_FERRAMENTA.md` | Tutorial para instalar e utilizar a edição Java. |
| `docs/MANUAL_PARA_UTILIZACAO_DA_FERRAMENTA.docx` | Versao Word do tutorial Java. |
| `docs/CONVERSACAO_COM_A_IA.md` | Prompts, devolutivas e problemas historicos da etapa Java. |
| `docs/CONVERSACAO_COM_A_IA.docx` | Versao Word do histórico Java. |
| `docs/LOGICA_POR_TRAS_DOS_CODIGOS.md` | Explicacao executiva e técnica dos códigos Java. |
| `docs/LOGICA_POR_TRAS_DOS_CODIGOS.docx` | Versao Word da explicacao Java. |
| `docs/REVISAO_E_MELHORIA_DE_DESEMPENHO.md` | Revisao do código, validações e oportunidades de desempenho Java. |
| `docs/REVISAO_E_MELHORIA_DE_DESEMPENHO.docx` | Versao Word do relatório Java. |

