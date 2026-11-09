# Registro de desenvolvimento - StockFlow Java

## 1. Escopo

Este registro reúne as principais solicitações, decisões técnicas, correções de ambiente e verificações relacionadas ao StockFlow como aplicação desktop desenvolvida em Java, com Swing, JDBC e SQLite.

## 2. Requisitos e solicitações funcionais

- Disponibilizar cadastro e manutenção de produtos e fornecedores.
- Controlar entradas, saídas, reservas, bloqueios, estoque, lotes e movimentações.
- Exportar informações para arquivos CSV.
- Aplicar as regras de classificação percentual, limites por categoria, exceção de entrada abaixo de 25%, restrição de saída em condições específicas e seleção FEFO.
- Manter os dados em um banco SQLite local.
- Preparar instruções de instalação, execução e utilização para usuários iniciantes.

## 3. Decisões e resultados de implementação

- A interface utiliza Swing e é organizada pela classe `StockFlowApp`.
- A classe interna `StockService` concentra as regras de negócio, consultas JDBC e operações de persistência.
- O arquivo `pom.xml` declara Java 17, o driver SQLite JDBC e os plugins Maven de compilação e execução.
- As consultas utilizam `PreparedStatement`; os recursos JDBC são gerenciados com try-with-resources.
- Operações compostas utilizam transações, e a aplicação exporta os dados para CSV.

## 4. Problema de configuração do Maven

Durante a preparação do ambiente, o PowerShell informou que o comando `mvn` não era reconhecido. A verificação `Test-Path .\mvnw.cmd` retornou `False`, indicando que o projeto não contém Maven Wrapper.

A orientação registrada foi instalar o Apache Maven, adicionar a pasta `bin` ao `PATH` e abrir um novo terminal. O projeto requer Maven instalado para executar os comandos definidos no `pom.xml`.

## 5. Verificações realizadas

- O código-fonte foi compilado com `javac --release 17` durante a preparação da documentação.
- Maven não estava instalado no ambiente de preparação; por isso, não foi possível executar `mvn compile exec:java` nem verificar a conexão JDBC em tempo de execução.
- O pacote não inclui uma suíte automatizada de testes Java.

A compilação do fonte com `javac` não substitui a verificação da execução completa com Maven e o driver SQLite JDBC.

## 6. Utilização da IA

A IA auxiliou na tradução das regras de negócio em métodos Java, na organização da janela Swing e do serviço de aplicação, na configuração do `pom.xml`, na explicação da relação entre JDK e Maven, na orientação para corrigir a configuração do `PATH` e na elaboração da documentação técnica e operacional.

## 7. Limitações registradas

- A execução integral via Maven e a conexão com SQLite JDBC ainda precisam ser verificadas em um ambiente com Maven instalado.
- Não há suíte automatizada de testes Java incluída nesta distribuição.
- A interface e o serviço estão concentrados em um arquivo Java principal, o que aumenta o acoplamento.
- Não há autenticação, perfis de acesso, trilha de auditoria completa, expiração automática de reservas ou benchmark formal de carga.
