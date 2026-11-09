# Manual para utilização da ferramenta - StockFlow_Java

## 1. Objetivo do manual

Este manual explica como instalar, abrir e utilizar a edição Java do StockFlow. A interface e uma aplicação desktop Swing, e os dados sao guardados localmente em SQLite.

## 2. O que precisa ser baixado

**Obrigatorios:**

- **JDK 17 ou superior:** https://adoptium.net/temurin/releases/?version=17 . Baixe a distribuição JDK (não apenas JRE), pois o projeto precisa compilar.
- **Apache Maven:** https://maven.apache.org/download.cgi . Baixe a distribuição binaria, extraia e adicione a pasta `bin` do Maven ao PATH. Guia oficial: https://maven.apache.org/install.html .

**Dependencia instalada pelo Maven:**

- SQLite JDBC `org.xerial:sqlite-jdbc:3.46.1.3`, declarado no `pom.xml`. O Maven baixa essa biblioteca na primeira execução.

**Banco de dados:** não é necessário instalar um servidor SQLite. A aplicação utiliza um arquivo local para armazenar os dados.

## 3. Como abrir o StockFlow

1. Instale o JDK 17 ou superior.
2. Instale o Apache Maven e configure o PATH.
3. Abra um novo PowerShell e confirme:

```powershell
java -version
javac -version
mvn -version
```

4. Extraia `StockFlow_Java.zip`.
5. Abra PowerShell na pasta `StockFlow_Java`.
6. Execute:

```powershell
mvn -q compile exec:java
```

Ou de dois cliques em `INICIAR_STOCKFLOW.bat`. Na primeira execução, Maven pode precisar de internet para baixar o driver SQLite JDBC.

## 4. Conhecendo as abas e campos

### Visão geral

Mostra os produtos e indicadores gerais. O botao **Atualizar** consulta novamente os dados; **Exportar CSV** salva a posicao de estoque.

### Produtos

Clique **Cadastrar** e informe SKU (único), nome, categoria, unidade, capacidade máxima positiva e preço unitario não negativo. As categorias sao MEAT, FRUIT, LEGUME, GREENS, DAIRY e CEREAL.

Para editar, selecione uma linha e clique **Editar**. Para ativar produto inativo, use **Reativar (5s)**. A confirmação fica habilitada após cinco segundos. Para desativar, use **Desativar**.

### Fornecedores

Informe nome, documento, e-mail e telefone para cadastrar. Em **Editar**, o campo Ativo utiliza `1` para ativo e `0` para inativo. Para solicitar reposição, selecione um fornecedor, clique **Solicitar reposição**, escolha produto e quantidade. A operação cria uma reserva REPLENISHMENT e grava o fornecedor na referência; não adiciona estoque automaticamente.

### Entradas e saídas

**Registrar entrada:** informe produto, quantidade, código do lote, data de recebimento, validade opcional, ID de fornecedor opcional, custo unitario e ID de reserva de reposição opcional. Datas usam `AAAA-MM-DD`.

**Registrar saída:** informe produto, quantidade e observações. O sistema escolhe os lotes por FEFO e respeita disponibilidade, bloqueios, idade e validade.

### Reservas

Informe produto, tipo (`CUSTOMER` ou `REPLENISHMENT`), quantidade, referência e expiração opcional.

- CUSTOMER reserva estoque existente. Selecione a reserva ativa e clique **Confirmar saída** para efetivar.
- REPLENISHMENT reserva capacidade futura. Para adicionar estoque, registre uma entrada real associada a reserva.
- **Cancelar** libera a quantidade/capacidade comprometida por uma reserva ativa.

### Bloqueios operacionais

Escolha produto, operação (`INBOUND`, `OUTBOUND` ou `BOTH`) e motivo. Para liberar, selecione o registro e informe o responsavel.

### Bloqueios fisicos

Informe produto, quantidade e motivo. A quantidade e alocada entre lotes livres. Para liberar, selecione o registro e informe o responsavel.

### Estoque, movimentações e lotes

Estoque mostra quantidade, capacidade, percentual, classificação e disponibilidade. Movimentacoes apresenta o histórico de operações. Lotes mostra código, fornecedor, datas, quantidade recebida/restante e custo. Os botoes de exportação geram arquivos CSV no local escolhido.

## 5. Cuidados importantes

- Selecione uma linha da tabela antes de editar, desativar, reativar, confirmar ou liberar.
- IDs de fornecedor e reserva sao valores numericos exibidos nas respectivas tabelas.
- Uma reserva de reposição so aumenta estoque após entrada real.
- Nao apague `stockflow.db` se quiser preservar os dados.
- O campo de responsavel pela liberação registra um texto informado, não um usuário autenticado.

## 6. Onde ficam os dados

O caminho do banco e `stockflow.db` no diretório de trabalho atual. O arquivo BAT executa `cd /d "%~dp0"`, garantindo que o programa seja iniciado a partir da pasta do projeto. Se executar pelo terminal, entre na pasta `StockFlow_Java` antes do comando Maven.

## 7. Solução de problemas

- **`java`/`javac` não reconhecido:** instale o JDK e configure `JAVA_HOME`/PATH; abra novo PowerShell.
- **`mvn` não reconhecido:** instale Apache Maven e adicione a pasta `bin` ao PATH. O pacote não inclui `mvnw.cmd`.
- **Maven não baixa dependências:** verifique conexão com internet e acesso ao repositorio Maven.
- **Banco inesperado ou duplicado:** confirme que o terminal esta na pasta `StockFlow_Java` antes da execução.
- **Erro na operação:** leia a mensagem apresentada no dialogo; confirme que a linha correta foi selecionada e que os dados estao preenchidos.

A fonte Java foi compilada com `javac --release 17` na verificação desta entrega. A execução completa via Maven e o acesso ao driver SQLite JDBC ainda precisam ser confirmados com Maven instalado.
