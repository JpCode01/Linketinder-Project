# Linketinder - ZG-Hero Project

Plataforma de recrutamento que aproxima candidatos e empresas por vagas, competências e interesses. A identificação entre as partes é liberada após um match válido. O projeto integra backend Groovy, frontend TypeScript e PostgreSQL.

## Sobre o projeto

Desenvolvido por **João Pedro Vaz** durante o **Acelera ZG**, com inspiração no LinkedIn e no Tinder. A evolução começou com um MVP no terminal, passou pela interface web e persistência JDBC e chegou à integração HTTP sem frameworks como Spring ou ORM.

A aplicação possui três perfis:

- **Candidato:** cadastra seu perfil e competências, consulta vagas anônimas, demonstra interesse e acompanha matches.
- **Empresa:** cria vagas, consulta interessados, retribui interesses, acompanha matches e cadastra recrutadores vinculados a áreas de recrutamento.
- **Recrutador:** pertence a uma empresa e a uma área; cria vagas e consulta interessados e matches somente nas vagas de sua autoria.

O dashboard empresarial inclui um gráfico de competências dos interessados. A contagem considera cada interesse: um candidato interessado em duas vagas da mesma empresa contribui duas vezes.

O menu de terminal continua disponível para candidatos e empresas. Suas operações incluem atualização de contas e competências, desativação e exclusão de vagas; a interface web não oferece necessariamente todas essas operações.

O estado da extensão de recrutadores está documentado na [especificação](docs/specs/recrutador/spec.md), no [plano](docs/specs/recrutador/plano.md) e nas [evidências de validação](docs/specs/recrutador/validacao.md). O [changelog](CHANGELOG.md) registra as alterações do projeto.

## Tecnologias

As versões abaixo vêm de [build.gradle](build.gradle), [package.json](frontend/package.json) e do Gradle Wrapper. Os JARs em `lib/` não substituem essas declarações.

| Parte | Tecnologias declaradas |
| --- | --- |
| Backend | Groovy e groovy-json 5.0.8; Jakarta Servlet API 6.1.0; Tomcat embarcado 11.0.26 |
| Persistência | PostgreSQL via JDBC; driver 42.7.13; SQL e mapeamento nos DAOs |
| Frontend | HTML, CSS, TypeScript `^5.9.3`, Chart.js `^4.5.1`, DOM e Fetch API |
| Build frontend | Gulp `^4.0.2`, Webpack `^5.110.3` e ts-loader `^9.6.2` |
| Build e testes backend | Gradle Wrapper 9.3.0, Spock 2.4-groovy-5.0, JUnit BOM 6.0.0 e JaCoCo 0.8.14 |
| Testes frontend | Runner nativo `node:test`, com DOM e fetch controlados; aceite separado em Chrome/Chromium |

PIT Mutation Testing fez parte dos exercícios descritos no histórico do README; o Gradle atual não configura plugin ou tarefa de PIT.

## Arquitetura e estrutura de pastas

O backend usa composição manual por construtores em `DaoConfig`, `ServiceConfig` e `ControllerConfig`. `ApiServletConfig` registra as rotas e `TomcatConfig` configura o servidor.

```text
Frontend: Page → Service → ApiClient → fetch com cookies
                                          ↓
Backend: Servlet → Controller → Service → contrato de repositório → DAO → PostgreSQL
```

As estratégias de login delegam ao `LoginApiClient`. `ServiceConfig.ts` monta os serviços frontend. As páginas manipulam o DOM e os clientes HTTP centralizam JSON, cookies e falhas HTTP.

`MatchFacade` coordena a retribuição de interesse e a gravação de match na mesma transação. O terminal reutiliza controllers, serviços e persistência.

```text
src/main/groovy/com/jpcode/  Backend: api, config, controller, service,
                           facade, dao/contratos, dao, dto, model,
                           database, exception, view e factory
src/test/groovy/com/jpcode/ Serviços, fachada, configuração, HTTP e integração
frontend/src/ts/            Páginas, componentes, serviços, clientes/DTOs HTTP,
                           estratégias de login, modelos e gráficos
frontend/src/html/          Páginas HTML
frontend/src/css/           Estilos
frontend/tests/            Testes Node e aceite de navegador
frontend/dist/             Saída gerada pelo build; não editar
queriesLinketinder.sql     DDL atual e exemplos SQL históricos
docs/specs/recrutador/     Especificação, plano e evidências da extensão
```

## Pré-requisitos

- JDK compatível com Gradle 9.3, Groovy 5 e Tomcat 11. O build não fixa uma toolchain Java; o ambiente registrado na validação usa Java 25.0.3.
- Node.js e npm. O manifesto não declara versão mínima de Node; o aceite documentado usa Node.js 24.18.0.
- PostgreSQL com banco, usuário e dados de referência preparados manualmente. O projeto não fixa a versão do servidor PostgreSQL.
- Servidor de arquivos estáticos para o frontend, na origem `http://127.0.0.1:5500`.
- Chrome/Chromium para o aceite de navegador, quando executado.

## Instalação e configuração

### Prepara??o do banco

1. Crie manualmente o banco PostgreSQL e o usuário com as permissões necessárias.
2. Em [queriesLinketinder.sql](queriesLinketinder.sql), aplique **somente os CREATE TABLE e índices da seção iniciada por `-- Versão Atualizada para Criação do projeto`, até antes de `-- Inserções`**, na ordem apresentada. Não execute o arquivo inteiro.
3. Cadastre países, estados, competências e áreas de recrutamento antes dos registros que os referenciam. Não há inicialização automática, migração ou seed configurado.
4. Não use os INSERTs legados como preparação da instalação: eles não representam as fixtures atuais nem o fluxo de criação de contas com senha protegida.

### Conexão do backend

`PostgresConnectionFactory` exige as três chaves abaixo. Variáveis de ambiente têm prioridade; o arquivo `.env` complementa as chaves ausentes e é lido em UTF-8 no diretório de execução. Execute o backend a partir da raiz para usar o `.env` local.

| Variável | Conteúdo |
| --- | --- |
| `DB_URL` | URL JDBC do banco PostgreSQL escolhido |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha desse usuário |

Não há banco ou credenciais padrão. Informe os valores reais apenas no ambiente ou no `.env` local ignorado pelo Git. Reinicie o backend após alterações. O arquivo aceita `CHAVE=valor`, aspas simples/duplas e comentários em linhas iniciadas por `#`.

As variáveis `LINKETINDER_TEST_*` são exclusivas dos testes e não configuram a aplicação.

### Dependências e compilação

Na raiz, em **PowerShell**, compile o backend sem executar os testes:

```powershell
.\gradlew.bat assemble
```

No frontend:

```powershell
cd frontend
npm ci
npx tsc --noEmit
npx gulp build
```

No Linux/macOS, use `./gradlew` em lugar de `.\gradlew.bat`. Os comandos npm e npx são os mesmos. No Windows, `npm.cmd` e `npx.cmd` podem ser usados quando a política do PowerShell bloquear os respectivos scripts `.ps1`.

## Como executar

### Iniciar o backend

Importe o projeto Gradle na IDE e execute uma das classes com o **classpath de runtime do projeto** e a raiz como diretório de trabalho:

| Entrada | Classe | Uso |
| --- | --- | --- |
| API HTTP | `com.jpcode.api.RestServer` | Inicia o Tomcat na porta 8080 para o frontend |
| Terminal | `com.jpcode.Main` | Abre o menu interativo de candidatos e empresas |

O Gradle não aplica o plugin `application` e não declara tarefa `run`. O JAR gerado não é configurado como aplicação autônoma com todas as dependências.

### Frontend

Após o build, sirva `frontend/dist/` por HTTP em `http://127.0.0.1:5500` e abra:

```text
http://127.0.0.1:5500/html/index.html
```

O `ApiClient` usa `http://127.0.0.1:8080` com `credentials: "include"`. Mantenha esse host no navegador e a origem frontend indicada; misturar `localhost` com `127.0.0.1` afeta os cookies e a política de origem. O build gera `dist/html`, `dist/css` e `dist/js` e não inicia servidor estático.

O login dos três perfis usa e-mail e senha, valida hash PBKDF2 no backend e cria sessão HTTP com cookie `HttpOnly` e `SameSite=Lax`. O LocalStorage guarda apenas `{ id, tipo }` na chave `sessao` para navegação. A autorização e a atividade da conta são revalidadas no servidor. Respostas `401`/`403` fazem a interface voltar ao login. Cadastro de candidato ou empresa não autentica automaticamente.

## Testes

### Verifica??es do backend

Na raiz, em PowerShell:

```powershell
.\gradlew.bat test
```

Para compilar, empacotar e executar as verificações do build:

```powershell
.\gradlew.bat build
```

Os testes usam JUnit Platform, Spock e mocks de persistência nos serviços. Há também testes de fachada, conexão, servlets e contratos HTTP em Tomcat, além das suítes com PostgreSQL real.

- Relatório de testes: `build/reports/tests/test/index.html`.
- Cobertura JaCoCo: `build/reports/jacoco/test/html/index.html`; XML também habilitado. A tarefa `test` aciona o relatório.

As integrações exigem `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` no **ambiente do processo**; o suporte de testes não lê `.env` automaticamente. O banco deve ser dedicado, chamado `linketinder_test` ou `linketinder_test_<sufixo>` aceito pelo suporte. A URL é validada sem parâmetros e cada conexão confere `current_database()` antes de usar schemas isolados. Não aponte testes para o banco de desenvolvimento.

Sem configuração de integração, os cenários dependentes do banco são ignorados por condição; testes de proteção do suporte podem executar sem banco. O aceite de navegador também exige `LINKETINDER_TEST_BROWSER`, com caminho para Chrome/Chromium instalado, e o frontend compilado. Consulte o [roteiro de validação](docs/specs/recrutador/validacao.md) para preparar e executar esse ambiente. Os resultados ali registrados são evidências históricas, não garantem uma nova execução local.

### Verifica??es do frontend

`npm test` continua sendo um placeholder que termina com erro. Existem suítes independentes em `frontend/tests/`; execute no diretório `frontend`:

```powershell
node --test tests/transporte-t12.test.cjs tests/entrada-t13.test.cjs tests/seletor-t14.test.cjs tests/candidato-t15.test.cjs tests/empresa-t16.test.cjs tests/recrutador-t17.test.cjs
```

Essas suítes exercitam transporte, login, seletor e jornadas com DOM/fetch controlados. Não substituem o aceite integrado de navegador, Tomcat e PostgreSQL descrito no roteiro.

## API / Uso

A API recebe e retorna JSON com `JsonSlurper` e `JsonOutput`. Erros usam o formato `{ "erro": "..." }`. As rotas são registradas em [ApiServletConfig](src/main/groovy/com/jpcode/config/ApiServletConfig.groovy), com regras de método, sessão e corpo nas servlets correspondentes.

O CORS permite `http://127.0.0.1:5500`, credenciais, métodos `GET`, `POST`, `PUT` e o cabeçalho `Content-Type`. Preflight válido recebe `204`. Mutações exigem essa origem e `application/json`, inclusive quando chamadas fora do navegador.

| Método | Rota | Uso e autorização |
| --- | --- | --- |
| `POST` | `/login/candidato`, `/login/empresa`, `/login/recrutador` | Autentica e inicia sessão HTTP; credenciais inválidas recebem `401` |
| `POST` | `/candidatos`, `/empresas` | Cadastro público dos dois perfis |
| `GET`, `PUT` | `/candidatos/{id}`, `/empresas/{id}` | Consulta/atualização do próprio perfil |
| `GET` | `/candidatos`, `/empresas` | Listagens identificáveis bloqueadas com `403` |
| `GET` | `/vagas`, `/vagas/{id}` | Descoberta anônima para candidato autenticado |
| `GET` | `/vagas/empresa/{idEmpresa}` | Vagas internas da própria empresa e autoria histórica |
| `GET` | `/vagas/recrutador/{idRecrutador}` | Vagas do próprio recrutador ativo |
| `POST` | `/vagas` | Empresa autenticada cria vaga; `201` |
| `PUT` | `/vagas/{id}` | Empresa proprietária atualiza vaga |
| `GET` | `/competencias` | Catálogo de nomes (`string[]`) para seleção |
| `GET` | `/referencias/paises`, `/referencias/estados` | Referências dos cadastros |
| `GET` | `/areas-recrutamento` | Áreas persistidas, com `id` e `nome` |
| `POST` | `/empresas/{idEmpresa}/recrutadores` | Própria empresa ativa cadastra recrutador; `201` |
| `GET` | `/recrutadores/{id}` | Perfil do próprio recrutador ativo, sem credenciais |
| `POST` | `/recrutadores/{id}/vagas` | Autor autenticado cria vaga vinculada à sua empresa; `201` |
| `POST` | `/curtidas/vagas` | Corpo `{idVaga}`; candidato definido pela sessão; `204` inclusive na repetição |
| `POST` | `/curtidas/candidatos` | Corpo `{idCandidato, idVaga}`; retribuição pela empresa ou autor ativo; match atômico; `204` |
| `GET` | `/curtidas/vagas/candidato/{idCandidato}` | Vagas anônimas curtidas pelo próprio candidato |
| `GET` | `/curtidas/candidatos/vaga/{idVaga}` | Interessados anônimos; empresa proprietária ou autor ativo |
| `GET` | `/curtidas/candidatos/vaga/{idVaga}/retribuidos` | IDs retribuídos; mesmo escopo de empresa/autoria |
| `GET` | `/matches/candidato/{idCandidato}` | Matches do próprio candidato |
| `GET` | `/matches/empresa/{idEmpresa}` | Matches da própria empresa |
| `GET` | `/matches/recrutador/{idRecrutador}` | Matches das vagas do próprio autor ativo |

Os IDs dos caminhos não concedem autorização: a sessão determina o ator. As consultas antigas `/verificar` e a listagem empresarial global de curtidas não integram o contrato atual.

## Banco de dados

O schema atual em [queriesLinketinder.sql](queriesLinketinder.sql) contém:

| Grupo | Tabelas e relações |
| --- | --- |
| Referências | `pais`, `estados`, `competencias`, `areas_recrutamento` |
| Contas | `candidatos`, `empresas`, `recrutadores`; recrutador vinculado a empresa e área |
| Vagas | `vagas`, com empresa obrigatória e recrutador autor opcional |
| Competências N:N | `candidatos_competencias`, `vagas_competencias` |
| Interesse inicial | `vagas_curtidas_candidato`, por candidato e vaga |
| Retribuição | `candidatos_curtidos_empresa`, por candidato e vaga, com empresa e recrutador ator opcional |
| Match | `match`, relacionando candidato, empresa e vaga |

A FK composta da vaga exige que seu autor pertença à empresa proprietária. As restrições de retribuição e match vinculam interesse inicial, vaga e empresa corretos. Competências, curtidas e matches usam restrições de unicidade; os INSERTs pertinentes tratam repetição com `ON CONFLICT DO NOTHING`.

O MER/DER foi desenvolvido no **dbdiagram.io**. A imagem abaixo é **histórica**: inclui formação e seus relacionamentos, ausentes do DDL atual, e não representa a extensão de recrutadores. O SQL atual é a referência de instalação.

<img width="1148" height="1263" alt="MER/DER histórico do Linketinder, anterior à extensão de recrutadores" src="https://github.com/user-attachments/assets/51feb32e-5ac6-4395-8134-0a08f6282f30" />

## Regras de negócio

1. O candidato descobre vagas somente por `id`, `descricao` e `competencias`, conforme `VagaAnonimaDTO`.
2. Curtir uma vaga registra o interesse inicial e a inclui entre as vagas curtidas. Essa operação não gera match.
3. A empresa proprietária ou o recrutador autor ativo consulta interessados por vaga. `CandidatoAnonimoDTO` expõe somente `id`, `descricao` e `competencias`.
4. A retribuição exige interesse prévio naquela vaga. `MatchFacade` valida ator, propriedade, autoria e atividade e grava retribuição e match na mesma transação, com rollback em caso de falha.
5. Existe um único match por candidato e vaga, mesmo com repetições por empresa e recrutador. Competências não geram cálculo automático de compatibilidade ou match.
6. Somente após match válido os contratos específicos de match liberam identificação e contato, documentos, descrições, localização, idade, competências e dados da vaga. O candidato recebe nome e área do recrutador autor quando houver; senhas não fazem parte da resposta.

A empresa mantém acesso às próprias vagas, interessados e matches após a inatividade do autor. Autoria histórica da vaga e recrutador que executou a retribuição são informações distintas.

Candidatos e empresas usam desativação lógica. Vagas podem ser excluídas fisicamente pelo fluxo backend/terminal: o DAO remove match e retribuição na mesma transação antes de excluir a vaga, respeitando FKs e cascades. A desativação de empresa não apaga suas vagas.

CPF é único por perfil ativo; CNPJ, por empresa ativa. E-mail é exclusivo entre contas ativas dos três perfis, com validação transacional no backend e índices de unicidade por tabela.

Os cadastros reutilizam referências persistidas. O seletor frontend envia competências selecionadas do catálogo; texto livre não selecionado não é enviado. No cadastro de vagas, o frontend adapta `localizacao` para `local`; campos locais como `tipo` não são persistidos. A idade do candidato é calculada pelo frontend no cadastro a partir da data de nascimento e enviada como `idade`; `data_nascimento` também é armazenada.

## Autor

Desenvolvido por **João Pedro Vaz** durante o **Acelera ZG**.
