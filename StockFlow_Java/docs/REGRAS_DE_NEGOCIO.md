# Regras de negócio - StockFlow

Este documento consolida as regras de negócio aplicadas pelo StockFlow. Os limites são parâmetros definidos para o projeto demonstrativo e não constituem recomendação técnica, sanitária ou regulatória.

## Classificacao do estoque

O percentual e calculado como quantidade física atual dividida pela capacidade máxima, multiplicado por 100.

| Percentual | Classificacao |
|---|---|
| 80% ou mais | ALTO |
| 50% até abaixo de 80% | ACEITAVEL |
| 25% até abaixo de 50% | BAIXO |
| Abaixo de 25% | CRITICO |

## Qualidade geral por idade do lote

A idade e contada em dias corridos a partir da data de recebimento.

| Idade | Classificacao |
|---|---|
| 0 a 10 dias | OTIMA |
| 11 a 19 dias | BOA |
| 20 a 30 dias | BAIXA |
| 31 dias ou mais | PROIBIDO_CONSUMO |

Uma data de recebimento futura e recusada. A data de validade informada também e verificada; um lote vencido não e elegivel para saída.

## Regras por categoria

| Categoria/código | Limite de idade da última remessa para autorizar entrada | Intervalo mínimo que bloqueia nova entrada | Limite de idade aplicado a saída |
|---|---:|---:|---:|
| Carnes / MEAT | mais de 5 dias | menos de 2 dias | menos de 6 dias |
| Frutas / FRUIT | mais de 15 dias | menos de 10 dias | menos de 20 dias |
| Legumes / LEGUME | mais de 15 dias | menos de 10 dias | menos de 20 dias |
| Vegetais e folhas / GREENS | mais de 15 dias | menos de 10 dias | menos de 20 dias |
| Laticinios / DAIRY | mais de 20 dias | menos de 15 dias | menos de 25 dias |
| Cereais / CEREAL | mais de 30 dias | menos de 30 dias | menos de 40 dias, limitado pela regra geral de qualidade |

## Entradas

A entrada e submetida a todas as validações abaixo:

- Um bloqueio operacional INBOUND ou BOTH impede a operação.
- Estoque acima de 85% da capacidade impede a entrada.
- A regra de intervalo mínimo por categoria e aplicada quando o estoque esta em 25% ou mais.
- A autorizacao-base exige estoque abaixo de 60% ou idade da última remessa superior ao limite da categoria.
- **Excecao de estoque crítico:** quando a quantidade física esta estritamente abaixo de 25%, a restricao de intervalo mínimo da categoria deixa de impedir a reposição. Essa exceção não elimina os bloqueios manuais, o teto de 85%, a capacidade máxima nem as demais validações.
- A capacidade considera estoque físico e capacidade reservada para reposição; o recebimento vinculado a uma reserva de reposição libera a parcela correspondente dessa reserva.

Em exatamente 25%, a exceção de estoque crítico não se aplica.

## Saidas

- O percentual de estoque deve ser estritamente superior a 15%; em 15% ou menos, a saída e recusada.
- Um bloqueio operacional OUTBOUND ou BOTH impede a saída.
- Quando o estoque estiver estritamente abaixo de 25%, a saída e bloqueada se a idade da última remessa for inferior a 3 dias ou superior a 31 dias. Exatamente 3 ou 31 dias não acionam essa regra adicional.
- A disponibilidade deve descontar reservas ativas de clientes e bloqueios fisicos ativos.
- Lotes vencidos, proibidos pela idade/qualidade ou fora do limite da categoria não podem ser utilizados.
- A seleção segue FEFO (First Expire, First Out): lotes com validade mais próxima primeiro; lotes sem validade ficam depois dos lotes com validade definida.

## Reservas

- CUSTOMER reserva unidades de lotes existentes. A quantidade fica comprometida e deixa de estar disponível para outras reservas/saídas, mas so e abatida do estoque físico quando a reserva e confirmada.
- A confirmação de CUSTOMER efetiva a saída, registra a movimentação e reduz estoque e reserva.
- REPLENISHMENT reserva capacidade futura. Ela não adiciona unidades ao estoque físico.
- A reserva REPLENISHMENT e concluida por uma entrada real, com quantidade, lote, data, fornecedor e, quando usado, ID da reserva.
- Reservas ativas podem ser canceladas; a quantidade comprometida e liberada.
- O campo de expiração e armazenado, mas não existe rotina automática de expiração nesta distribuição.

## Bloqueios

- Bloqueio operacional: INBOUND, OUTBOUND ou BOTH, com motivo.
- Bloqueio físico: separa quantidade de lotes específicos e reduz a quantidade livre.
- A liberação exige o nome do responsavel e so ocorre se o registro estiver ativo.

## Dados e rastreabilidade

O modelo registra produtos, fornecedores, saldos, lotes, operações, movimentações, reservas, alocacoes de reservas, bloqueios operacionais, bloqueios fisicos e alocacoes dos bloqueios fisicos. O aplicativo registra os dados e a rastreabilidade das operações em seu arquivo SQLite local.

## Observacoes de interpretacao

- A solicitação de reposição iniciada na aba Fornecedores cria uma reserva REPLENISHMENT cuja referência registra o nome do fornecedor. Ela não e uma ordem de compra formal nem cria uma entidade completa de compras.
- A liberação de bloqueio grava o responsavel informado no formulario, mas não equivale a uma trilha de auditoria autenticada.
- Os limites de idade e percentual sao regras do exercicio e devem ser aprovados por especialistas antes de qualquer uso operacional real.
