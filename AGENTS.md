# Orientações para agentes — Linketinder

## 1. Visão geral do projeto

Linketinder é uma plataforma de recrutamento que aproxima candidatos e empresas por vagas, competências, curtidas e matches. O anonimato até o match é uma regra central do negócio.

Antes de trabalhar, confira a branch e leia os arquivos envolvidos. A integração HTTP descrita neste documento tem como referência `feat/frontend-backend-integration`; aplique as orientações aos componentes existentes no checkout, sem presumir que arquivos de outra branch estejam presentes.

## 2. Tecnologias

- Backend: Groovy, `groovy-json`, Jakarta Servlet e Apache Tomcat embarcado. Sem Spring, ORM ou contêiner de injeção de dependências.
- Persistência: PostgreSQL e JDBC, com SQL nos DAOs e conexões fornecidas por `ConnectionFactory`.
- Frontend: TypeScript estrito, HTML/CSS, manipulação direta do DOM e Chart.js. Gulp e Webpack compilam e empacotam a interface.
- Build e testes: Gradle Wrapper, Spock, JUnit Platform e JaCoCo. Consulte `build.gradle` e `frontend/package.json` para as versões efetivamente declaradas; os JARs em `lib/` não substituem essas declarações.

## 3. Estrutura do repositório

- `src/main/groovy/com/jpcode/`: código do backend.
  - `api/servlet/`: entrada HTTP, leitura de requisições e respostas JSON.
  - `config/`: montagem explícita de DAOs, serviços, controllers, servlets e Tomcat.
  - `controller/`, `service/` e `facade/`: encaminhamento, operações de negócio e coordenação de operações.
  - `dao/contratos/`: interfaces de persistência; demais pacotes de `dao/`: implementações JDBC.
  - `model/`, `dto/` e `exception/`: entidades, contratos de transferência e exceções.
  - `database/`: conexões; `view/` e `factory/`: interface e composição dos menus de terminal.
- `src/test/groovy/com/jpcode/service/`: testes dos serviços.
- `frontend/src/ts/`: páginas, modelos, serviços, estratégias de login e configuração; na integração, `api/clientes/` e `api/dto/` contêm comunicação HTTP e contratos.
- `frontend/src/html/` e `frontend/src/css/`: páginas e estilos; `frontend/dist/`: saída gerada.
- `queriesLinketinder.sql`: SQL do banco; `README.md`: contexto geral do projeto.

## 4. Arquitetura e fluxo de dados

O backend HTTP segue `Servlet → Controller → Service → contrato de repositório → DAO → PostgreSQL`. A composição ocorre por construtores nas classes `DaoConfig`, `ServiceConfig` e `ControllerConfig`; `ApiServletConfig` registra as rotas. Preserve essa montagem manual.

`api/RestServer.groovy` inicia o Tomcat na porta 8080. `Main.groovy` inicia a interface de terminal, que reutiliza controllers e serviços. `MatchFacade` coordena a curtida empresarial e o registro do match.

No frontend integrado, o fluxo é `Página → Service → ApiClient → fetch → Servlet`. As páginas manipulam o DOM; serviços delegam a comunicação aos clientes HTTP e fazem adaptações de dados. As estratégias de login realizam sua própria chamada HTTP. `App.ts` seleciona a página pelo caminho e `ServiceConfig.ts` compõe os serviços.

Confira a configuração utilizada antes de alterar persistência ou composição: classes de repositório local também existem no histórico e nos checkouts do projeto. Não migre fluxos entre armazenamento local e HTTP sem solicitação.

## 5. Modelo de domínio

- `Candidato` e `Empresa` estendem `Pessoa` no backend; seus dados são persistidos em `candidatos` e `empresas`.
- Uma empresa possui várias vagas; cada `Vaga` pertence a uma empresa por `vagas.id_empresa`.
- `Competencia` é uma referência persistida. Candidatos e competências se relacionam por `candidatos_competencias`; vagas e competências, por `vagas_competencias`, ambos N:N.
- A curtida inicial do candidato é armazenada em `vagas_curtidas_candidato`, com `id_candidato` e `id_vaga`.
- A declaração de interesse da empresa é armazenada em `candidatos_curtidos_empresa`, com `id_empresa` e `id_candidato`.
- O match é persistido em `match`, com `id_candidato`, `id_empresa` e `id_vaga`. Curtidas e matches possuem DAOs e contratos próprios; não presuma entidades backend `Curtida` ou `Match`.

Modelos frontend, entidades backend e DTOs têm responsabilidades diferentes. Por exemplo, o serviço frontend de vagas converte `localizacao` para `local` e competências em nomes para o cadastro HTTP. As entidades backend usam IDs de referências; DTOs de consulta podem trazer objetos de país, estado e competência. Não copie campos entre essas estruturas automaticamente.

## 6. Regras centrais de negócio

1. O candidato consulta vagas anônimas e demonstra interesse em uma delas pela descrição e pelas competências.
2. A curtida é persistida e a vaga aparece na área de vagas curtidas do candidato.
3. A empresa responsável consulta os candidatos anônimos interessados em suas vagas.
4. Ao curtir um desses candidatos, a empresa declara interesse nele. Ela completa a reciprocidade e gera o match, permitindo a identificação entre as partes.

A vaga é o contexto de descoberta e do interesse inicial; a curtida empresarial relaciona empresa e candidato. Preserve os identificadores usados por cada operação e tabela.

`MatchController.curtirCandidato` encaminha a operação a `MatchFacade`, que chama `EmpresaService.curtirCandidato` e `MatchService.salvar`. As consultas de matches são feitas por candidato ou empresa a partir dos registros persistidos. Não introduza cálculo de compatibilidade por competências ou geração de match na curtida inicial do candidato.

As combinações de curtidas e matches possuem restrições de unicidade; os respectivos INSERTs usam `ON CONFLICT DO NOTHING`. Preserve esse tratamento de repetição. Candidatos e empresas usam desativação lógica; vagas podem ser excluídas fisicamente. Respeite as chaves estrangeiras e os cascades definidos no SQL atual.

## 7. Anonimato e visibilidade dos dados

**ANONIMATO É UMA INVARIANTE CENTRAL DO NEGÓCIO. Nenhuma funcionalidade, endpoint, DTO, consulta, componente frontend ou refatoração pode expor informações que identifiquem candidato ou empresa à outra parte antes de um match válido. Toda mudança de visibilidade deve preservar essa regra no backend.**

- Para vagas disponíveis e curtidas apresentadas anonimamente ao candidato, preserve `VagaAnonimaDTO`: somente `id`, `descricao` e `competencias`. Não acrescente nome da vaga, nome/ID/localização da empresa ou outros identificadores empresariais.
- Para candidatos anônimos apresentados à empresa, siga `CandidatoAnonimoDTO`: `id`, `descricao` e `competencias`. Na integração, preserve os contratos frontend correspondentes `VagaAnonimaResponseDTO` e `CandidatoAnonimoResponseDTO`.
- Nunca substitua um DTO anônimo por uma entidade completa ou suponha que ocultar campos no DOM seja suficiente. A presença de um campo no banco não autoriza sua exposição.
- Após o match, utilize os contratos específicos de match. `MatchEncontradoDTO` e, na integração, `MatchResponseDTO` incluem identificação e contato das partes, CPF/CNPJ, descrições, localização por país/estado, idade do candidato, competências e dados da vaga. Isso não autoriza retornar outros campos da entidade, especialmente senhas.
- Não use contratos de perfil próprio ou de vagas da própria empresa como contratos de descoberta anônima. Qualquer dúvida ou ampliação dos campos permitidos exige esclarecimento com o responsável antes da implementação.

## 8. Contratos da API e integração frontend

As servlets usam `JsonSlurper` para entrada e `JsonOutput` para saída. Preserve métodos, nomes de campos, códigos HTTP e DTOs existentes. Consulte os registros de `ApiServletConfig` e a servlet responsável antes de criar ou consumir uma rota.

Na branch de integração, os clientes usam `http://localhost:8080`. Os principais contratos são:

| Fluxo | Rotas relevantes |
| --- | --- |
| Cadastro e perfil | `/candidatos`, `/empresas` e consultas por ID |
| Vagas | `/vagas` e `/vagas/empresa/{idEmpresa}` |
| Referências | `/competencias`, `/referencias/paises`, `/referencias/estados` |
| Login | `POST /login/candidato`, `POST /login/empresa` |
| Curtir | `POST /curtidas/vagas`, `POST /curtidas/candidatos` |
| Consultar interesse | `/curtidas/vagas/candidato/{idCandidato}`, `/curtidas/candidatos/vaga/{idVaga}` |
| Matches | `/matches/candidato/{idCandidato}`, `/matches/empresa/{idEmpresa}` |

O cadastro de candidato e a criação de vaga reutilizam a consulta de competências. Preserve essa fonte de referência no fluxo integrado.

Os API Clients concentram `fetch`, JSON e tratamento de falhas HTTP; os serviços preservam os contratos assíncronos com `Promise`/`async`/`await`. O login usa Strategy por perfil, envia e-mail/senha ao backend e grava `{ id, tipo }` em `localStorage` na chave `sessao`. No fluxo integrado, use esse armazenamento para sessão, mantendo os dados de negócio no backend. Não presuma que esse objeto seja um token ou substitua validações no servidor.

## 9. Preparação do banco

A referência de schema é **somente** a seção de `queriesLinketinder.sql` iniciada pelo marcador exato `-- Versão Atualizada para Criação do projeto`. Ignore os schemas anteriores; não execute o arquivo inteiro.

O banco e os dados dos campos de seleção são preparados manualmente. Em uma instalação nova, crie o banco PostgreSQL e o usuário, aplique os `CREATE TABLE` da seção atual na ordem de dependência e cadastre países, estados e competências antes dos registros que os referenciam. Confira os IDs exigidos pelos exemplos antes de usar suas inserções.

`database/PostgresConnectionFactory.groovy` usa `DB_URL`, `DB_USER` e `DB_PASSWORD`, com prioridade para variáveis de ambiente e leitura complementar do `.env` no diretório de execução (raiz do projeto). Não há fallback de banco ou credenciais. Confira a configuração necessária sem reproduzir credenciais em documentação, logs ou respostas. As variáveis `LINKETINDER_TEST_*` são exclusivas dos testes de integração. Não presuma migrações ou inicialização automática. Não modifique schema ou dados como efeito colateral de outra tarefa.

## 10. Build, execução e testes

Pré-requisitos: JDK compatível com Gradle 9.3/Tomcat 11, Node.js/npm e PostgreSQL para fluxos persistidos. Use as dependências declaradas pelo projeto.

Na raiz, em PowerShell:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

Os testes de serviços usam Spock e mocks de persistência. `test` aciona o relatório JaCoCo, com saída em `build/reports/jacoco/test/`; o relatório dos testes fica em `build/reports/tests/test/`. Esses testes não substituem verificação dos endpoints ou do banco.

Para iniciar o backend, execute a classe `com.jpcode.api.RestServer` com o classpath de runtime do projeto, por exemplo pela IDE. Para o terminal, execute `com.jpcode.Main`. O Gradle não declara tarefa `run`.

No diretório `frontend`:

```powershell
npm ci
npx tsc --noEmit
npx gulp build
```

Sirva `frontend/dist/` como conteúdo estático e abra `html/index.html`. Na integração, o `CorsFilter` permite a origem `http://127.0.0.1:5500`; confira a origem usada pelo servidor frontend. Não edite os arquivos gerados em `dist/`.

Não há suíte de testes frontend configurada: `npm test` é um placeholder que termina com erro. Execute apenas as verificações pertinentes e informe exatamente o que foi executado e seus resultados.

## 11. Convenções de código

- **Naming convention:** When operations depend on different authenticated actors or authorization contexts, method names must explicitly communicate the responsible actor. Avoid ambiguous overloads that conceal security-relevant differences. Prefer descriptive names over signatures that require inspecting parameters to understand authorization context.
- Preserve nomes de domínio em português, classes em PascalCase e métodos/variáveis em camelCase, seguindo o arquivo existente.
- Respeite os pacotes e sufixos estabelecidos: `DAO`, `Repository`, `Service`, `Controller`, `Servlet`, `DTO`, `ApiClient`, `Page` e `Strategy`.
- Mantenha SQL e mapeamento JDBC nos DAOs, regras/orquestração nos serviços e fachadas, tratamento HTTP nas servlets e apresentação nas páginas/menus.
- Reutilize contratos, configuração por construtor, exceções, DTOs e padrões de fechamento de recursos já adotados. Use parâmetros em `PreparedStatement`.
- Preserve a tipagem estrita do TypeScript e as adaptações explícitas entre modelos e DTOs. Não introduza frameworks ou dependências sem necessidade aprovada.

## 12. Instruções e restrições para agentes

- Leia o código relevante e trace produtores e consumidores antes de propor uma alteração; confira a branch e o schema atual.
- Explique a mudança proposta antes de implementá-la. Obtenha aprovação antes de alterar comportamento existente ou arquitetura; considere a autorização já dada no pedido em andamento.
- Faça mudanças incrementais e focadas. Preserve a separação frontend/backend, a composição manual e os contratos existentes.
- Não invente arquivos, métodos, classes, endpoints, camadas ou regras de negócio. Não trate documentação histórica como substituta do código.
- Não enfraqueça o anonimato, amplie DTOs anônimos nem exponha entidades completas para contornar contratos de visibilidade.
- Não faça refatorações alheias ao pedido, atualizações de dependências ou mudanças no banco sem autorização correspondente.
- Em ambiguidades de domínio ou visibilidade, pergunte antes de implementar; não resolva silenciosamente por suposição.
- Execute testes e verificações relevantes quando possível. Nunca afirme que passaram sem executá-los; informe limitações concretas.
- Postman CLI está instalado e pode ser utilizado em trabalho de engenharia de API quando pertinente à tarefa.
