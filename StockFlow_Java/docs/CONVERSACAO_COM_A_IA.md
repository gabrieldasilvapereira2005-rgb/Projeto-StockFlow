# Registro de desenvolvimento e uso de IA - StockFlow Java

## 1. Objetivo deste registro

Este documento registra as decisões centrais e as interações de IA que mais influenciaram a construção da edição Java do StockFlow. A solução é uma aplicação desktop em Java, com Swing, JDBC e SQLite, gerenciada por Maven.

## 2. Escopo e requisitos considerados

O PDF do case exige identificação do produto, quantidades total/disponível/reservada/bloqueada, entrada, saída, reserva, bloqueio, liberação e consulta, além de consistência do estoque. O case também solicita código-fonte, testes automatizados, README, registro do uso de IA e cinco aspectos de engenharia.

A edição StockFlow ampliou esse núcleo com cadastro de fornecedores, lotes, movimentações, classificações por categoria e idade, bloqueios operacionais e físicos, exportação CSV e interface desktop. Essas extensões são decisões do projeto, não exigências literais do PDF.

## 3. Decisões e tecnologias da edição Java

- Java com compatibilidade de compilação Java 17.
- Swing para a interface desktop.
- SQLite local, acessado por JDBC e pelo driver Xerial SQLite JDBC.
- Maven para dependências, compilação, execução e testes.
- `BigDecimal` para quantidades, valores e percentuais; `java.time` para datas.
- `PreparedStatement`, `try-with-resources` e transações JDBC para operações persistentes.
- `StockFlowApp` concentra a janela e a classe interna `StockService` concentra as regras e operações de negócio.
- JUnit 5 e Maven Surefire para testes automatizados do serviço com bancos SQLite temporários.

## 4. Os cinco prompts de maior impacto

### Nota sobre a fidelidade histórica

O pacote e os registros disponíveis preservam as decisões, regras e temas das interações, mas não contêm a transcrição literal completa de todas as mensagens enviadas à IA durante a construção original. Por isso, os cinco textos abaixo são **prompts completos reconstruídos e consolidados** a partir dos requisitos do PDF, das regras de negócio, do código e da documentação do StockFlow Java. Eles foram escritos para representar com precisão o conteúdo e a profundidade das solicitações que mais impactaram o projeto; não devem ser apresentados como cópias literais de mensagens históricas.

#### Prompt 1 - Análise do case e especificação verificável

```text
Atue como um engenheiro de software sênior responsável por analisar requisitos e orientar a construção de uma solução de controle de estoque. Leia integralmente o PDF "Case Prático - Controle de Estoque" e use o documento como referência principal para os requisitos do desafio. Considere também o contexto do projeto StockFlow Java descrito nesta solicitação.

O objetivo é desenvolver uma aplicação desktop de controle de estoque em Java. Neste projeto, a escolha é Java; a interface será desktop, com Swing. Não transforme a solução em aplicação web e não utilize HTML, CSS ou JavaScript. O PDF permite liberdade de arquitetura, banco de dados, bibliotecas e forma de exposição das funcionalidades; portanto, justifique as escolhas em vez de afirmar que existe uma arquitetura obrigatória.

Comece separando claramente:
1. Requisitos expressamente exigidos pelo PDF: identificação do produto, quantidade total, disponível, reservada e bloqueada; entrada; saída; reserva; bloqueio; liberação; consulta; múltiplas reservas e bloqueios simultâneos; preservação da consistência; testes automatizados; README; registro do uso de IA; e cinco aspectos de engenharia.
2. Funcionalidades adicionais adotadas para a edição StockFlow: cadastro de fornecedores, lotes, movimentações, classificação por categoria e idade, interface desktop, exportação CSV e persistência local.
3. Premissas que precisam ser definidas para transformar o enunciado em regras executáveis. Não apresente uma premissa como se estivesse escrita no PDF.

Para cada requisito, descreva o comportamento esperado e um critério de aceite verificável. Inclua cenários normais, limites, operações inválidas e estados simultâneos. Dê atenção especial à definição das quantidades: diferencie capacidade máxima do produto, quantidade física em estoque, quantidade comprometida por reservas, quantidade bloqueada fisicamente e quantidade disponível para saída. Explique quais operações alteram cada indicador.

Não invente regras de negócio, limites percentuais ou prazos sem identificá-los como decisões do projeto. Quando houver informação insuficiente, registre a lacuna e apresente a pergunta ou premissa necessária. Não simplifique reservas e bloqueios para um único campo agregado: cada registro precisa ter identificação própria para permitir consulta, cancelamento ou liberação independente.

Entregue como resultado:
- uma matriz de requisitos com origem (PDF ou decisão do projeto), comportamento e critério de aceite;
- uma lista das entidades e informações que precisam ser persistidas;
- os fluxos de entrada, saída, reserva, bloqueio, liberação e consulta;
- os principais riscos de inconsistência e como evitá-los;
- as decisões técnicas que precisam ser documentadas;
- uma lista de testes automatizados necessários;
- uma estrutura objetiva para README, documentação do uso de IA e visão de engenharia.

A solução deve ser proporcional a um case técnico de escopo limitado. Priorize correção, clareza, rastreabilidade e facilidade de explicação, evitando complexidade desnecessária.
```

#### Prompt 2 - Arquitetura Java, modelagem e persistência local

```text
Atue como arquiteto de software e engenheiro Java sênior. Com base nos requisitos já levantados para o StockFlow, defina e implemente uma arquitetura desktop simples, local e justificável para o desafio técnico.

Decisões tecnológicas desta edição:
- linguagem: Java;
- versão de compilação: Java 17;
- interface desktop: Java Swing;
- persistência: SQLite local;
- acesso a dados: JDBC com driver SQLite JDBC;
- gerenciamento de dependências, compilação e execução: Maven;
- não utilizar HTML, CSS, JavaScript, servidor web ou banco de dados externo.

A aplicação deve funcionar localmente em uma máquina Windows com JDK e Maven instalados. O arquivo SQLite deve ser criado/aberto localmente, sem exigir instalação de um servidor de banco de dados. Documente o diretório de trabalho esperado e evite que os testes automatizados utilizem o banco real da aplicação.

Modele, no mínimo, os conceitos já previstos no projeto: produtos, fornecedores, saldos de estoque, lotes, operações, movimentações, reservas, alocações de reservas, bloqueios operacionais, bloqueios físicos e alocações de bloqueios físicos. A modelagem deve permitir várias reservas e vários bloqueios ativos para um mesmo produto, inclusive associados a solicitações distintas. Cada reserva e cada bloqueio precisam de identificador próprio, status e dados suficientes para consulta e encerramento independente.

Não confunda capacidade máxima com quantidade física. Diferencie também:
- quantidade física existente;
- quantidade comprometida por reservas CUSTOMER;
- capacidade futura comprometida por reservas REPLENISHMENT;
- quantidade indisponível por bloqueios físicos;
- restrições operacionais que impedem entrada, saída ou ambas, sem necessariamente retirar unidades do saldo físico.

Adote `BigDecimal` para quantidades, preços e percentuais, evitando cálculos centrais com `float` ou `double`. Utilize `java.time` para datas de recebimento, validade e idade dos lotes. Use `PreparedStatement` nas consultas parametrizadas, `try-with-resources` para fechamento de recursos JDBC e transações para operações que atualizam mais de uma tabela. Uma falha durante uma operação composta deve provocar rollback, sem deixar lote, saldo, reserva ou movimentação parcialmente atualizados.

Mantenha a solução simples para o tamanho do desafio. A interface pode ficar na classe principal `StockFlowApp`, enquanto a lógica de aplicação e as operações de estoque ficam concentradas em `StockService`, desde que as responsabilidades sejam claras e as regras não dependam exclusivamente da interface. Caso a implementação permaneça em um único arquivo Java, explique essa escolha e registre a separação lógica entre interface, serviço e persistência.

Configure o `pom.xml` para compilar com `maven.compiler.release=17`, incluir o driver SQLite JDBC, incluir JUnit 5 no escopo de testes e configurar Maven Surefire para localizar os testes. Configure também o plugin necessário para iniciar a classe principal `stockflow.StockFlowApp`.

Entregue o esquema SQLite, a estrutura de código, as principais decisões de modelagem, a forma de inicialização do banco, os comandos Maven e as limitações dessa arquitetura para uma operação com vários usuários simultâneos. Não afirme que SQLite local é uma solução multiusuário de produção sem testes específicos que sustentem essa conclusão.
```

#### Prompt 3 - Regras de negócio, limites e condições de fronteira

```text
Implemente as regras de negócio do StockFlow exatamente conforme os parâmetros abaixo. Centralize as validações no serviço de negócio para que nenhuma regra importante dependa apenas dos campos ou botões da interface. Não altere limites, não arredonde condições de fronteira e não crie exceções além das descritas.

1. Percentual e classificação do estoque
O percentual é calculado pela quantidade física atual dividida pela capacidade máxima do produto, multiplicada por 100. Classifique assim:
- 80% ou mais: ALTO;
- de 50% até abaixo de 80%: ACEITAVEL;
- de 25% até abaixo de 50%: BAIXO;
- abaixo de 25%: CRITICO.

2. Idade e qualidade dos lotes
Calcule a idade em dias corridos a partir da data de recebimento:
- 0 a 10 dias: OTIMA;
- 11 a 19 dias: BOA;
- 20 a 30 dias: BAIXA;
- 31 dias ou mais: PROIBIDO_CONSUMO.
Recuse data de recebimento futura. Um lote vencido também não pode ser selecionado para saída, independentemente da quantidade restante.

3. Regras por categoria
Use os seguintes parâmetros:
- MEAT (carnes): para autorizar entrada, a última remessa deve ter mais de 5 dias; o intervalo mínimo que bloqueia nova entrada é menor que 2 dias; limite de idade aplicado à saída: menos de 6 dias.
- FRUIT (frutas): última remessa com mais de 15 dias; bloqueio de nova entrada quando o intervalo for menor que 10 dias; limite de saída: menos de 20 dias.
- LEGUME (legumes): última remessa com mais de 15 dias; bloqueio de nova entrada quando o intervalo for menor que 10 dias; limite de saída: menos de 20 dias.
- GREENS (vegetais e folhas): última remessa com mais de 15 dias; bloqueio de nova entrada quando o intervalo for menor que 10 dias; limite de saída: menos de 20 dias.
- DAIRY (laticínios): última remessa com mais de 20 dias; bloqueio de nova entrada quando o intervalo for menor que 15 dias; limite de saída: menos de 25 dias.
- CEREAL (cereais): última remessa com mais de 30 dias; bloqueio de nova entrada quando o intervalo for menor que 30 dias; limite de saída: menos de 40 dias, respeitando também a regra geral de qualidade.

4. Regras de entrada
- Bloqueio operacional INBOUND ou BOTH impede a entrada.
- Estoque acima de 85% da capacidade impede a entrada.
- A regra de intervalo mínimo por categoria é aplicada quando o estoque está em 25% ou mais.
- A autorização-base exige estoque abaixo de 60% ou idade da última remessa superior ao limite definido para a categoria.
- Exceção crítica: quando a quantidade física está estritamente abaixo de 25% da capacidade, o intervalo mínimo da categoria deixa de impedir a reposição. Essa exceção não elimina bloqueios manuais, teto de 85%, capacidade máxima ou as demais validações.
- Em exatamente 25%, a exceção crítica não se aplica.
- A capacidade deve considerar o estoque físico e a capacidade já reservada para reposição. Quando uma entrada está vinculada a uma reserva REPLENISHMENT, a parcela correspondente dessa reserva deve ser liberada corretamente.

5. Regras de saída
- O percentual do estoque deve ser estritamente superior a 15%; em 15% ou menos, a saída é recusada.
- Bloqueio operacional OUTBOUND ou BOTH impede a saída.
- Quando a quantidade física estiver estritamente abaixo de 25%, bloquear a saída se a idade da última remessa for inferior a 3 dias ou superior a 31 dias.
- Exatamente 3 dias e exatamente 31 dias não acionam essa regra adicional.
- A quantidade disponível deve descontar reservas CUSTOMER ativas e bloqueios físicos ativos.
- Lotes vencidos, proibidos pela idade/qualidade ou fora do limite de idade da categoria não podem ser usados.
- Distribua a saída usando FEFO (First Expire, First Out): lotes com vencimento mais próximo primeiro; lotes sem validade definida ficam depois dos que possuem validade.

6. Precisão das fronteiras
Inclua validações explícitas para 15%, 25%, 60%, 85%, 3 dias e 31 dias. Use comparações estritas ou inclusivas exatamente como descritas acima. Não considere "abaixo de 25%" como "25% ou menos". Não considere "acima de 15%" como "15% ou mais".

Para cada regra, apresente a condição, a mensagem de erro esperada e pelo menos um cenário permitido e um cenário recusado. Organize a lógica em métodos de serviço reutilizáveis e escreva testes automatizados para os limites mais sensíveis. Se uma regra já existente no código divergir desta especificação, identifique a divergência antes de alterar o comportamento.
```

#### Prompt 4 - Implementação dos fluxos, reservas, bloqueios e consistência

```text
Implemente os fluxos centrais de estoque no `StockService` do StockFlow Java. O objetivo é que entrada, saída, reserva, bloqueio e liberação sejam operações persistentes, identificáveis e consistentes, e não apenas alterações visuais em tabelas da interface.

ENTRADA
Ao receber mercadoria, valide o produto, quantidade, fornecedor, data de recebimento, lote, validade e regras de categoria/percentual. Crie o lote, atualize o saldo físico e registre a operação e a movimentação. Verifique bloqueio operacional de entrada e capacidade máxima. Quando a entrada for vinculada a uma reserva REPLENISHMENT, associe o recebimento à reserva e reduza a quantidade/capacidade reservada correspondente; a reserva não pode ser tratada como mercadoria já recebida.

SAÍDA
Antes de retirar unidades, valide as regras percentuais, a última remessa, o bloqueio operacional de saída e a disponibilidade real. Considere somente lotes elegíveis, não vencidos e dentro dos limites de idade/qualidade. Distribua a retirada por FEFO. Atualize lote, saldo e movimentações dentro de uma única transação. Se a quantidade solicitada exceder a disponível, recuse a operação sem modificar nenhum saldo.

RESERVAS
Implemente dois tipos distintos:
- CUSTOMER: compromete unidades de lotes existentes. A quantidade reservada deixa de estar disponível para outras saídas e reservas, mas não é abatida do estoque físico na criação. A confirmação da reserva efetiva a saída, registra movimentação e reduz o estoque. O cancelamento de uma reserva ativa devolve a quantidade comprometida à disponibilidade.
- REPLENISHMENT: compromete capacidade futura do produto. Não aumenta o estoque físico e não cria lote. É concluída por uma entrada real, informando quantidade, lote, data, fornecedor e, quando aplicável, o identificador da reserva vinculada.

Permita diversas reservas ativas para um mesmo produto e referências diferentes. Cada registro deve ter ID, tipo, referência, quantidade, status e, quando informado, expiração. A confirmação de uma reserva CUSTOMER deve efetivar a saída uma única vez; uma reserva já confirmada não pode reduzir novamente o estoque. Uma reserva cancelada ou encerrada não pode voltar a comprometer saldo.

BLOQUEIOS
Diferencie:
- bloqueio operacional INBOUND, OUTBOUND ou BOTH, com motivo e efeito sobre o tipo de operação permitido;
- bloqueio físico, que separa quantidade de lotes específicos e reduz a quantidade livre para saída.

Permita múltiplos bloqueios independentes para o mesmo produto. Um bloqueio físico não pode exceder a quantidade disponível do lote. Ao liberar, verifique se o registro está ativo e solicite o nome do responsável pela liberação. A liberação deve devolver a quantidade à condição apropriada, sem aumentar indevidamente o saldo físico.

CONSISTÊNCIA E TRATAMENTO DE ERROS
Use transações JDBC para operações que alteram mais de uma tabela. Se qualquer etapa falhar, execute rollback. Garanta que os saldos agregados, quantidades restantes dos lotes, alocações de reservas, alocações de bloqueios e histórico de movimentações permaneçam coerentes. Utilize BigDecimal para quantidades. Parametrize SQL com PreparedStatement e feche recursos JDBC com try-with-resources.

As regras devem ser aplicadas dentro do serviço, mesmo quando a operação for chamada sem passar pela tela. Retorne mensagens compreensíveis em português para produto inexistente, quantidade inválida, disponibilidade insuficiente, lote inelegível, bloqueio ativo, reserva encerrada ou tentativa duplicada. Não converta falha de negócio em sucesso silencioso.

Ao concluir, descreva para cada operação quais dados são lidos, quais registros são criados/atualizados e como a transação preserva a consistência em caso de erro.
```

#### Prompt 5 - Interface desktop, testes automatizados e documentação de entrega

```text
Finalize a aplicação StockFlow como uma ferramenta desktop Java utilizável por uma pessoa sem conhecimento técnico avançado e prepare uma entrega verificável para o case. Use Swing na interface, `StockService` para as regras de negócio, JDBC/SQLite para persistência e Maven para compilação e execução. Não introduza HTML, CSS, JavaScript ou componentes web.

INTERFACE E FLUXOS
Organize a janela em abas claras, com formulários, tabelas, botões de ação, validação dos campos e mensagens em português. Inclua:
- Visão geral: indicadores resumidos, atualização e exportação CSV.
- Produtos: cadastrar, editar, atualizar, desativar e reativar; a ação de reativação deve respeitar a confirmação/espera de aproximadamente cinco segundos definida para o fluxo.
- Fornecedores: cadastrar, editar, atualizar e iniciar solicitação de reposição associada ao fornecedor.
- Entradas e saídas: selecionar produto, informar quantidade e dados do lote/fornecedor/data/validade/custo, indicar reserva de reposição quando aplicável e registrar observações.
- Reservas: criar, consultar, confirmar saída CUSTOMER, cancelar e atualizar.
- Bloqueios operacionais: criar INBOUND, OUTBOUND ou BOTH, consultar e liberar.
- Bloqueios físicos: selecionar produto/lote, informar quantidade e motivo, consultar e liberar com identificação do responsável.
- Estoque: mostrar quantidade física em estoque, capacidade, percentual, classificação e disponibilidade.
- Movimentações: apresentar histórico de entradas e saídas.
- Lotes: mostrar código, fornecedor, recebimento, validade, saldo restante e custo.

Após uma operação bem-sucedida, atualize os indicadores e tabelas relevantes. Em caso de erro, mantenha os dados anteriores na tela e informe o motivo sem encerrar inesperadamente a aplicação. Faça as consultas demoradas fora da Event Dispatch Thread quando necessário, sem atualizar componentes Swing fora da thread de eventos. A exportação CSV deve utilizar delimitador ponto e vírgula e codificação UTF-8 com BOM, conforme o padrão adotado pelo projeto.

TESTES AUTOMATIZADOS
Inclua JUnit 5 e configure Maven Surefire. Crie testes de integração que chamem o serviço real e utilizem um banco SQLite temporário e isolado por teste (por exemplo, com `@TempDir`); nunca utilize o `stockflow.db` da aplicação nos testes. Cubra pelo menos:
- cadastro do produto e saldo inicial;
- entrada, lote e movimentação;
- rejeição de entrada acima da capacidade;
- saída permitida e rejeição de saída acima do disponível sem alterar o saldo;
- múltiplas reservas e rejeição de compromisso acima da disponibilidade;
- cancelamento e confirmação de reserva CUSTOMER, sem efetivar a saída duas vezes;
- criação, excesso e liberação de bloqueio físico;
- bloqueios operacionais de entrada e saída e respectivas liberações;
- reserva REPLENISHMENT baixada no recebimento real;
- exceção de entrada abaixo de 25%, distinguindo o caso de exatamente 25%;
- regra de saída com estoque crítico e última remessa recente;
- limite de saída em 15%;
- lote fora do limite de idade/qualidade.

DOCUMENTAÇÃO E ENTREGA
Prepare README com visão geral, arquitetura, decisões técnicas, premissas, limitações, instruções para execução e o que precisaria mudar para produção. Documente as regras de negócio, o mapa de funcionalidades, o manual para usuário iniciante e os testes automatizados. Registre os principais prompts utilizados com transparência, diferenciando transcrição literal de síntese reconstruída. Não declare que a aplicação inteira foi testada apenas porque a suíte de serviço passou.

Inclua os comandos de compilação, execução e teste no PowerShell, explique os requisitos JDK/Maven e indique onde ficam os relatórios Surefire. Ao revisar o pacote, confira se os documentos correspondem ao código efetivamente entregue, se não há arquivos ou instruções contraditórios e se os testes não alteram o banco real. Apresente separadamente o que foi comprovado por compilação, por testes automatizados e por teste manual da interface.
```

## 5. Contribuições e limites da IA

- **Onde mais ajudou:** decomposição do case em requisitos e critérios de aceite; organização das entidades e dos fluxos; tradução das regras percentuais, temporais e por categoria em validações; apoio à implementação Java/Swing/JDBC; preparação de comandos Maven, testes e documentação.
- **Limitação identificada:** a IA pode ajudar a escrever código e testes, mas uma resposta ou compilação isolada não comprova a execução correta de todos os fluxos. Por isso, os resultados de compilação, testes de serviço e validação manual da interface devem ser registrados separadamente.
- **Sugestão alterada ou não utilizada:** os arquivos disponíveis não preservam um exemplo específico de uma sugestão individual da IA que tenha sido rejeitada ou modificada, com o respectivo motivo. Esse item não foi inventado; deve ser preenchido pelo responsável caso consiga identificar uma interação real.

## 6. Verificação dos testes automatizados

A distribuição contém uma suíte de 19 testes de integração em `src/test/java/stockflow/StockFlowServiceTest.java`, utilizando JUnit 5, Maven Surefire e bancos SQLite temporários. Os cenários incluem cadastro, entrada, saída, reservas, bloqueios, reposição, limites percentuais e idade dos lotes.

Em 9 de novembro de 2026, o usuário executou `mvn test` no Windows 11. O console apresentado registrou:

- testes executados: 19;
- falhas: 0;
- erros: 0;
- ignorados: 0;
- resultado: `BUILD SUCCESS`.

A execução ocorreu com Apache Maven 3.10.0 e Java 27; o projeto está configurado para compilar com `maven.compiler.release=17`. O console também apresentou um aviso de acesso nativo do carregador SQLite; o aviso não impediu a execução nem alterou o resultado dos testes. Este registro reflete o resultado informado pelo usuário, não uma execução independente feita durante a edição deste documento.

A aprovação comprova os cenários cobertos pela suíte, mas não prova ausência de defeitos em toda a aplicação. Os testes são de integração do serviço e persistência; não representam uma automação completa da interface Swing nem teste formal de concorrência entre vários usuários.

## 7. Limitações e evoluções ainda relevantes

- A interface e o serviço permanecem concentrados no arquivo principal `StockFlowApp.java`, o que aumenta o acoplamento.
- Não há automação ponta a ponta da interface gráfica.
- Não há teste formal de concorrência multiusuário, benchmark de carga ou homologação para produção.
- Não há autenticação, perfis de acesso, trilha de auditoria autenticada ou expiração automática de reservas.
- SQLite atende à demonstração local; operação simultânea em várias estações exigiria reavaliação da persistência e do controle transacional.
- O pacote não inclui Maven Wrapper; é necessário instalar o Maven e configurar o PATH.
- A execução bem-sucedida de `mvn test` não substitui a validação manual das telas e dos fluxos completos de entrada, saída, consulta e exportação.
