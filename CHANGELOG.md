# Histórico de Alterações

Todas as alterações relevantes deste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e segue o [Versionamento Semântico](https://semver.org/lang/pt-BR/), quando aplicável.

## [Não lançado]

### Adicionado

- Interface de terminal para gerenciamento de candidatos, empresas e vagas.
- Frontend em TypeScript com cadastro, login, perfis, vagas e curtidas, inicialmente utilizando LocalStorage de forma independente do backend.
- Gráfico de competências dos candidatos no frontend.
- Persistência no PostgreSQL via JDBC para candidatos, empresas, vagas, competências, curtidas e matches.
- Consulta de vagas curtidas, candidatos interessados e matches por candidato ou empresa no backend.
- Desativação de candidatos e empresas, atualização de cadastros e gerenciamento de competências de candidatos e vagas no backend.
- API HTTP com Tomcat embarcado e Jakarta Servlet para cadastro, consulta e atualização de candidatos, empresas e vagas, além da consulta de competências.
- Integração do frontend com a API HTTP para cadastro, login, consulta de perfis, gerenciamento de vagas, curtidas e visualização de matches.
- Clientes HTTP e DTOs no frontend para comunicação com o backend e tratamento de falhas nas requisições.
- Consulta de países, estados e competências do backend para preenchimento dos formulários.
- Testes unitários dos serviços com Spock e geração de relatórios de cobertura com JaCoCo.
- Orientações para execução e contribuição na documentação do projeto.

### Modificado

- Substituído o armazenamento em memória do backend por persistência em PostgreSQL.
- Reorganizado o backend em controllers, serviços e contratos de persistência, com composição explícita por construtores.
- Centralizado o armazenamento local do frontend em repositórios e separado o login por perfil com Strategy.
- Coordenadas a curtida empresarial e a gravação do match por meio de MatchFacade.
- Migrados os fluxos integrados do frontend de repositórios locais para comunicação HTTP com persistência no backend, mantendo a sessão no LocalStorage.

### Corrigido

- Corrigida a consulta das competências do candidato para utilizar o repositório de associações entre candidatos e competências.
