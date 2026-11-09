# Testes automatizados - StockFlow Java

## Objetivo

Os testes automatizados verificam os principais fluxos do serviço de estoque e a persistência SQLite. Eles são testes de integração: chamam os métodos reais de `StockService` e utilizam um banco de dados temporário criado para cada teste.

## Tecnologia

- JUnit 5 para organização e execução dos testes.
- Maven Surefire para localizar e executar os testes durante a fase `test` do Maven.
- SQLite JDBC para executar as operações reais de persistência.

## O que é verificado

1. Cadastro de produto e saldo inicial zerado.
2. Entrada de mercadorias, criação de lote e movimentação.
3. Bloqueio de entrada acima da capacidade.
4. Saída permitida e atualização de saldo/lote.
5. Rejeição de saída acima da quantidade disponível, sem alterar o saldo.
6. Múltiplas reservas de cliente e rejeição de reservas acima da disponibilidade.
7. Cancelamento e confirmação de reservas.
8. Criação e liberação de bloqueio físico.
9. Bloqueio operacional de entrada e saída, incluindo liberação.
10. Reserva de reposição e baixa da reserva no recebimento.
11. Exceção de entrada quando o estoque está abaixo de 25%, respeitando o limite exato de 25%.
12. Regra de saída com estoque abaixo de 25% e última remessa há menos de três dias.
13. Limite geral de saída para estoque em 15%.
14. Rejeição de lote com idade superior ao limite de qualidade.

## Como executar no Windows

1. Instale JDK 17 ou superior e Apache Maven.
2. Abra o PowerShell na pasta `StockFlow_Java`.
3. Execute:

```powershell
mvn test
```

Para executar apenas a classe de testes do serviço:

```powershell
mvn -Dtest=StockFlowServiceTest test
```

O resultado resumido é apresentado no terminal. Os relatórios detalhados são gerados em:

```text
target/surefire-reports/
```

## Isolamento dos dados

Cada teste recebe um diretório temporário (`@TempDir`) e cria nele o seu próprio banco SQLite. Assim, os testes não utilizam o banco `stockflow.db` da aplicação nem dependem de dados cadastrados manualmente.

## Como interpretar o resultado

- `BUILD SUCCESS`: a fase de testes terminou sem falhas.
- `Failures`: uma ou mais verificações produziram resultado diferente do esperado.
- `Errors`: ocorreu uma exceção inesperada durante um teste.
- `Skipped`: teste não executado por alguma condição de exclusão.

Um resultado positivo comprova os cenários cobertos por estes testes; não comprova, sozinho, ausência de defeitos em todos os cenários, concorrência entre vários usuários ou adequação para produção.
