# Revisão e melhoria de desempenho - StockFlow_Java

## 1. Objetivo

Este relatório descreve as escolhas de desempenho presentes no código Java e as validações efetivamente executadas. Nao foi realizado benchmark formal.

## 2. Estrategias presentes no código

### Consultas JDBC parametrizadas

Os metodos `exec()`, `scalar()`, `query()` e `one()` utilizam `PreparedStatement`, centralizando a associacao de parâmetros e reduzindo montagem repetitiva de comandos SQL. O código usa try-with-resources para fechar automaticamente statements e result sets.

### Reutilizacao de inventario na interface

`refreshAll()` obtem o resultado de `service.inventory(true)` uma vez e reutiliza a lista para as tabelas de visão geral, estoque e lista de produtos ativos. Isso evita repetir a mesma consulta de resumo para cada grade durante a mesma atualização.

### Agregacao de quantidade livre

`free()` inicia com o saldo restante dos lotes e aplica duas consultas agregadas: reservas CUSTOMER ativas e bloqueios fisicos ativos. Os totais sao agrupados por lote e armazenados em um mapa. `allocate()` utiliza esse mapa para distribuir a quantidade em ordem FEFO.

### Escrita de CSV

A montagem das linhas de CSV usa `String.join()` para reunir os campos com delimitador ponto e virgula, evitando concatenacoes sucessivas em loops externos. O arquivo recebe BOM UTF-8 para facilitar leitura por ferramentas que dependem dessa assinatura.

## 3. Preservacao de regras

A revisão documental não alterou percentuais, categorias, datas, FEFO, exceção de entrada abaixo de 25%, restricao de saída com última remessa inferior a 3 ou superior a 31 dias, reservas, bloqueios ou fluxo de recebimento.

## 4. Validacoes realizadas

O fonte Java foi compilado com `javac --release 17` com sucesso. O `pom.xml` foi inspecionado e declara Java 17, Xerial SQLite JDBC 3.46.1.3 e plugins Maven para compilação/execução.

A execução via Maven e o teste com driver SQLite não foram realizados porque Maven não estava instalado no ambiente de preparação. O pacote também não inclui suite automatizada de testes Java.

## 5. Limites e oportunidades futuras

- `inventory()` percorre os produtos e calcula disponibilidade produto a produto; para grandes volumes, consolidar as consultas de lotes/reservas/bloqueios em lote pode diminuir round-trips.
- Swing atualiza a interface na Event Dispatch Thread; consultas demoradas podem congelar temporariamente a janela. Para volumes maiores, usar `SwingWorker` e atualizar componentes na EDT.
- Nao existe benchmark de latencia, throughput ou concorrencia.
- Nao foi validada a exportação com arquivos grandes nem a restauracao de backup.

## 6. Conclusao

O código utiliza consultas parametrizadas, fechamento automático de recursos, transações, agregacao de disponibilidade por lote e reaproveitamento da consulta de inventario entre tabelas. A sintaxe Java compila sob release 17; a execução Maven e o desempenho sob carga ainda precisam de validação.
