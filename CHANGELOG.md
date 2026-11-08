# Histórico de Alterações

Todas as alterações relevantes deste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e segue o [Versionamento Semântico](https://semver.org/lang/pt-BR/), quando aplicável.

## [Não lançado]

### Adicionado

- Consultas backend de matches restritas ao candidato titular, empresa proprietária ou recrutador autor ativo, com nome e área do autor no contrato pós-match, inclusive quando ele está inativo no histórico (T10).
- Testes de visibilidade de perfis, JSON anônimo e consultas JDBC de matches para isolamento por titular/vaga, autoria histórica e preservação dos campos autorizados sem credenciais (T10).
- Operações backend explícitas de retribuição e consulta de interessados por empresa proprietária ou recrutador autor ativo, com contratos anônimos preservados e continuidade empresarial após inatividade do autor (T09).
- Testes de fachada e JDBC para isolamento por vaga/ator, idempotência concorrente, locks de atividade/autoria/interesse, rollback entre retribuição e match e posse das conexões transacionais (T09).
- Criação backend de vagas diretas e de recrutador, com empresa derivada do vínculo persistido, validação de atividade e gravação transacional de vaga e competências; consultas internas por proprietária ou autor e identificação histórica do recrutador, inclusive inativo (T08).
- Testes unitários, de servlet e JDBC para autoria, isolamento entre empresas/recrutadores, rollback de criação parcial, preservação de vínculos na atualização e contratos anônimos de vagas (T08).
- Login HTTP de candidato, empresa e recrutador com resposta restrita a ID/tipo, renovação de sessão nativa e erros sem credenciais; autenticação do recrutador verifica o hash no serviço (T07).
- Contexto HTTP reutilizável com revalidação de atividade persistida, invalidação de sessões de contas inativas, cookie configurado com HttpOnly/SameSite=Lax e testes de login, sessão e rejeição de identidade enviada pelo cliente (T07).
- Cadastro backend de recrutador pela empresa ativa, com área persistida, senha derivada, atividade inicial forçada, consulta restrita ao próprio perfil por DTO sem credenciais e leitura das áreas de recrutamento (T06).
- Protocolo transacional JDBC de validação de identificadores, com locks ordenados nos três perfis, unicidade concorrente de e-mail entre contas ativas e conflitos de domínio sem detalhes SQL (T06).
- Testes unitários de recrutador e integração PostgreSQL com conexões independentes para concorrência, rollback, liberação de locks e reutilização de identificadores sem transferência de vínculos históricos (T06).
- Serviço de senhas PBKDF2-HMAC-SHA256 com 600.000 iterações, salt aleatório individual de 16 bytes, chave de 256 bits e representação versionada com validação estrita, sem fallback para texto simples (T05).
- Testes unitários e JDBC de derivação, login, atualização de senha e bloqueio de contas inativas para candidatos e empresas, incluindo ausência de credenciais nos erros e nos DTOs de perfil serializados (T05).
- FKs de reciprocidade e propriedade empresarial no DDL atual, impedindo retribuição sem interesse na vaga e match sem a retribuição correspondente; vínculo opcional do recrutador ator à mesma empresa (T04).
- Testes JDBC de contexto candidato/vaga, rejeição de matches órfãos e empresas divergentes, repetição sem duplicatas e dependências de exclusão, preservando os cascades existentes (T04).
- Índices únicos parciais de CPF/CNPJ e e-mail por perfil para contas ativas no DDL atual; a unicidade de e-mail entre perfis permanece para T06 (T03).
- Testes JDBC de conflitos de identificadores ativos, reutilização após inatividade, CPF coincidente entre candidato e recrutador e preservação dos relacionamentos históricos por ID (T03).
- Autoria opcional de vagas no DDL atual, com FK composta que exige a mesma empresa do recrutador, e chaves únicas de suporte em recrutadores e vagas (T02).
- Testes JDBC de vagas diretas e autorais, rejeição de vínculos incompatíveis e preservação da autoria após inatividade do recrutador (T02).
- DDL de áreas de recrutamento e recrutadores, com vínculos obrigatórios de empresa e área, na seção atual do SQL (T01).
- Testes JDBC do schema de recrutadores e suporte a PostgreSQL dedicado, com schema temporário, rollback e bloqueio ao banco de desenvolvimento (T01).
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

- Consultas HTTP de candidato e empresa por ID restritas ao próprio perfil com sessão ativa; listagens identificáveis bloqueadas. A mesma restrição de titular protege a atualização existente desses perfis (T10).
- Retribuição contextualizada por candidato/vaga e gravação de match na mesma conexão/transação, com revalidação de atividade, propriedade, autoria e interesse prévio; interesse inicial permanece único e não gera match. Operações legadas sem contexto de ator/vaga e gravação direta de match são bloqueadas (T09).
- Renomeado o método empresarial de criação de vagas para `criarVagaEmpresa` no controller, serviço e testes, explicitando o ator e evitando sobrecarga ambígua sobre o contexto de autorização. Preservados `criarVagaRecrutador` e o método legado `criarVaga(CadastrarVagaDTO)`, sem mudanças de comportamento; convenção permanente registrada em `AGENTS.md`.
- Cadastro, atualização e desativação nos DAOs de candidato e empresa participam do mesmo protocolo de gravação de contas; cadastro de recrutador utiliza esse protocolo, preservando CPF por perfil, CNPJ empresarial e reutilização após inatividade. Testes JDBC de senhas passam a usar conexões próprias com transações reais em schema isolado (T06).
- Cadastro e atualização de candidatos e empresas derivam a senha antes da persistência; login busca conta ativa por e-mail e verifica o hash no serviço, sem comparar senha no SQL (T05).
- Retribuição empresarial contextualizada por vaga no DDL atual, com ator recrutador opcional e unicidade por candidato/vaga também no match; adaptação dos DAOs e atomicidade permanecem para T09 (T04).
- Substituídas as unicidades permanentes de CPF/e-mail de candidatos e CNPJ/e-mail de empresas por unicidades condicionadas à atividade, permitindo reutilização sem transferência de histórico (T03).
- Definido `ativo NOT NULL DEFAULT TRUE` para candidatos no DDL atual, alinhado ao estado inicial de empresas e recrutadores (T01).
- Substituído o armazenamento em memória do backend por persistência em PostgreSQL.
- Reorganizado o backend em controllers, serviços e contratos de persistência, com composição explícita por construtores.
- Centralizado o armazenamento local do frontend em repositórios e separado o login por perfil com Strategy.
- Coordenadas a curtida empresarial e a gravação do match por meio de MatchFacade.
- Migrados os fluxos integrados do frontend de repositórios locais para comunicação HTTP com persistência no backend, mantendo a sessão no LocalStorage.

### Corrigido

- Consulta empresarial de competências do candidato nos matches utiliza `buscarPorId`, conforme o contrato de persistência (T10).
- Exclusão JDBC de vaga remove match e retribuição antes da vaga na mesma transação, revertendo todas as remoções em caso de falha e preservando os cascades existentes (T09).
- Criação de vaga não deixa registros parciais quando uma competência é inválida ou uma associação falha; atualização valida o escopo empresarial sem alterar propriedade ou autoria. Consulta HTTP por ID utiliza o DTO anônimo, evitando expor os campos internos da entidade ampliada (T08).
- Removida a exposição de credenciais nas exceções de login de candidato e empresa; atualizações rejeitam contas inativas no serviço e no SQL e preservam a atividade persistida sem confiar no DTO (T05).
- Corrigida a consulta das competências do candidato para utilizar o repositório de associações entre candidatos e competências.
