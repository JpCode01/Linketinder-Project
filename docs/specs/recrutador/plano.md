# Plano de implementação — Recrutador

Status: T01, T02, T03, T04, T05, T06, T07, T08, T09, T10, T11 e T12 concluídas e validadas nos respectivos escopos; demais tarefas pendentes. Implementações de T01, T02, T03 e T04 autorizadas explicitamente pelo responsável em 07/11/2026; T05, T06, T08, T09, T10, T11 e T12 autorizadas explicitamente em 08/11/2026, sem execução de commits pelos agentes. Evidências de T07 registradas na respectiva tarefa.

Branch verificada: `feature/recruiter`. Data: 07/11/2026.

## 1. Objetivo e escopo

Implementar incrementalmente a [especificação aprovada](spec.md), mantendo sua autoridade sobre requisitos e decisões de negócio. A sequência preliminar foi aprovada pelo responsável no diálogo. Este documento detalha essa sequência; sua criação não autoriza código, alterações de banco ou commits.

Abranger os fluxos obrigatórios das seções 2 a 10 da especificação. Após a importação autorizada do frontend integrado de master, reaproveitar essa base e adaptá-la ao backend de T01–T11, sem recomeçar ou modificar as entregas backend. Não incluir os endpoints opcionais de edição de perfil e troca de senha do recrutador: CA-26 será registrado como condicional não aplicável. As atualizações já existentes de candidato/empresa continuam sujeitas às regras de hash, unicidade e inatividade.

Não adaptar menus de terminal, migrar credenciais ou dados de teste, criar administração de vagas/reatribuição, identidade central, frameworks ou novas dependências. Manter as assinaturas utilizadas pelo terminal quando possível, sem preservar operações que burlem a consistência exigida. Alterações internas compartilhadas precisam continuar compilando com os consumidores existentes.

Após o aceite de T18, executar T19 como última tarefa: analisar e remover somente código comprovadamente obsoleto da transição para os fluxos aprovados. A inclusão desta tarefa foi autorizada pelo responsável em 08/11/2026; não autoriza sua execução antecipada, retirada do terminal ou alteração da especificação. A limpeza deve preservar as entregas anteriores e repetir as validações afetadas após as remoções.

## 2. Arquitetura atual e impacto técnico

### Evidências da análise inicial (histórico anterior a T01–T11 e à importação)

| Componente existente | Situação e impacto |
| --- | --- |
| `src/main/groovy/com/jpcode/config/ApiServletConfig.groovy` | Registra somente competências, candidatos, empresas e vagas. Login, referências, curtidas e matches precisam de entrada HTTP; não existem neste checkout as rotas da branch de integração. |
| `config/DaoConfig.groovy`, `config/ServiceConfig.groovy`, `config/ControllerConfig.groovy`, sob `src/main/groovy/com/jpcode/` | Composição manual por construtores; `ControllerConfig` instancia `MatchFacade`. Estender essa composição. |
| `database/ConnectionFactory.groovy` | Expõe `getConnection()`. DAOs abrem e fecham suas conexões; as chamadas atuais de retribuição e match não compartilham uma transação. |
| `service/CandidatoService.groovy`, `service/EmpresaService.groovy`, `dao/contratos/UsuarioRepository.groovy` | Login usa `buscarPorEmailESenha`; DAOs comparam senha no SQL. Cadastro e atualização encaminham senha sem derivação. Exceções de login incluem a senha. |
| `facade/MatchFacade.groovy`, `service/MatchService.groovy` | A fachada registra curtida empresarial global e depois salva match. Não garante atomicidade, interesse prévio ou propriedade. `MatchController.salvar` também encaminha gravação direta e precisa deixar de permitir contorno dessas regras. |
| `dao/relacionamento/CandidatoCurtirDAO.groovy` | Guarda interesse em `vagas_curtidas_candidato`, usa `ON CONFLICT` e retorna DTOs anônimos. A consulta de interessados recebe apenas ID da vaga e precisa de validação de escopo. |
| `dao/relacionamento/EmpresaCurtirDAO.groovy`, `dao/match/MatchDAO.groovy` | Retribuição tem apenas empresa/candidato. Match possui empresa/candidato/vaga. A consulta empresarial de matches chama `competenciasCandidatoRepository.buscar`, enquanto o contrato declara `buscarPorId`: corrigir esse produtor/consumidor no trabalho de matches. |
| `api/servlet/VagaServlet.groovy`, `CandidatoServlet.groovy`, `EmpresaServlet.groovy` | Há consultas por ID e listagens identificáveis; vaga por ID é serializada como entidade. Precisam de contratos e contexto do observador, não apenas ocultação no frontend. |
| `frontend/src/ts/config/RepositoryConfig.ts`, `config/ServiceConfig.ts` | Serviços usam repositórios locais síncronos. Não há diretório de clientes HTTP/DTOs. Todos os consumidores afetados precisam de adaptação assíncrona coerente. |
| `frontend/src/ts/strategy/login/` | `LoginStrategy.login(email): void` e estratégias consultam armazenamento local, sem senha. Preservar Strategy, alterando entrada e retorno. |
| `frontend/src/ts/models/Competencia.ts`, formulários e modelos | Enum fixo tecnológico; formulários de candidato e vaga usam checkboxes. Modelos não equivalem aos DTOs backend. Cadastro de candidato precisa também capturar sobrenome, nascimento, país e senha exigidos pelo backend; empresa precisa capturar senha. |
| `frontend/src/ts/services/RelatorioCompetenciasService.ts` | Gráfico consulta perfis completos por candidato. Recalcular a mesma contagem por interesse/vaga usando competências dos interessados anônimos. |
| `queriesLinketinder.sql`, seção atual | CPF/CNPJ/e-mail têm unicidade permanente por tabela. Não existem recrutadores, áreas ou autoria da vaga. `candidatos_que_curtiram_vaga` não é a tabela usada pelo DAO de interesse; não criar uma segunda fonte de verdade. |
| `build.gradle`, `src/test/groovy/com/jpcode/service/` | Groovy 5.0.8, Spock 2.4, JUnit Platform, JDBC PostgreSQL, Jakarta Servlet/Tomcat e JaCoCo. Existem testes de serviços com mocks; não há suíte configurada de integração PostgreSQL/HTTP. |
| `frontend/package.json`, `tsconfig.json`, `gulpfile.js` | TypeScript estrito, Gulp/Webpack, Chart.js. `npm test` é placeholder com erro; não há suíte frontend. |
| `CHANGELOG.md` | Contém histórico de integração HTTP que não corresponde à árvore atual. Usar como histórico, sem presumir código ausente; registrar futuramente somente entregas verificadas. |

Os caminhos backend abreviados na tabela acima são relativos a `src/main/groovy/com/jpcode/`. O Tomcat é iniciado por `com.jpcode.api.RestServer`, porta 8080; `TomcatConfig` não registra filtro CORS neste checkout. Não há tarefa Gradle `run`.

### Base atual após a importação do frontend (08/11/2026)

Por determinação do responsável, a implementação anterior de T12 foi desconsiderada. T12 volta a estar **pendente**, sem aproveitar seu status, arquivos exclusivos ou resultados de testes como evidência da nova entrega. A base frontend é agora o snapshot integral de `origin/master:frontend`, commit `da273790fb0c10235981aa78fabe29b9a28310cf`, importado somente para o diretório de trabalho da `feature/recruiter`. Não houve merge, alteração de master ou importação de backend. T01–T11 e suas evidências permanecem preservadas.

A tabela anterior descreve a **análise inicial histórica**, não a árvore frontend atual. O backend HTTP de T11 passa a ser o contrato vigente para adaptar o frontend importado; não se reimplementam T01–T11 para reproduzir o backend antigo de master.

| Componente atual | Situação comprovada e impacto no plano |
| --- | --- |
| `F/ts/config/ServiceConfig.ts`, `F/ts/api/clientes/`, `F/ts/services/` | Já existem clientes HTTP e serviços assíncronos para candidato, empresa, vagas, curtidas, matches, competências e referências. Reutilizar a composição; não construir outra integração paralela. `RepositoryConfig.ts` não existe nesse snapshot; repositórios/factories históricos remanescentes só serão avaliados em T19. |
| `F/html/{index,cadastro-candidato,cadastro-empresa}.html` e páginas correspondentes | Já possuem senha, sobrenome/nascimento no candidato, referências e submissão HTTP. T13 adapta o comportamento existente e acrescenta login de recrutador, sem recriar os formulários. Cadastros atuais tentam login automático, divergindo do encaminhamento ao login definido neste plano. |
| Clientes HTTP e estratégias de login | Usam `localhost`, não enviam `credentials: "include"` e repetem tratamento de respostas. T12 deve unificar host/cookie e erros, inclusive os `fetch` diretos das páginas/estratégias, conforme o servidor de T11. |
| Curtidas e páginas consumidoras | Enviam ID do ator no corpo e chamam `/curtidas/vagas/verificar` e `/curtidas/candidatos/verificar`, ausentes em T11. T12 adapta corpos e consumidores sem criar essas rotas no backend, nem recriar curtida global. |
| `CompetenciaApiClient`, formulários e `models/Competencia.ts` | O enum já foi substituído por classe `{id,nome}`. Porém o catálogo é tipado como objetos e os formulários acessam ID/nome, enquanto o backend vigente retorna `string[]`. T12 corrige esse contrato e os consumidores mínimos; T14 adiciona a pesquisa/seleção reutilizável, sem inventar IDs. |
| DTOs anônimos, internos, perfis e match | Os anônimos já têm ID/descrição/competências. Ampliar somente vaga interna com autoria e match com nome/área anuláveis do recrutador; manter perfil próprio separado. Conferir a serialização real de `LocalDate` antes de tipar a resposta como string. |
| `CandidatoMatch.ts`, `EmpresaMatch.ts` e gráfico | Componentes de matches já existem; o gráfico já usa competências de interessados anônimos por vaga. T15/T16 estendem a apresentação existente e validam contagem, sem recriar essa lógica. `EmpresaCandidatoCurtido.ts` contém busca de perfil alheio; rastrear seus consumidores, impedir seu uso no caminho integrado e usar matches autorizados, sem reabrir perfil público. |
| Login, perfil e telas do recrutador | Ausentes no frontend importado. Acrescentar somente os contratos/serviços em T12, entrada em T13, cadastro empresarial em T16 e jornada em T17. Backend correspondente já entregue em T06–T11. |

**Verificação da importação:** 92 arquivos conferidos byte a byte com o archive do commit acima; removidos do conjunto frontend 17 arquivos ausentes no snapshot, após backup. `npx.cmd tsc --noEmit` e `npx.cmd gulp build` passaram; o build emitiu apenas o aviso não impeditivo `DEP0180`. `git diff --check` aponta sete ocorrências de whitespace herdadas do snapshot de master, preservadas para manter a cópia exata; a documentação revisada não introduz erros de whitespace. Os testes exclusivos da T12 descartada não fazem parte desta base e não foram contados. Compilação não valida cookie/CORS, rotas ou integração persistida: as incompatibilidades da tabela continuam pendentes em T12. Nenhuma escrita/conexão de banco foi realizada.

Backup local ignorado pelo Git: `build/frontend-antes-importacao-20261108T211819903785Z/`, contendo fontes anteriores, documentação, diff, snapshot importado e manifesto de hashes/referências. Os 140 arquivos protegidos (backend/testes, SQL, build e especificação) e os commits de `HEAD`, `master` e `origin/master` permaneceram intactos. Detalhes em [diagnostico-integracao.md](diagnostico-integracao.md).

## 3. Decisões técnicas e dependências

As escolhas abaixo são propostas técnicas deste plano para aprovação final, não novas regras de negócio.

### 3.1 Banco e referências

- Não utilizar triggers no banco de dados. As demais ferramentas e recursos de banco previstos neste plano permanecem autorizados.
- Trabalhar somente na seção iniciada pelo marcador exato `-- Versão Atualizada para Criação do projeto`. Durante a implementação, separar DDL e preparação manual de referências dos exemplos legados; nunca executar o arquivo inteiro.
- Propor `areas_recrutamento(id, nome)` e `recrutadores` com os campos lógicos da seção 5, `ativo NOT NULL DEFAULT TRUE` e FKs obrigatórias. Não herdar `Pessoa` apenas por reaproveitamento: ela exige endereço/país não previstos para recrutador. Criar entidade específica.
- Acrescentar `vagas.id_recrutador` anulável; garantir a relação `(id_recrutador, id_empresa)` com chave composta referenciando `(id, id_empresa)` de recrutadores. Manter FK empresarial obrigatória. A referência anulável permite vaga direta sem enfraquecer a propriedade.
- Trocar unicidades permanentes de CPF/CNPJ/e-mail por índices únicos parciais `WHERE ativo = true` nos respectivos perfis. Definir `ativo` inicial verdadeiro no banco e serviço. Não presumir normalização de CPF/CNPJ ou e-mail insensível a maiúsculas: conservar a representação dos contratos; a feature não define novas equivalências de identificadores.
- Áreas, países, estados e competências serão preparados manualmente como referências. Fixtures novas de teste podem usar áreas e competências de RH/administração sem converter registros legados.

### 3.2 E-mail concorrente entre tabelas

Adotar a alternativa da seção 5 de serialização transacional efetiva na persistência, sem tabela central de identidade. Antes de qualquer gravação de conta, abrir transação JDBC em `READ COMMITTED` e obter locks de tabela `SHARE ROW EXCLUSIVE`, sempre na ordem `candidatos`, `empresas`, `recrutadores`. Depois dos locks, consultar e-mails ativos nos três perfis, excluindo somente a própria linha/perfil em atualização; validar CPF/CNPJ e executar a escrita na mesma conexão. Confirmar ou reverter e liberar todos os recursos.

Aplicar o protocolo em **todos** os métodos de `salvar`, `atualizarDados` e `desativar` dos três DAOs; não basta fazê-lo nas servlets. A consulta posterior ao lock vê a gravação já confirmada do concorrente em `READ COMMITTED`. Índices parciais continuam como defesa intratabela. Centralizar apenas o SQL/protocolo comum em um colaborador de persistência, sem extrair identidade ou regras de HTTP para ele. Derivar senha antes da região com locks para não bloquear cadastros durante o hash.

Essa solução serializa gravações de contas e é adequada ao porte atual; leituras comuns continuam possíveis. Escritas administrativas externas não participam da validação de aplicação e não devem ser tratadas como um caminho suportado de cadastro. Testar duas conexões efetivamente concorrentes, falha/rollback e ausência de caminhos de gravação sem protocolo. A semântica de conflito dos locks consta na [documentação PostgreSQL](https://www.postgresql.org/docs/current/explicit-locking.html).

### 3.3 Retribuição, match e transação

- Manter `vagas_curtidas_candidato` como interesse único candidato–vaga, sem duplicação por destinatário.
- Evoluir `candidatos_curtidos_empresa` para conter `id_vaga` e `id_recrutador_ator` opcional. O segundo campo identifica quem retribuiu quando foi recrutador; **não** representa autoria da vaga. Retribuição empresarial deixa-o nulo. O primeiro ator confirmado pode permanecer na repetição idempotente; não há requisito de log de todos os atores.
- Tornar única a retribuição por `(id_candidato, id_vaga)` e referenciar o interesse por FK composta; garantir empresa proprietária via FK `(id_vaga, id_empresa)` para `vagas(id, id_empresa)`, cuja chave única de suporte será criada. Se houver recrutador ator, validar atividade, autoria e empresa no serviço/transação, além da FK de existência/vínculo empresarial.
- Tornar único o match por `(id_candidato, id_vaga)` e garantir FKs para a vaga/proprietária e para a retribuição correspondente `(id_candidato, id_empresa, id_vaga)`, com chave única de suporte nesta última. Assim, gravação isolada não produz match sem reciprocidade. Não persistir recrutador autor novamente no match: obtê-lo por JOIN da vaga e área, sem filtro de atividade nessa associação histórica.
- `MatchFacade` coordena uma única transação para validar ator/vaga/interesse, registrar retribuição e salvar match. Acrescentar operações nos contratos/DAOs que recebam a conexão da transação e não a fechem; métodos que abrem conexões continuam responsáveis por fechá-las. SQL fica nos DAOs; não abrir outra conexão entre os dois INSERTs.
- Bloquear as linhas relevantes de ator, vaga e interesse durante a operação, em ordem documentada e igual nos dois caminhos. Revalidar atividade/propriedade dentro da transação. Usar `ON CONFLICT DO NOTHING` com os novos alvos e confirmar que a repetição corresponde ao mesmo contexto. Qualquer falha reverte ambas as gravações.
- Não alterar cascades existentes nem criar exclusão física de recrutadores. As novas FKs devem ser revisadas contra `VagaDAO.deletar`: quando necessário, remover match e retribuição dependentes antes da vaga, na mesma transação, preservando o resultado da exclusão física já existente. Não criar tela ou nova operação de remoção.

### 3.4 Senhas e contexto HTTP

Propor `PBKDF2WithHmacSHA256`, 600.000 iterações, salt aleatório de 16 bytes por senha e chave derivada de 256 bits. Guardar versão/algoritmo/iterações/salt/hash em representação textual no campo `senha`. Usar `SecretKeyFactory`, `PBEKeySpec`, `SecureRandom` e comparação dos bytes por `MessageDigest.isEqual`; limpar buffers temporários quando aplicável. Verificar parâmetros codificados sem aceitar formato inválido ou custo arbitrário sem limite. Não aceitar senha em texto simples como fallback. Medir o custo no JDK de execução antes do aceite, sem reduzir silenciosamente o fator aprovado no plano. Base técnica: [Java SecretKeyFactory](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/javax/crypto/SecretKeyFactory.html) e [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

Login procura conta ativa por e-mail e verifica o hash no serviço. Manter métodos/overloads necessários para compilação dos consumidores existentes, sempre delegando ao caminho seguro. Nenhum retorno HTTP serializa entidade com credenciais. Cadastro inicial força atividade; atualização de candidato/empresa rejeita conta já inativa, não permite reativação incidental e aplica hash a senha nova.

Usar `HttpSession` nativa do Servlet para guardar o ator autenticado no servidor, com ID e tipo, invalidando sessão anterior no login e removendo seu uso quando a conta estiver inativa. É contexto mínimo das operações HTTP, sem tabela de identidade, token próprio ou subsistema genérico de autorização. Validar atividade em cada operação protegida; não confiar no ID/tipo de `localStorage`, no corpo ou em cabeçalho controlado pelo cliente. `sessao` permanece apenas navegação frontend. A sessão de uma conta não habilita operar como outro perfil.

Configurar frontend em `http://127.0.0.1:5500` e API em `http://127.0.0.1:8080`, evitando misturar `localhost` e `127.0.0.1` no fluxo de cookies. Clientes usam `credentials: "include"`; CORS permite essa origem explícita e credenciais, trata `OPTIONS` e não usa origem `*`. Usar cookie de sessão `HttpOnly` e `SameSite=Lax`; para mutações HTTP validar a origem permitida e exigir JSON. Não criar novos endpoints de infraestrutura além dos necessários à jornada.

### 3.5 Contratos HTTP propostos

**A tabela abaixo foi a proposta histórica e está implementada no backend até T11. Os marcadores novo/adaptar descrevem a classificação original, não trabalho backend pendente. O frontend importado deve consumir esses contratos vigentes, sem alterar as rotas para reproduzir master.** Campos permanecem em português/camelCase, datas de entrada em ISO (`YYYY-MM-DD`) e competências de entrada como nomes. O ator é obtido da sessão. IDs de rota delimitam recursos e são comparados ao ator e aos vínculos persistidos.

| Método e caminho | Entrada e resultado planejados |
| --- | --- |
| `POST /candidatos`, `POST /empresas` — existentes | Preservar campos reais dos DTOs de cadastro e `201`; cadastrar ativo e com hash. Sem credenciais no retorno. Frontend navega para login depois do cadastro, sem inventar ID local. |
| `POST /login/candidato`, `/login/empresa`, `/login/recrutador` — novos | `{email, senha}`; `200` com `{id, tipo}` e sessão HTTP. Falha genérica `401`, sem revelar senha ou hash. |
| `GET /candidatos/{id}`, `/empresas/{id}` — existentes, restringir | Perfil próprio apenas, validando sessão, atividade e ID. Não usar para descobrir a outra parte. Listagens gerais identificáveis sem contexto deixam de ser públicas. |
| `PUT /candidatos/{id}`, `/empresas/{id}` — existentes, proteger | Apenas próprio cadastro ativo, validações e hash. Não incluir PUT de recrutador nesta entrega. |
| `POST /empresas/{idEmpresa}/recrutadores` — novo | Nome, CPF, e-mail, nascimento, descrição, senha e `idAreaRecrutamento`; empresa deriva da rota validada contra sessão empresarial. `201`, sem credenciais; rejeitar referência/campo empresarial conflitante. |
| `GET /recrutadores/{id}` — novo | Somente perfil próprio ativo, DTO sem senha/hash/salt. Não criar listagem pública de recrutadores. |
| `GET /areas-recrutamento`, `/referencias/paises`, `/referencias/estados` — novos | Arrays de `{id, nome}` para áreas/países e `{id, sigla}` para estados; referências necessárias aos formulários. Seleção frontend envia os nomes/siglas exigidos pelos cadastros atuais, não troca os contratos por IDs. |
| `GET /competencias` — existente | Preservar `string[]` do catálogo backend. |
| `POST /vagas` — existente, adaptar | `{nome, descricao, local, competencias, idEmpresa?}`; empresa autenticada cria vaga direta. Se `idEmpresa` vier, deve corresponder ao ator; impedir injeção de autoria. `201`. |
| `POST /recrutadores/{id}/vagas` — novo | `{nome, descricao, local, competencias}`; autor vem da sessão/rota e empresa do vínculo persistido. Campos de empresa/autoria conflitantes são rejeitados. `201`. |
| `GET /vagas/empresa/{id}`, `/vagas/recrutador/{id}` — novos | DTO interno com campos de `VagaEmpresaDTO` e identificação do autor quando houver. Cada perfil consulta somente seu escopo. Empresa identifica autor histórico mesmo inativo. |
| `GET /vagas` — existente; `GET /vagas/{id}` — adaptar | Candidato recebe apenas `VagaAnonimaDTO`, inclusive após match. Acesso interno identificável usa os caminhos delimitados acima. Não serializar entidade completa no GET por ID. |
| `POST /curtidas/vagas` — novo | `{idVaga}` e candidato da sessão; `204`, inclusive repetição válida. Não gera match. |
| `GET /curtidas/vagas/candidato/{id}` — novo | Apenas o titular, com lista de `VagaAnonimaDTO`. |
| `GET /curtidas/candidatos/vaga/{idVaga}` — novo | Empresa proprietária ou recrutador autor ativo; somente `CandidatoAnonimoDTO`. |
| `POST /curtidas/candidatos` — novo | `{idCandidato, idVaga}`; empresa ou recrutador da sessão, sem escolher ator no corpo. Retribuição e match atômicos; `204` também na repetição válida. |
| `GET /matches/candidato/{id}`, `/matches/empresa/{id}`, `/matches/recrutador/{id}` — novos | Cada titular no seu escopo; campos atuais de `MatchEncontradoDTO` mais `nomeRecrutador` e `areaRecrutamento` anuláveis. Não adicionar perfil completo do recrutador. |

Para rotas novas: `400` para formato/campos/referências inválidas; `401` para ausência de sessão ou conta inativa; `403` para ator/perfil incompatível; `404` para recurso inexistente; `409` para unicidade ou ausência de interesse necessário à retribuição; `500` genérico para falha inesperada. Não devolver detalhes SQL, identificadores de conta alheia ou credenciais nos erros. Preservar códigos já compatíveis e documentar explicitamente os ajustes necessários de erro/visibilidade nas rotas existentes. Não registrar rota HTTP de gravação direta de match. PUT de vaga existente não pode permitir trocar propriedade/autoria ou contornar escopo; não ampliar seu uso no frontend.

### 3.6 Frontend e testes

A base é o frontend integrado importado de `origin/master`, não a versão local da análise inicial. Reutilizar os clientes específicos, DTOs organizados por domínio, serviços com `Promise`, estratégias e componentes já existentes. Não reinstalar a estrutura descartada de T12 nem criar pares concorrentes de contratos para a mesma capacidade. Um cliente base compartilhado pode concentrar host, cookies, JSON e falhas, preservando os clientes por domínio e a composição manual.

T12 adapta o transporte **ao backend atual de T11**, sem alterar backend, schema ou rotas: host `127.0.0.1`, `credentials: "include"`, corpos de interesse/retribuição sem identidade escolhida pelo cliente, sucesso vazio em `201`/`204` e tratamento de `401`/`403`/`409`. Remover do caminho ativo as chamadas `/verificar` inexistentes e substituir a consulta candidata por uma listagem de vagas curtidas; na retribuição, preservar a repetição idempotente sem inferir reciprocidade global. Não inventar IDs de candidato/vaga no DTO de match nem deduzi-los por nomes para reconstruir estado.

DTOs anônimos continuam limitados a ID, descrição e competências. Perfis próprios, vagas internas e matches mantêm contratos separados. Acrescentar autoria somente no contrato interno e nome/área anuláveis somente no match. Datas de entrada usam ISO; tipar a resposta conforme o JSON efetivo do backend. Adaptar explicitamente `localizacao → local`, sem persistir `tipo` ou `formacao`. O catálogo selecionável usa nomes (`string[]`), sem fabricar IDs; competências de respostas anônimas/de perfil continuam referências `{id,nome}`. O gráfico já usa interessados anônimos por vaga e deve preservar essa contagem por interesse.

T13 reaproveita os formulários com senha e referências já presentes, padroniza sessão/navegação e acrescenta login do recrutador. T14 substitui os checkboxes pelo seletor pesquisável. T15/T16 estendem os componentes de matches existentes; T17 acrescenta a jornada do recrutador. Adaptações mecânicas mínimas de consumidores cabem em T12 para manter o conjunto coerente, sem declarar concluídas as jornadas futuras.

Reutilizar os testes backend já entregues quando aplicável; a importação não os reexecutou. Integração com escrita exige PostgreSQL dedicado e auditoria prévia de todas as conexões/setup/cleanup, sem fallback para desenvolvimento. Não introduzir dependência Testcontainers, framework frontend ou Postman obrigatório. Validar frontend por TypeScript estrito, Gulp/Webpack, checagens de transporte reproduzíveis e navegador; não declarar compilação ou simulação como prova de aceite full-stack.

## 4. Tarefas numeradas de implementação

Convenções: `B = src/main/groovy/com/jpcode`, `T = src/test/groovy/com/jpcode`, `F = frontend/src`. Caminhos com esses prefixos são relativos à raiz. Expansões `{A,B}` indicam arquivos individuais. **Novo** significa proposta futura, não arquivo já existente. Cada tarefa deve produzir um incremento revisável; tarefas dependentes só começam após validação da anterior. Registrar a entrega correspondente em `CHANGELOG.md` quando concluída, sem antecipar resultados.

### Banco

### T01 — Criar referências e cadastro persistido de recrutador

- **Objetivo:** estabelecer as tabelas e o ciclo inicial necessários ao cadastro.
- **Referência:** RN-01, RN-06; seção 5, “Recrutador” e “Área de recrutamento”; CA-01, CA-02.
- **Camada:** Banco.
- **Arquivos existentes:** `queriesLinketinder.sql` (somente seção atual).
- **Arquivos novos:** `T/integracao/SchemaRecrutadorTest.groovy`, `T/integracao/SuportePostgres.groovy` (suporte específico a banco de testes, sem credenciais versionadas).
- **Dependências:** nenhuma tarefa; implementação depende dos gates SDD da seção 8.
- **Passos:** criar tabelas/FKs da seção 3.1; estabelecer defaults de atividade nos três perfis; definir preparação manual de áreas e demais referências, sem alterar dados legados. Criar suporte que opere apenas com configuração explícita de banco de testes e falhe se apontar para o banco de desenvolvimento.
- **Validação:** inspeção do DDL; integração PostgreSQL com criação de área/recrutador e rejeição de empresa/área ausente; conferir estado inicial ativo e FK simples de área. Executar o teste específico segundo seção 6.
- **Conclusão:** esquema novo comporta uma empresa e uma área por recrutador e rejeita referências inexistentes; nenhuma FK/cascade anterior foi alterada incidentalmente.

**Estado da T01 em 07/11/2026: concluída.** Criados o DDL de áreas/recrutadores e os dois arquivos de teste previstos. Candidatos passam a ter `ativo NOT NULL DEFAULT TRUE`; empresas conservam esse default e recrutadores também o recebem. A preparação manual das referências foi documentada no SQL, separada dos exemplos legados, sem alterar suas inserções. As FKs/cascades anteriores foram preservadas. Unicidades condicionais, autoria de vagas e implementação backend/frontend continuam nas tarefas seguintes.

**Ambiente e isolamento:** JDK 25.0.3 e PostgreSQL 18. Após autorização adicional do responsável, foi criado o banco dedicado `linketinder_test_t01_c8946707`. Sua configuração foi acrescentada somente ao `.env` local, ignorado pelo Git, sem divulgar credenciais ou alterar dados do banco atual. O suporte exige `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` no ambiente do processo, não carrega `.env` automaticamente e não utiliza `PostgresConnectionFactory`. Aceita somente banco `linketinder_test` ou `linketinder_test_<sufixo>`, valida o banco conectado e usa schema aleatório sem `public` no caminho de busca. Cada teste executa somente o DDL atual e desfaz schema/fixtures por rollback.

**Evidências executadas:**

- `.\gradlew.bat compileGroovy compileTestGroovy` — compilação bem-sucedida. A primeira tentativa no sandbox falhou ao acessar o cache; a repetição autorizada fora dele passou.
- `.\gradlew.bat test --tests 'com.jpcode.integracao.*'` — inicialmente 7 testes de configuração passaram e 10 JDBC foram ignorados por ausência de configuração; esse resultado não foi tratado como aceite.
- `.\gradlew.bat test --tests 'com.jpcode.integracao.*' --rerun-tasks`, com configuração do banco dedicado carregada do `.env` no processo — 17 testes passaram, incluindo os 10 JDBC, sem falhas ou testes ignorados. Cobertos persistência com área/empresa compartilhadas, defaults dos três perfis, rejeição de FKs ausentes/nulas, atividade nula e FKs simples sem cascades novos.
- `.\gradlew.bat build` — build inicial bem-sucedido; repetido com banco configurado via `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01`: 61 testes passaram, sem falhas ou testes ignorados. O init script temporário, fora dos arquivos versionados, apenas exportou o classpath de runtime para a verificação de inicialização.
- `java -cp <classpath de runtime> com.jpcode.api.RestServer` — aplicação iniciada em diretório temporário sob `build/`; `GET http://127.0.0.1:8080/__t01_startup_probe__` retornou `404`, como esperado para rota inexistente. Somente o processo iniciado na verificação foi encerrado. Essa evidência comprova inicialização, não um fluxo HTTP de recrutador, que não integra T01.
- Inspeção integral do diff e `git diff --check` — sem erros de whitespace; alterações restritas à T01 e sua documentação. DTOs, consultas HTTP e frontend não foram alterados; não há nova exposição de identidade. A matriz HTTP completa permanece nas tarefas posteriores.

**Reexecução:** carregar as três variáveis de teste a partir do `.env` para o ambiente do processo, preservando as credenciais exclusivamente nesse arquivo, e executar `.\gradlew.bat test --tests 'com.jpcode.integracao.*' --rerun-tasks` e `.\gradlew.bat build`. O usuário de teste precisa poder criar schemas no banco dedicado; o suporte não cria bancos automaticamente. Ausência total de configuração indica testes JDBC não executados; configuração parcial ou banco não permitido falha antes da conexão. Relatórios: `build/reports/tests/test/` e `build/reports/jacoco/test/`.

**Limites:** nenhuma migração, referência manual ou exemplo legado foi aplicado ao banco atual. Não foram implementadas T02–T18. Changelog atualizado somente com entregas da T01. Nenhum commit ou alteração de staging executado; o pedido explícito desta implementação prevaleceu sobre o checkpoint de commit da skill. Consumo de tokens não disponível como medição confiável.

### T02 — Registrar autoria de vaga com integridade empresarial

- **Objetivo:** distinguir vaga direta e vaga de recrutador sem duplicar propriedade.
- **Referência:** RN-02, RN-06; seção 5, “Vaga”; CA-05, CA-18.
- **Camada:** Banco.
- **Existentes:** `queriesLinketinder.sql`; teste de schema criado em T01.
- **Novos:** nenhum.
- **Dependências:** T01.
- **Passos:** adicionar coluna anulável e chave composta de suporte no recrutador; estabelecer FK autor/empresa; preparar chave `(id, id_empresa)` de vagas para T04. Não apagar autoria ao inativar.
- **Validação:** integração com vagas direta e de autor; rejeitar empresa divergente e autor inexistente; inativar fixture e confirmar referência preservada.
- **Conclusão:** banco aceita ambas as origens e impede combinação entre empresas diferentes.

**Estado da T02 em 07/11/2026: concluída.** Adicionados somente ao DDL atual `vagas.id_recrutador` anulável, `uk_recrutador_empresa (id, id_empresa)`, `fk_vaga_recrutador_empresa (id_recrutador, id_empresa)` e `uk_vaga_empresa (id, id_empresa)`. A FK empresarial obrigatória permanece; as ações referenciais anteriores não foram modificadas e a FK nova não possui cascades. A chave de vagas prepara a referência prevista em T04, sem implementar suas interações.

**Validação executada:** reutilizado o banco dedicado configurado em T01, carregando somente `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` do `.env` local para o processo, sem modificar esse arquivo. Cada caso JDBC usou o DDL atual em schema temporário e rollback pelo suporte existente; nenhum schema/dado de desenvolvimento ou exemplo legado foi alterado.

- `.\gradlew.bat test --tests 'com.jpcode.integracao.*' --rerun-tasks` — 29 testes passaram: 22 JDBC e 7 do suporte, sem falhas, erros ou testes ignorados. A tentativa inicial no sandbox falhou ao criar o lock do cache Gradle em `C:\.gradle`; a repetição autorizada fora dele passou. Compilação Groovy e dos testes executada com sucesso nessa execução.
- Os 12 casos novos cobrem vaga direta omitindo autor (compatível com o INSERT atual do DAO), autor explicitamente nulo, autoria válida, rejeição de empresa divergente/autor inexistente em INSERT e UPDATE, empresa nula/inexistente, autoria preservada após inatividade, FK composta sem cascades e chave única de suporte da vaga. SQLStates observados: `23503` para FK e `23502` para propriedade nula. Esses testes demonstram a parcela de persistência de CA-05/CA-18; não representam o aceite full-stack desses critérios.
- `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01` — build completo bem-sucedido, 73 testes passaram, sem falhas, erros ou testes ignorados. Reutilizado o init script temporário não versionado de T01 exclusivamente para exportar o classpath de runtime. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.
- `java -cp <classpath de runtime> com.jpcode.api.RestServer` — iniciado em diretório temporário sob `build/`, com `GET http://127.0.0.1:8080/__t02_startup_probe__` retornando `404`, esperado para rota inexistente. Encerrado somente o processo iniciado nessa verificação. Não houve requisição de negócio nem uso do banco de desenvolvimento; a verificação funcional de T02 foi realizada pelo JDBC real.
- Revisão integral do diff e `git diff --check` — sem erros de whitespace; somente SQL atual, teste de schema, changelog e este plano alterados. DTOs anônimos continuam com `id`, `descricao` e `competencias`; nenhum produtor/consumidor HTTP ou frontend foi alterado. A verificação de anonimato nesta tarefa é estática, sem alegar validação da matriz HTTP futura.

**Reexecução e limites:** carregar as três variáveis de teste do `.env` para o processo e executar `.\gradlew.bat test --tests 'com.jpcode.integracao.*' --rerun-tasks` e `.\gradlew.bat build`. Não há pendência de validação de T02. A aplicação do DDL a uma instalação nova continua manual; nenhuma migração foi executada. T03 e tarefas seguintes não foram implementadas. Especificação aprovada preservada. Nenhum `git add`, `git commit` ou `git push` executado. Consumo de tokens não disponível como medição confiável.

### T03 — Preparar unicidades condicionadas à atividade

- **Objetivo:** permitir reutilização dos identificadores inativos no escopo correto.
- **Referência:** RN-07, RN-08, RN-09; seção 5, “Integridade de identificadores”; CA-19 a CA-22.
- **Camada:** Banco.
- **Existentes:** `queriesLinketinder.sql`; teste de schema de T01.
- **Novos:** nenhum.
- **Dependências:** T01.
- **Passos:** substituir constraints permanentes por índices parciais dos três perfis; manter PKs/FKs por ID. Não declarar resolvida a unicidade global de e-mail nesta tarefa: ela depende de T06.
- **Validação:** testes JDBC para duplicatas ativas, reutilização inativa e CPF coincidente entre candidato/recrutador. Conferir que novos IDs não herdam vagas, recrutadores ou matches antigos.
- **Conclusão:** unicidade intratabela correta; pendência entre tabelas explicitamente vinculada a T06.

**Estado da T03 em 07/11/2026: concluída.** Removidas somente no DDL atual as quatro unicidades permanentes de CPF/e-mail de candidatos e CNPJ/e-mail de empresas. Criados seis índices únicos parciais `WHERE ativo = true`: CPF/e-mail de candidatos, CNPJ/e-mail corporativo de empresas e CPF/e-mail de recrutadores. PKs, FKs por ID e chaves compostas de T02 foram preservadas. Não foi introduzida normalização de identificadores nem alterada sua comparação. A unicidade global e concorrente de e-mail entre tabelas **permanece pendente de T06**; CA-20 não está integralmente concluído por T03.

**Validação executada:** reutilizado o suporte de T01/T02 e o banco dedicado já configurado, carregando apenas as três variáveis `LINKETINDER_TEST_*` do `.env` para o processo, sem modificar o arquivo ou exibir credenciais. Todos os casos JDBC executaram somente o DDL atual e fixtures próprias em schema temporário, desfeitos por rollback; nenhum dado/schema de desenvolvimento ou exemplo legado foi aplicado.

- `.\gradlew.bat test --tests 'com.jpcode.integracao.*' --rerun-tasks` — tentativa inicial bloqueada no sandbox ao criar o lock do cache `C:\.gradle`. A primeira execução autorizada fora dele executou 52 casos, com 12 falhas: o `assert` do helper novo encapsulava a SQLException esperada em `ConditionFailedWithExceptionError`. Corrigido o helper para executar o comando antes da asserção de contagem; a repetição passou com **52 testes, zero falhas, erros ou ignorados** (45 JDBC e 7 do suporte), incluindo compilação Groovy e dos testes.
- Os **23 casos novos** cobrem os seis identificadores em INSERT, UPDATE e tentativa SQL de reativação conflitante (`23505`, sem criar funcionalidade de reativação); reutilização dos identificadores após inatividade nos três perfis, inclusive múltiplas contas inativas; mesmo CPF entre candidato e recrutador ativos com e-mails distintos; IDs novos sem herança de vagas, recrutadores, interesses, retribuições ou matches antigos. As fixtures de interação usam exclusivamente o schema existente, sem implementar T04.
- `.\build\executar-t01.ps1` — reutilizado o script temporário existente, que executa `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01` com banco de teste configurado. **Build bem-sucedido, 96 testes, zero falhas, erros ou ignorados**, incluindo os 44 testes de serviços e regressão de T01/T02. O init script apenas exporta o classpath de runtime; nenhum arquivo de build versionado foi alterado. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.
- `.\build\iniciar-t03.ps1` — script temporário não versionado, baseado na verificação de T01, iniciou `java -cp <classpath de runtime> com.jpcode.api.RestServer` sob `build/tomcat-t03`; `GET http://127.0.0.1:8080/__t03_startup_probe__` retornou **404**, esperado para rota inexistente. A tentativa no sandbox foi bloqueada pela execução de scripts; a repetição autorizada fora dele passou. Encerrado somente o processo criado. Essa verificação comprova inicialização, sem consulta de negócio/banco de desenvolvimento; o comportamento de T03 foi verificado por JDBC real.
- Revisão integral do diff e `git diff --check` — alterações restritas ao DDL atual, teste de schema, changelog e este plano, sem erros de whitespace. DTOs anônimos preservados com somente `id`, `descricao` e `competencias`; produtores/consumidores HTTP, consultas de descoberta e frontend não foram modificados. A verificação de anonimato é estática, sem alegar validação completa da matriz HTTP futura.

**Reexecução e limites:** carregar `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` do `.env` para o ambiente do processo e executar `.\gradlew.bat test --tests 'com.jpcode.integracao.*' --rerun-tasks` e `.\gradlew.bat build`. Não há validação pendente de T03. O DDL permanece destinado à preparação manual de instalação nova; nenhuma migração foi executada. T04 e tarefas seguintes permanecem pendentes; a especificação aprovada foi preservada. Nenhum `git add`, `git commit` ou `git push` executado. A autorização explícita de T03 prevalece sobre o checkpoint de commit da skill. Consumo de tokens não disponível como medição confiável.

### T04 — Garantir contexto e reciprocidade no schema das interações

- **Objetivo:** suportar retribuição por vaga e match único íntegro.
- **Referência:** RN-03 a RN-05; seção 5, “Curtidas e matches”; CA-08 a CA-13.
- **Camada:** Banco.
- **Existentes:** `queriesLinketinder.sql`; teste de schema de T01; `B/dao/vaga/VagaDAO.groovy` somente para conferir o impacto na exclusão (alteração em T09).
- **Novos:** nenhum.
- **Dependências:** T02.
- **Passos:** acrescentar vaga/ator na retribuição, unicidades e FKs descritas em 3.3; não gerar matches ou converter curtidas globais antigas. Conservar interesse inicial e suas constraints. Verificar dependências de exclusão sem modificar cascades existentes.
- **Validação:** inserir match sem interesse/retribuição deve falhar; rejeitar empresa divergente; repetir candidato/vaga não duplica; vaga B exige interesse e retribuição próprios. Verificar que as novas referências têm suporte de chave única válido.
- **Conclusão:** o banco impede match órfão ou de proprietária incompatível; execução atômica/idempotente fica para T09.

**Estado da T04 em 07/11/2026: concluída.** Alterado somente o DDL da seção atual: retribuição com `id_vaga` obrigatório e `id_recrutador_ator` opcional, unicidade por candidato/vaga e chave única de suporte candidato/empresa/vaga. Acrescentadas FKs compostas para interesse, vaga/proprietária e recrutador ator/empresa. O match é único por candidato/vaga e referencia a vaga/proprietária e a retribuição correspondente. A ordem de criação coloca o interesse antes da retribuição; o interesse e suas constraints permanecem intactos. Nenhum autor foi duplicado no match; não foram criados triggers, geração automática de match ou conversão de curtidas legadas.

**Segurança do banco verificada antes da execução:** reutilizado o suporte de T01 e a configuração dedicada existente, carregando apenas `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` do `.env` local para o processo. O script não modifica esse arquivo nem divulga seus valores. O suporte rejeita configuração parcial e qualquer banco fora de `linketinder_test`/`linketinder_test_<sufixo>`, sem fallback para `PostgresConnectionFactory`; confere `current_database()` antes das escritas, cria schema aleatório sem `public` no `search_path` e desfaz DDL/fixtures por rollback. Não foram executados exemplos legados, migrações ou escritas no banco de desenvolvimento.

**Evidências executadas:**

- `.\build\executar-t01.ps1` — reutilizado o script temporário existente, que carrega a configuração dedicada e executa `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01`. A tentativa inicial foi bloqueada pela política de execução de scripts no sandbox; a repetição autorizada fora dele passou: **BUILD SUCCESSFUL, 116 testes, zero falhas, erros ou ignorados**, confirmados nos XMLs de `build/test-results/test/`. Compilação de testes, build e relatório JaCoCo concluídos; compilação principal sem mudanças, `UP-TO-DATE`. São 65 casos JDBC, 7 de segurança do suporte e 44 de serviços, incluindo regressão de T01–T03. Nenhum arquivo de build versionado foi alterado.
- Os **20 casos novos** verificam: interesse inicial único sem retribuição/match; match rejeitado sem interesse, sem retribuição, com retribuição em outra vaga ou empresa divergente (`23503`); retribuição rejeitada sem interesse, com empresa divergente, ator de outra empresa/inexistente (`23503`) ou vaga nula (`23502`); retribuição empresarial em vaga direta/autoral e por recrutador sem alterar autoria; isolamento entre duas vagas; repetição sequencial com `ON CONFLICT (id_candidato, id_vaga) DO NOTHING` e rejeição de duplicata sem essa cláusula (`23505`). A fixture histórica de T03 foi adaptada somente para informar a vaga obrigatória.
- Rejeitadas também remoção de interesse/retribuição referenciados, troca da proprietária e exclusão direta da vaga com retribuição (`23503`). Inspeção de `pg_constraint` confirmou as cinco FKs novas com `NO ACTION` e os três `ON DELETE CASCADE` anteriores preservados. A criação real de todo o DDL e os INSERTs válidos confirmam o suporte de chaves únicas das referências compostas.
- Revisão integral do diff e `git diff --check` — sem erros de whitespace; alterações restritas ao SQL atual, teste de schema, changelog e plano. DTOs anônimos permanecem com somente `id`, `descricao` e `competencias`; consultas, servlets, contratos de acesso e frontend não foram modificados. Esta verificação de anonimato/acesso é estática e não representa aceite da matriz HTTP futura. Verificação funcional feita por JDBC real; nenhum teste HTTP/frontend foi executado nesta tarefa de banco.

**Impactos e limites previstos, sem bloqueio de T04:** `VagaDAO.deletar` ainda executa apenas `DELETE FROM vagas`; as novas referências impedem excluir diretamente vaga com retribuição. T09 deverá ordenar a remoção de match/retribuição antes da vaga na mesma transação. `EmpresaCurtirDAO` ainda usa INSERT global sem vaga e alvo de conflito empresa/candidato; `MatchDAO` ainda usa alvo candidato/empresa/vaga. Esses caminhos não são compatíveis com o DDL novo e sua adaptação pertence explicitamente a T09, sem alegar funcionamento deles neste schema. Atividade/autoria do ator, atomicidade, concorrência e idempotência da aplicação também permanecem em T09; os testes desta tarefa comprovam as garantias de schema, não o aceite integral de CA-08 a CA-13.

**Conclusão dos critérios de T04:** match órfão ou de proprietária incompatível é impedido; repetição candidato/vaga não duplica; vaga B requer seus próprios vínculos; chaves de suporte válidas e dependências de exclusão verificadas. Não há validação pendente de T04. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`. Especificação preservada; T05 e seguintes não implementadas. Nenhum `git add`, `git commit` ou `git push` executado; a autorização explícita deste pedido prevalece sobre o checkpoint de commit da skill. Consumo de tokens não disponível como medição confiável.

### Backend com testes

### T05 — Proteger senhas em todos os caminhos existentes

- **Objetivo:** derivar senhas e verificar credenciais sem exposição.
- **Referência:** seção 7, RN-06, D-15; CA-03, CA-04.
- **Camada:** Backend.
- **Existentes:** `B/service/{CandidatoService,EmpresaService}.groovy`, `B/dao/{candidato/CandidatoDAO,empresa/EmpresaDAO}.groovy`, `B/dao/contratos/UsuarioRepository.groovy`, `B/exception/{candidato/CandidatoLoginException,empresa/EmpresaLoginException}.groovy`, `B/config/ServiceConfig.groovy`, `T/service/{CandidatoServiceTest,EmpresaServiceTest}.groovy`.
- **Novos:** `B/service/SenhaService.groovy`, `T/service/SenhaServiceTest.groovy`.
- **Dependências:** T01 (estado inicial), sem dependência do frontend.
- **Passos:** implementar parâmetros de 3.4; buscar conta ativa por e-mail; substituir comparação SQL por verificação de hash no serviço; cobrir cadastro e atualização de senha de candidato/empresa. Proteger atualização inativa no serviço e no `UPDATE ... WHERE ativo = true`, sem reativação via DTO. Sanear exceções; preservar compatibilidade de compilação dos consumidores existentes usando delegação segura quando necessária.
- **Validação:** unitários para senha correta/incorreta, mesmo segredo com salts diferentes, formato inválido, conta inativa e atualização; verificar ausência de credenciais nas mensagens. Medição real do tempo de derivação no JDK de execução como evidência separada.
- **Conclusão:** nenhuma senha nova dos fluxos abrangidos é persistida em texto simples; login não depende de comparação da senha no SQL.

**Estado da T05 em 08/11/2026: concluída.** Criado `SenhaService` com os parâmetros aprovados em 3.4: PBKDF2-HMAC-SHA256, 600.000 iterações, salt de 16 bytes por `SecureRandom` e chave de 256 bits. A representação `v1$PBKDF2WithHmacSHA256$600000$<salt Base64>$<hash Base64>` valida versão, algoritmo, custo exato, tamanho e codificação canônica antes da derivação; custos arbitrários e texto simples são rejeitados. Comparação por `MessageDigest.isEqual` e limpeza de buffers temporários/`PBEKeySpec`. Nenhuma dependência adicionada.

Cadastros e atualizações de candidato/empresa derivam a senha no serviço antes de chamar o DAO. `UsuarioRepository.buscarAtivoPorEmail` substitui a busca por e-mail/senha e filtra atividade no SQL; os serviços também rejeitam conta inativa e verificam o hash. Atualizações consultam o estado persistido, rejeitam conta inexistente/inativa e não usam `ativo` do DTO para mudar o ciclo de vida. Ambos os DAOs usam `UPDATE ... WHERE id = ? AND ativo = true` e sinalizam zero linhas alteradas. `ServiceConfig` injeta o serviço de senhas; construtores anteriores dos serviços continuam disponíveis pelo parâmetro padrão seguro. Exceções de login têm mensagem genérica; os construtores anteriores delegam sem incluir e-mail, senha, hash ou salt. Nenhum consumidor restante da busca SQL antiga foi encontrado.

**Arquivos:** criados `B/service/SenhaService.groovy`, `T/service/SenhaServiceTest.groovy` e `T/integracao/SenhaPersistenciaTest.groovy` (este último para verificar os DAOs reais de T05). Modificados os dois serviços de contas, os dois DAOs, `UsuarioRepository`, as duas exceções de login, `ServiceConfig` e os dois testes de serviços previstos, além deste plano e do changelog. A alteração preexistente do responsável em `queriesLinketinder.sql` foi preservada, sem qualquer edição pelo agente.

**Segurança do banco verificada antes das escritas:** inspecionados `SuportePostgres` e o script temporário existente `build/executar-t01.ps1`. O script carrega do `.env` somente variáveis de teste para o processo, sem imprimir valores nem editar o arquivo. O suporte exige as três variáveis explícitas, aceita exclusivamente banco `linketinder_test` ou `linketinder_test_<sufixo>`, sem parâmetros JDBC ou fallback para `PostgresConnectionFactory`, e confere `current_database()` antes de criar schemas. Cada caso usa schema aleatório sem `public` no `search_path`, DDL atual sem exemplos e rollback no encerramento. O teste novo empresta essa conexão aos DAOs por proxy que adia somente `close` até o cleanup, mantendo JDBC real e isolamento. Nenhuma escrita/migração/reset no banco de desenvolvimento foi executada.

**Evidências executadas:**

- `.\gradlew.bat test --tests 'com.jpcode.service.*'` — bloqueado inicialmente pelo cache Gradle no sandbox; repetição autorizada fora dele compilou o código principal, mas falhou na compilação dos testes por import ausente de `AtualizarCandidatoDTO`. Import corrigido antes do build completo; essa tentativa não é contada como testes aprovados.
- `.\build\executar-t01.ps1` — primeira tentativa bloqueada pela política de scripts no sandbox; repetição autorizada fora dele executou `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01` com banco dedicado configurado: **BUILD SUCCESSFUL, 148 testes, zero falhas, erros ou ignorados**, conferidos nos XMLs de `build/test-results/test/`. São 70 testes de serviços, 71 JDBC reais e 7 de segurança do suporte. Regressão de T01–T04 preservada; compilação principal já concluída na tentativa anterior, compilação dos testes, empacotamento e JaCoCo concluídos no build final.
- `java -version` — Oracle JDK **25.0.3**, HotSpot 64 bits. `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/medir-senha-t05.groovy` — script temporário não versionado, sem banco e sem imprimir credenciais; 3 aquecimentos e 10 derivações medidas. Primeira execução: mínimo **455,93 ms**, mediana **466,57 ms**, média **474,96 ms**, máximo **539,58 ms**. Repetição após o término dos testes: mínimo **461,63 ms**, mediana **465,38 ms**, média **469,83 ms**, máximo **511,12 ms**. Custo observado neste ambiente, sem reduzir o fator aprovado nem definir SLA.
- Revisão integral do diff, incluindo arquivos novos, e `git diff --check` — sem erros de whitespace. A única alteração anterior à tarefa era a remoção de SQL legado pelo responsável; as alterações do agente pertencem exclusivamente a T05. Não houve alterações de frontend, schema, rotas, DTOs anônimos ou regras de match.

**Verificação dos critérios de T05:**

| Critério | Evidência observada |
| --- | --- |
| Senha correta/incorreta, salt individual e parâmetros aprovados | `SenhaServiceTest`: duas derivações do mesmo segredo diferem, ambas verificam; senha errada falha; representação gerada independentemente pela API Java verifica |
| Formato inválido, custo limitado e ausência de fallback | Rejeitados texto simples, versão/algoritmo/custo alterados, overflow, Base64 inválido/não canônico, salt curto e campos extras |
| Cadastro e atualização sem persistência em texto simples | Asserções dos dois testes de serviço e round-trip JDBC real dos dois perfis; senha nova verifica e a anterior deixa de autenticar |
| Login por conta ativa e atualização protegida | JDBC confirma bloqueio após desativação, seleção da nova conta ativa com e-mail reutilizado e rejeição de UPDATE de entidade lida antes da desativação; unitários rejeitam conta ausente/inativa mesmo com DTO ativo |
| Ausência de credenciais nos erros e consultas abrangidos | Mensagens exatas das exceções, inclusive construtores de compatibilidade; serialização dos DTOs reais de perfil sem senha/hash/salt |
| Compatibilidade e custo medido | Build completo e regressões aprovados; medição separada no JDK efetivamente usado |

**Anonimato, acesso e limites:** DTOs anônimos permanecem exclusivamente com `id`, `descricao` e `competencias`; produtores/consumidores de descoberta e match não foram alterados. Inspeção das servlets de candidato/empresa confirmou uso de DTOs de consulta e ausência de serialização da entidade com credenciais nas operações abrangidas. Login e atualização de inativos foram exercitados nos serviços/DAOs reais; a matriz completa de autorização HTTP, sessões e navegador continua nas tarefas posteriores. Não foram executados testes HTTP ou frontend, nem implementados login HTTP/recrutador ou unicidade concorrente entre perfis. CA-03/CA-04 estão verificados somente na parcela de T05 para os dois perfis existentes, sem declarar aceite full-stack dos três perfis.

**Conclusão:** todos os critérios de T05 verificados, sem bloqueios pendentes da tarefa. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`. Changelog atualizado somente com entregas implementadas. Especificação e estado das demais tarefas preservados; T06 e seguintes não iniciadas. Nenhum `git add`, `git commit` ou `git push` executado; a autorização explícita deste pedido prevalece sobre o checkpoint de commit da skill. Consumo de tokens não disponível como medição confiável.

### T06 — Implementar recrutador, áreas e gravação serializada de contas

- **Objetivo:** cadastrar recrutador pela empresa e concluir unicidade entre perfis.
- **Referência:** RN-01, RN-06 a RN-09; seções 3, 5 e 7; CA-01, CA-02, CA-19 a CA-22.
- **Camada:** Backend.
- **Existentes:** `B/dao/{candidato/CandidatoDAO,empresa/EmpresaDAO}.groovy`, `B/service/{CandidatoService,EmpresaService}.groovy`, `B/config/{DaoConfig,ServiceConfig,ControllerConfig}.groovy`; testes existentes correspondentes.
- **Novos:** `B/model/core/Recrutador.groovy`, `B/model/referencia/AreaRecrutamento.groovy`, `B/dao/contratos/RecrutadorRepository.groovy`, `B/dao/recrutador/RecrutadorDAO.groovy`, `B/dao/referencia/AreaRecrutamentoDAO.groovy`, `B/dao/conta/ValidacaoContaDAO.groovy`, `B/service/RecrutadorService.groovy`, `B/controller/recrutador/RecrutadorController.groovy`, `B/dto/recrutador/{CadastrarRecrutadorDTO,RecrutadorViewDTO}.groovy`, `B/exception/conta/IdentificadorEmUsoException.groovy`, `T/service/RecrutadorServiceTest.groovy`, `T/integracao/UnicidadeContaTest.groovy`.
- **Dependências:** T01, T03, T05.
- **Passos:** implementar cadastro com referências válidas, empresa ativa operante, senha derivada e atividade forçada; consulta própria sem credenciais; leitura de áreas. Implementar 3.2 em todos os métodos de gravação dos três DAOs, inclusive atualização/desativação já existentes. Retornar conflitos de domínio sem SQL; não expor endpoints opcionais.
- **Validação:** mocks para referências inválidas e cadastro; JDBC concorrente entre cada par de perfis, inclusive mesmo perfil, com barreira de início e conexões separadas; testar atualização concorrente e reutilização após inatividade. Auditar todos os INSERTs/UPDATEs de contas.
- **Conclusão:** somente uma conta ativa pode confirmar o mesmo e-mail; mesmo CPF entre candidato/recrutador continua permitido; falha libera locks e não altera dados.

**Estado da T06 em 08/11/2026: concluída.** Implementados entidade própria de recrutador (sem herdar `Pessoa`), área, DTOs de cadastro/perfil, contratos, DAOs, serviço e controller com composição manual nas três configurações existentes. `RecrutadorService.cadastrarRecrutador` recebe a empresa operante separadamente do DTO, exige empresa ativa e área existente, deriva a senha antes da persistência e força atividade inicial. O DAO revalida a empresa após obter os locks e protege a referência da área com `FOR KEY SHARE`. Cadastro e consulta própria retornam somente `RecrutadorViewDTO`, sem senha/hash/salt; consulta rejeita titular divergente, ausente ou inativo. A leitura das áreas vem do banco, sem catálogo fixo ou interface de gestão.

`ValidacaoContaDAO` possui a conexão e a transação: `READ COMMITTED`, locks `SHARE ROW EXCLUSIVE` na ordem `candidatos`, `empresas`, `recrutadores`, validação de e-mail ativo nos três perfis, CPF/CNPJ no perfil correto, escrita na mesma conexão e commit ou rollback com fechamento. Atualização exclui somente a própria linha do próprio perfil e exige atividade persistida; desativação participa dos mesmos locks. Os sete escritores existentes estão abrangidos: `salvar`, `atualizarDados` e `desativar` de candidato/empresa, e `salvar` de recrutador. Não foram criados atualização/desativação de recrutador ou endpoints opcionais. Os índices parciais de T03 permanecem como defesa intratabela; conflitos usam `IdentificadorEmUsoException` com mensagem genérica, sem SQL ou identificadores de terceiros. Não foi introduzida normalização de identificadores, identidade central ou trigger.

**Arquivos:** criados os do campo “Novos” desta tarefa e dois suportes necessários: `B/dao/contratos/AreaRecrutamentoRepository.groovy`, que acrescenta listagem ao contrato de referências sem modificar seus consumidores, e `T/integracao/SuporteContasPostgres.groovy`, para transações confirmadas em conexões independentes. Alterados os dois DAOs de contas existentes e as três configurações. `T/integracao/SuportePostgres.groovy` extrai a abertura validada de conexão para reutilização; `SenhaPersistenciaTest.groovy` substitui a conexão emprestada por conexões próprias, mantendo a regressão de T05 com o novo protocolo. Os serviços existentes de candidato/empresa já derivavam a senha e iniciavam contas ativas por T05, sem necessidade de alteração adicional. Atualizados somente este plano e o changelog na documentação.

**Segurança do banco verificada antes das escritas:** revisados o suporte existente, o novo suporte e o script temporário preexistente `build/executar-t01.ps1`. O script carrega somente as variáveis `LINKETINDER_TEST_*` do `.env` para o processo, sem editar o arquivo ou imprimir valores. `SuportePostgres.conectar` exige configuração completa, aceita exclusivamente `linketinder_test`/`linketinder_test_<sufixo>`, sem parâmetros JDBC e sem fallback para `PostgresConnectionFactory`, e verifica `current_database()` em cada conexão antes das escritas. T01–T04 continuam usando rollback do schema/fixtures; os testes de contas criam e confirmam schema aleatório próprio para permitir conexões independentes. O `search_path` contém somente esse schema; cleanup usa `DROP SCHEMA ... CASCADE` exclusivamente no nome gerado, validado pelo padrão `linketinder_test_[a-f0-9]{32}`, em nova conexão igualmente validada. Os executores são encerrados antes da limpeza, com espera e timeouts limitados. Nenhum reset, migração ou escrita no banco principal foi executado; apenas o DDL atual e fixtures próprias foram usados.

**Comandos e resultados observados:**

- `git status --short` e `git branch --show-current` — início sem alterações preexistentes, branch `feature/recruiter`.
- `.\gradlew.bat compileGroovy` — primeira tentativa bloqueada no sandbox pelo lock do cache `C:\.gradle`. Repetição autorizada fora dele revelou `return` com expressão em método `void` nas novas delegações de desativação; corrigido antes dos testes aprovados.
- `.\gradlew.bat test --tests 'com.jpcode.service.RecrutadorServiceTest'` — execução autorizada fora do sandbox: compilação principal/testes e **9 testes, zero falhas, erros ou ignorados**, confirmados no XML.
- `.\build\executar-t01.ps1` — tentativa no sandbox bloqueada pela política de scripts; primeira execução autorizada fora dele executou 189 testes com 9 falhas de comparação textual: a mensagem esperada nos testes novos continha `j?` em vez de `já` por codificação na geração do arquivo. As verificações anteriores nessas mesmas execuções já observavam somente um cadastro confirmado e um conflito de domínio. Corrigida a codificação dos arquivos novos e ampliadas as verificações de desativação concorrente, identificadores e rollback antes da repetição final.
- `.\build\executar-t01.ps1` — repetição final autorizada, executando `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01`: **BUILD SUCCESSFUL, 201 testes, zero falhas, erros ou ignorados**, conferidos nos XMLs. São **79 testes de serviços, 115 JDBC reais e 7 de segurança do suporte**. Inclui compilação, empacotamento, JaCoCo e regressão de T01–T05. Nenhuma dependência ou configuração de build versionada alterada.
- `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/verificar-composicao-t06.groovy` — script temporário não versionado: instancia `DaoConfig → ServiceConfig → ControllerConfig`, confirma `RecrutadorController` e serializa os DTOs anônimos reais com igualdade exata das chaves `id`, `descricao`, `competencias`. **Passou**, sem iniciar servidor ou abrir conexão.
- Auditoria dos INSERTs/UPDATEs de contas em `src/main` e revisão do diff completo, incluindo arquivos novos; `git diff --check` — todos os escritores abrangidos e sem erros de whitespace na revisão final.

**Critérios de T06 e rastreabilidade:**

| Critério e parcela dos requisitos | Evidência verificada |
| --- | --- |
| Cadastro com empresa ativa, área válida, hash e atividade inicial (RN-01; parcela backend CA-01/CA-02/CA-04) | Mocks rejeitam empresa ausente/inativa e área ausente/nula antes de derivar/persistir; round-trip de serviço/DAO reais confere hash, FKs e leitura das áreas; DAO revalida referências; três DAOs forçam ativo mesmo com entidade inativa |
| Consulta própria sem credenciais (seção 7; parcela backend de acesso) | Unitários rejeitam titular divergente, conta ausente/inativa, conferem chaves exatas do DTO e ausência de hash; consulta própria também exercitada com JDBC real |
| Apenas uma conta ativa confirma o e-mail (RN-08; CA-20 no escopo de persistência) | Nove combinações ordenadas de cadastro entre os três perfis, incluindo os pares do mesmo perfil; seis combinações de atualização contra cadastro; duas atualizações simultâneas; seis combinações de desativação contra cadastro. Barreira inicial, conexões reais independentes, timeouts e contagem final de e-mails ativos |
| Serialização efetiva e revalidação posterior ao commit | Teste mantém lock e e-mail ainda não confirmado na primeira conexão, observa o segundo escritor realmente bloqueado em `pg_locks`, confirma a primeira e verifica conflito de domínio no segundo, com uma única conta ativa |
| CPF por perfil e CNPJ empresarial (RN-07/RN-09; CA-19/CA-22 backend) | Mesmo CPF simultâneo em candidato/recrutador permitido; duplicata de CPF no próprio perfil e CNPJ empresarial rejeitada; disputa concorrente de CPF/CNPJ nos três perfis confirma somente um cadastro; conflito de UPDATE não altera os dados |
| Reutilização inativa sem transferência (RN-07 a RN-09; CA-21 backend) | Novos IDs nos três perfis, contas antigas mantidas, vagas/interesse preservados com IDs históricos; reutilização de e-mail também entre perfis; regressão de schema de T03 preserva os demais vínculos históricos |
| Falha reverte e libera locks | Falhas reais de INSERT nos três DAOs não reservam e-mail e permitem nova tentativa; falha SQL injetada após UPDATE na transação comum reverte a alteração e permite novas gravações concorrentes; conflitos de UPDATE também preservam dados |

Os **44 casos novos JDBC** estão em `UnicidadeContaTest`; os **9 unitários novos**, em `RecrutadorServiceTest`. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`. Para repetir, fornecer as três variáveis `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER`, `LINKETINDER_TEST_DB_PASSWORD` no ambiente e executar `.\gradlew.bat build`; ausência total de configuração indica explicitamente integração não executada, sem contar como aprovação.

**Anonimato, limites e conclusão:** DTOs e consultas de descoberta/match, servlets existentes, schema e frontend não foram alterados. Confirmadas por serialização as chaves exatas dos contratos anônimos; cadastro/perfil de recrutador não retornam credenciais. T06 fornece o contexto explícito de empresa/titular aos métodos backend; autenticação da origem desse contexto via sessão e restrições HTTP permanecem em T07/T11. Não foram executados testes de navegador ou HTTP, nem declarado aceite full-stack dos CA compartilhados. A garantia concorrente aplica-se a todos os escritores de contas da aplicação, não a SQL administrativo externo ao protocolo. Não há pendências ou bloqueios de T06. T07 e posteriores permanecem pendentes; especificação aprovada preservada; nenhum `git add`, `git commit` ou `git push` executado. A autorização explícita deste pedido prevalece sobre o checkpoint de commit da skill. Consumo de tokens não disponível como medição confiável.

### T07 — Autenticar os três perfis e manter contexto no servidor

- **Objetivo:** fornecer login HTTP real e distinguir ator autenticado nas operações.
- **Referência:** seção 7; RN-06; seção 3; CA-03, CA-04, CA-18.
- **Camada:** Backend.
- **Existentes:** controllers e serviços de candidato/empresa; recrutador de T06; `B/config/{ControllerConfig,ApiServletConfig}.groovy`.
- **Novos:** `B/api/servlet/LoginServlet.groovy`, `B/api/SessaoHttp.groovy` (leitura/validação específica do contexto Servlet), `T/api/servlet/LoginServletTest.groovy`.
- **Dependências:** T05, T06.
- **Passos:** registrar três caminhos de login; receber somente credenciais de entrada; devolver `{id,tipo}` e renovar sessão nativa; implementar recuperação de ator e verificação de atividade no servidor para reutilização nas próximas servlets. Impedir que ID/tipo de payload selecionem o titular.
- **Validação:** testes de servlet com mocks para três perfis, senha inválida, conta inativa, renovação de sessão e ausência de credenciais no JSON; integração HTTP real em T18.
- **Conclusão:** login correto cria contexto de servidor; falhas não criam sessão válida e conta inativada não pode continuar operando.

**Estado da T07 em 08/11/2026: concluída.** Registrados somente os três caminhos `POST /login/candidato`, `/login/empresa` e `/login/recrutador`. `LoginServlet` recebe exclusivamente strings `email`/`senha`, rejeita campos adicionais (inclusive ID/tipo), exige JSON e a origem `http://127.0.0.1:5500`, delega ao controller do perfil da rota e responde `200` somente com `{id,tipo}`. Credenciais inválidas/inatividade produzem `401` genérico; formato inválido, origem divergente e falha inesperada produzem respectivamente `400`, `403` e `500`, sem detalhes internos. Nenhuma entidade é serializada na resposta de login.

`SessaoHttp` invalida a sessão anterior e cria uma nova sessão nativa contendo somente ID/tipo imutáveis. A recuperação do ator consulta novamente a atividade pelo ID persistido, sem ler identidade de payload, cabeçalho ou armazenamento frontend; conta ausente/inativa ou contexto inválido é rejeitado e sua sessão invalidada. Controllers/serviços dos três perfis oferecem essa consulta, e `ControllerConfig` compõe uma instância reutilizável do contexto. Acrescentados ao contrato/DAO de recrutador a busca ativa por e-mail com SQL parametrizado e ao serviço/controller o login com `SenhaService`, sem fallback para texto simples. Candidato/empresa reutilizam o login seguro de T05. Cookie configurado no contexto Tomcat com `HttpOnly` e `SameSite=Lax`.

**Arquivos:** criados `B/api/servlet/LoginServlet.groovy`, `B/api/SessaoHttp.groovy` e `T/api/servlet/LoginServletTest.groovy`. Alterados `B/config/{ApiServletConfig,ControllerConfig}.groovy`, os controllers/serviços dos três perfis, `B/dao/contratos/RecrutadorRepository.groovy`, `B/dao/recrutador/RecrutadorDAO.groovy`, `T/service/RecrutadorServiceTest.groovy` e os testes JDBC existentes `T/integracao/{SenhaPersistenciaTest,UnicidadeContaTest}.groovy`. Documentação alterada somente neste plano e em `CHANGELOG.md`.

**Segurança do banco antes da execução:** auditados `SuportePostgres`, `SuporteContasPostgres`, setup/cleanup dos três testes JDBC e o script local de execução. Toda conexão exige configuração explícita `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER`, `LINKETINDER_TEST_DB_PASSWORD`, aceita somente `linketinder_test`/`linketinder_test_<sufixo>` sem parâmetros e confere `current_database()` antes de escrever; não há fallback para `PostgresConnectionFactory`. O DDL é exclusivamente da seção atual. Fixtures ficam em schema aleatório, sem `public` no `search_path`; limpeza por rollback ou exclusão somente do schema gerado/validado. A inativação do recrutador no teste altera apenas sua fixture. Scripts carregam somente as variáveis de teste do `.env`, sem editar o arquivo ou imprimir valores. Nenhum schema/dado de desenvolvimento, migração ou reset foi executado; o protocolo transacional de T06 permaneceu intacto e sua regressão real passou.

**Comandos e evidências:**

- `.\gradlew.bat test --tests 'com.jpcode.api.servlet.LoginServletTest' --tests 'com.jpcode.service.*'` — execução final bem-sucedida: **114 testes, zero falhas, erros ou ignorados**. Inclui 30 casos novos de servlet/contexto e cinco de login do recrutador. As primeiras tentativas revelaram erros na parametrização Spock, construtores das fixtures, stubs e asserção de mapa vazio; corrigidos antes da execução aprovada. O primeiro comando no sandbox foi bloqueado pelo cache `C:\.gradle`; repetido com autorização fora dele.
- `.\build\executar-t01.ps1` — build com banco dedicado passou antes do ajuste de cookie. `.\gradlew.bat build` após esse ajuste passou, mas sem configuração JDBC ignorou 115 casos; essa execução não foi contada como aprovação do banco. A repetição do script com ambiente configurado retornou `UP-TO-DATE`, por isso foi exigida reexecução efetiva.
- `.\build\executar-t07.ps1` — script temporário não versionado, derivado do script auditado de T01, executa `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01 --rerun-tasks` com banco dedicado: **BUILD SUCCESSFUL; 236 testes, zero falhas, erros ou ignorados**, confirmados nos XMLs. São 84 testes de serviços, 30 de servlet/contexto, 115 JDBC e sete de segurança do suporte. Compilação principal/testes, empacotamento e JaCoCo executados. Políticas de scripts do sandbox exigiram execução autorizada fora dele. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.
- `.\build\verificar-http-t07.ps1` — script temporário não versionado iniciou o `RestServer` real em diretório sob `build/` e encerrou somente o processo criado. Os três POSTs com origem permitida e corpo `{}` retornaram **400**; origem divergente retornou **403**. Nenhuma chamada chegou à persistência. A primeira tentativa revelou que configurar `SessionCookieConfig` nesse momento do ciclo do Tomcat impedia iniciar; corrigido para configuração nativa do contexto/cookie processor e repetido com sucesso. O sandbox bloqueou o script; a execução autorizada passou. Não equivale à integração completa de login/cookie/navegador prevista em T18.
- `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/verificar-composicao-t06.groovy` — reutilizado script local auditado: composição manual e serialização dos dois DTOs anônimos com igualdade exata das chaves **passaram**, sem acesso ao banco.
- Revisão do diff completo, incluindo os três arquivos novos, e `git diff --check` — escopo restrito à T07, sem erros de whitespace. Especificação, SQL, frontend, DTOs e consultas de descoberta/match preservados.

**Critérios conferidos:** login válido cria sessão com titular/perfil obtidos do servidor; falhas de credenciais/inatividade não criam sessão e invalidam a anterior; os três perfis têm atividade revalidada em cada recuperação do contexto, bloqueando continuidade após inativação. Testes JDBC confirmam atividade/ausência/inatividade dos três perfis, login real e rejeição de senha incorreta/inatividade do recrutador; a regressão mantém hashes, unicidades, rollback e concorrência anteriores. Respostas de sucesso/erro têm contratos exatos sem credenciais. A verificação de anonimato abrange preservação e serialização dos DTOs; não declara concluída a matriz HTTP futura.

**Limites e parada:** nenhum bloqueio de T07. Aplicação desse contexto às demais operações, restrições de recursos/perfis e CORS/OPTIONS continuam nas tarefas correspondentes, especialmente T11; login/cookies em HTTP integrado e navegador continuam em T18. Não foram executados testes frontend nem implementadas T08 ou seguintes. T07 não declara CA-03/CA-04/CA-18 integralmente aceitos no full-stack. Especificação e status das demais tarefas preservados. Nenhum `git add`, `git commit` ou `git push` executado; a autorização explícita deste pedido prevalece sobre o checkpoint de commit da skill. Consumo de tokens não disponível como medição confiável.

### T08 — Criar e consultar vagas por proprietário e autor

- **Objetivo:** implementar vagas diretas e de recrutador, com escopos separados.
- **Referência:** RN-02, RN-06; seção 3; CA-05, CA-06, CA-18, CA-25.
- **Camada:** Backend.
- **Existentes:** `B/model/core/Vaga.groovy`, `B/dto/vaga/{CadastrarVagaDTO,VagaEmpresaDTO,AtualizarVagaDTO}.groovy`, `B/dao/contratos/VagaRepository.groovy`, `B/dao/vaga/{VagaDAO,CompetenciasVagaDAO}.groovy`, `B/service/VagaService.groovy`, `B/controller/vaga/VagaController.groovy`, `B/config/ServiceConfig.groovy`, `T/service/VagaServiceTest.groovy`.
- **Novos:** `T/integracao/VagaAutoriaTest.groovy`.
- **Dependências:** T02, T06.
- **Passos:** ampliar entidade/DTO interno e mapeamento JDBC com autoria; novas operações derivam empresa do recrutador; validar conta ativa/vínculo e catálogos antes de gravar. Compartilhar transação entre vaga e competências para evitar vaga parcial. Consultar empresa por proprietária e recrutador por autor; JOIN histórico não exclui autor inativo. Impedir atualização existente de reescrever autoria/propriedade fora do escopo.
- **Validação:** unitários e JDBC com empresa, dois recrutadores da mesma empresa e um de empresa diferente; confirmar rollback por competência inexistente; vaga direta sem autor; inatividade mantém autoria.
- **Conclusão:** empresa vê todas as próprias vagas; cada recrutador ativo vê somente as próprias; não há criação com vínculos incoerentes.

**Estado da T08 em 08/11/2026: concluída e validada.** Autorização explícita do responsável para implementar e validar somente T08, sem commits. Branch `feature/recruiter`, inicialmente sem alterações. Aplicada a skill `skills/feature-com-spec/SKILL.md`; a autorização deste pedido prevalece sobre seu checkpoint de commit. Especificação aprovada preservada.

**Implementação:** `Vaga.idRecrutador` é opcional; o DTO interno acrescenta `idRecrutador`, `nomeRecrutador` e `areaRecrutamento`, sem dados privados do profissional. O cadastro direto força ausência de autor; o cadastro pelo recrutador recebe seu contexto separadamente, consulta o vínculo persistido e rejeita empresa informada divergente. Ambos validam atividade e resolvem todos os nomes de competências antes de persistir, removendo repetições. `VagaDAO.salvarComCompetencias` possui uma única conexão/transação, revalida empresa e autor com locks `FOR SHARE` na ordem empresa–recrutador e compartilha a conexão com `CompetenciasVagaDAO`, que não a fecha. Falha reverte vaga e associações; o ID da entidade só é atribuído após commit.

As consultas internas validam titular e atividade: empresa por proprietária, recrutador por autor, sem compartilhar vagas entre colegas. O JOIN empresarial conserva nome/área do autor inativo. Atualização existente valida empresa ativa/proprietária no serviço e no SQL e não escreve IDs de propriedade/autoria. A composição continua manual. DTOs de cadastro/atualização não precisaram de campos novos. O ajuste mínimo de `VagaServlet` troca a serialização da entidade ampliada por `VagaAnonimaDTO` no GET por ID; evita exposição da autoria nesta tarefa, sem registrar novas rotas ou implementar a matriz de sessão/CORS de T11.

**Arquivos:** modificados `B/model/core/Vaga.groovy`, `B/dto/vaga/VagaEmpresaDTO.groovy`, `B/dao/contratos/VagaRepository.groovy`, `B/dao/vaga/{VagaDAO,CompetenciasVagaDAO}.groovy`, `B/service/VagaService.groovy`, `B/controller/vaga/VagaController.groovy`, `B/config/{DaoConfig,ServiceConfig}.groovy`, `B/api/servlet/VagaServlet.groovy` e `T/service/VagaServiceTest.groovy`. Criados `T/integracao/VagaAutoriaTest.groovy` e `T/api/servlet/VagaServletTest.groovy`. Documentação limitada a este plano e `CHANGELOG.md`. SQL, frontend, especificação e demais tarefas preservados.

**Segurança do banco antes das escritas:** inspecionados `SuportePostgres`, `SuporteContasPostgres`, setup/cleanup dos testes JDBC existentes/novo e scripts locais de execução. Cada conexão exige as três variáveis `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER`, `LINKETINDER_TEST_DB_PASSWORD`, restringe o nome a `linketinder_test`/`linketinder_test_<sufixo>` sem parâmetros e confere `current_database()` antes de escrever. Não há fallback para `PostgresConnectionFactory`. DDL exclusivamente atual, schemas aleatórios próprios sem `public` no `search_path`, limpeza por rollback ou DROP apenas do schema gerado e validado. Fixtures novas incluem duas empresas, dois recrutadores de uma e outro da segunda, duas áreas e competências de RH/administração. Os scripts carregam somente variáveis de teste do `.env`, sem imprimir valores ou editar o arquivo. Nenhuma escrita no banco de desenvolvimento, migração ou reset.

**Evidências executadas:**

- `.\gradlew.bat test --tests 'com.jpcode.service.VagaServiceTest'` — **21 testes, zero falhas/erros/ignorados**. A tentativa no sandbox foi bloqueada pelo cache Gradle; repetição autorizada fora dele passou.
- `.\build\executar-t08-tests.ps1` — script temporário não versionado que carrega o ambiente dedicado e executa `.\gradlew.bat test --tests 'com.jpcode.service.VagaServiceTest' --tests 'com.jpcode.integracao.VagaAutoriaTest' --tests 'com.jpcode.api.servlet.VagaServletTest' --rerun-tasks`: **BUILD SUCCESSFUL, 33 testes, zero falhas/erros/ignorados**, conferidos nos XMLs. São 21 unitários de serviço, 11 JDBC reais e um teste de servlet com mocks. Compilação principal/testes e JaCoCo executados.
- Duas tentativas anteriores de `.\build\executar-t07.ps1` executaram 255 testes e falharam, respectivamente, em quatro fixtures de competência e três consultas de autoria. Corrigidas as fixtures para a representação em maiúsculas exigida pelo DAO existente e a leitura JDBC de autor anulável (`getLong`/`wasNull`, pois `getObject(..., Long)` não aceita `int4` neste driver). Não se contam essas tentativas como aprovação. Inspeção local do driver por `javap` confirmou a restrição; repetição autorizada após bloqueio do cache concluiu o diagnóstico.
- `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/verificar-composicao-t06.groovy` — composição manual e igualdade exata das chaves dos DTOs anônimos aprovadas, sem conexão ao banco.
- Regressão final por `.\build\executar-t07.ps1`, script local auditado que executa `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01 --rerun-tasks` com ambiente dedicado: **BUILD SUCCESSFUL, 255 testes, zero falhas, erros ou ignorados**, conferidos nos 15 XMLs. São 91 testes de serviços, 31 de servlet/contexto, 126 JDBC reais e sete de segurança do suporte. Compilação principal/testes, JAR, verificações e JaCoCo executados; regressão de T01–T07 preservada. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.
- Diff integral revisado, incluindo os dois arquivos novos, e `git diff --check` sem erros de whitespace. `git diff --exit-code -- docs/specs/recrutador/spec.md queriesLinketinder.sql frontend` confirmou esses arquivos preservados; índice Git permaneceu vazio.

**Critérios de T08 e rastreabilidade:**

| Parcela do critério | Evidência observada |
| --- | --- |
| CA-05: vaga direta sem autor, autor e empresa corretos | Unitários verificam derivação e rejeição de empresa injetada; JDBC confirma IDs persistidos, NULL na vaga direta e rejeita vínculo entre empresas diferentes/contas inexistentes |
| CA-06: empresa vê todas as próprias; recrutador somente as próprias | JDBC com duas empresas e três recrutadores confirma conjuntos exatos; unitários rejeitam titular alheio antes da consulta |
| CA-18: autoria histórica e bloqueio de operação inativa | JDBC mantém nome/área/ID do autor inativo na consulta empresarial e permite atualização empresarial sem remover autoria; serviço rejeita criação/consulta do recrutador inativo e DAO revalida atividade |
| CA-25: competências N:N na criação de vagas | JDBC confirma duas associações por vaga, incluindo RH/administração, sem duplicar a seleção repetida; competência inexistente não cria vaga |
| Atomicidade e atualização restrita | Falha FK na segunda associação deixa zero vagas/associações e ID nulo; nova transação válida confirma duas associações. UPDATE de outra empresa falha e não altera a vaga; atualização válida mantém proprietário/autor |
| Anonimato e compatibilidade | Serialização JDBC da descoberta e teste de servlet por ID exigem exatamente `id`, `descricao`, `competencias`; composição manual e compilação dos consumidores existentes aprovadas |

**Conclusão e limites:** todos os critérios específicos de T08 verificados, sem bloqueios pendentes da tarefa. Interessado/retribuição/match de CA-06/CA-18, formulários/seletor de CA-25 e autenticação das operações por sessão HTTP dependem das tarefas posteriores e não são declarados aceitos integralmente. Os novos métodos recebem o contexto explícito do titular; sua ligação às rotas protegidas permanece em T11. Teste de servlet usa mocks, sem equivaler a integração HTTP real. Não foram executados navegador ou frontend. Nenhuma implementação de T09 ou seguintes, `git add`, `git commit` ou `git push`. Consumo de tokens não disponível como medição confiável.

### T09 — Tornar interesse, retribuição e match consistentes

- **Objetivo:** substituir curtida empresarial global pelo contexto candidato–vaga com atomicidade.
- **Referência:** RN-03 a RN-06; CA-07 a CA-12, CA-18.
- **Camada:** Backend.
- **Existentes:** `B/facade/MatchFacade.groovy`, `B/service/{EmpresaService,VagaService,MatchService}.groovy`, `B/controller/match/MatchController.groovy`, `B/dao/relacionamento/{CandidatoCurtirDAO,EmpresaCurtirDAO}.groovy`, `B/dao/match/MatchDAO.groovy`, `B/dao/vaga/VagaDAO.groovy`, `B/dao/contratos/{CurtidaRepository,MatchRepository,CandidatoCurtidasConsultaRepository}.groovy`, `B/config/{DaoConfig,ServiceConfig,ControllerConfig}.groovy`, `T/service/{EmpresaServiceTest,VagaServiceTest,MatchServiceTest}.groovy`.
- **Novos:** `B/dao/contratos/RetribuicaoRepository.groovy` (contrato específico com contexto de vaga; não ampliar o genérico de duas pontas indevidamente), `T/facade/MatchFacadeTest.groovy`, `T/integracao/RetribuicaoMatchTest.groovy`.
- **Dependências:** T04, T06, T08.
- **Passos:** preservar interesse inicial sem match; validar ator ativo, proprietária/autoria e interesse prévio. Aplicar 3.3 na fachada, com conexão única e APIs transacionais nos DAOs. Impedir gravação direta de match de contornar as FKs/regras; adaptar alvos de `ON CONFLICT`. Proteger consulta de interessados por escopo. Ajustar exclusão JDBC já existente somente para ordenar dependências novas sem mudar cascades ou criar UI.
- **Validação:** unitários com asserção de ausência de escrita nas rejeições; JDBC com duas vagas, retribuições dos dois perfis, mesma interação concorrente, falha forçada entre INSERTs e rollback integral. Repetir não aumenta contagens. Testar a exclusão de fixture de vaga respeitando as FKs.
- **Conclusão:** nenhum caminho cria match sem reciprocidade ou em vaga alheia; interesse é único para ambos os destinatários e a falha não deixa retribuição parcial.

**Estado da T09 em 08/11/2026: concluída e validada.** Branch `feature/recruiter`, inicialmente limpa. Revisadas as entregas T01–T08 e aplicadas as instruções de `AGENTS.md` e `skills/feature-com-spec/SKILL.md`. A autorização explícita para implementar somente T09 prevalece sobre o checkpoint de commit da skill; nenhum comando de staging, commit ou push foi executado.

**Implementação:** `MatchFacade` possui a conexão e coordena a transação `READ COMMITTED`. As operações `retribuirInteresseEmpresa` e `retribuirInteresseRecrutador` distinguem explicitamente o ator, validam atividade, propriedade/autoria e interesse naquela vaga e gravam retribuição/match na mesma conexão. Ordem de locks documentada e comum aos dois caminhos: empresa, recrutador operante quando houver, candidato, vaga e interesse. `FOR SHARE` protege atividade/vínculos; `FOR UPDATE` da vaga serializa retribuições e exclusão. A leitura preliminar da proprietária é revalidada após o lock da vaga. Erros revertem a transação e fecham a conexão possuída pela fachada.

`RetribuicaoRepository` substitui o uso indevido do contrato genérico de duas pontas na retribuição. Os DAOs participantes recebem a conexão, exigem transação explícita nas escritas e não a confirmam nem fecham. Os dois INSERTs usam conflito por candidato/vaga e verificam a empresa do registro persistido após repetição. O primeiro ator confirmado é conservado; autoria permanece exclusivamente na vaga. O interesse inicial mantém sua unicidade, revalida candidato/empresa ativos e vaga na própria transação e nunca grava match. As consultas `buscarInteressadosEmpresa` e `buscarInteressadosRecrutador` validam o mesmo escopo antes de retornar somente `CandidatoAnonimoDTO`. Recrutador inativo não opera; a empresa continua operando sem exigir atividade do autor histórico.

As assinaturas antigas necessárias à compilação permanecem: `curtirCandidato` da fachada/controller delega à operação empresarial contextualizada. A curtida global de `EmpresaService`, sua consulta de interessados sem ator e a gravação direta via `MatchController.salvar`/`MatchService.salvar` falham sem escrita. O terminal não foi adaptado; seu caminho antigo de consulta sem ator passa a ser rejeitado, conforme a restrição de não conservar caminhos inseguros. A consulta legada de retribuições empresariais continua anônima e elimina repetições decorrentes de múltiplas vagas. `VagaDAO.deletar` bloqueia a vaga e remove match, retribuição e vaga, nessa ordem, na mesma transação; cascades existentes cuidam de interesses/competências e não foram alterados.

**Arquivos criados:** `B/dao/contratos/RetribuicaoRepository.groovy`, `T/facade/MatchFacadeTest.groovy` e `T/integracao/RetribuicaoMatchTest.groovy`.

**Arquivos modificados:** `B/config/{ServiceConfig,ControllerConfig}.groovy`, `B/controller/match/MatchController.groovy`, `B/dao/contratos/{MatchRepository,CandidatoCurtidasConsultaRepository}.groovy`, `B/dao/relacionamento/{CandidatoCurtirDAO,EmpresaCurtirDAO}.groovy`, `B/dao/match/MatchDAO.groovy`, `B/dao/vaga/VagaDAO.groovy`, `B/facade/MatchFacade.groovy`, `B/service/{EmpresaService,MatchService}.groovy`, `T/service/{EmpresaServiceTest,MatchServiceTest}.groovy`, este plano e `CHANGELOG.md`. `VagaService.curtir` já delega ao DAO de interesse e não precisou de mudança; seu teste existente foi executado na regressão. `DaoConfig` e `CurtidaRepository` também não precisaram de alteração.

**Segurança do banco antes das escritas:** inspecionados `SuportePostgres`, `SuporteContasPostgres`, setup/cleanup dos testes e os scripts locais de execução. Toda conexão de teste exige `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD`, restringe a URL sem parâmetros ao banco `linketinder_test`/`linketinder_test_<sufixo>` e verifica `current_database()` antes das escritas. Não há fallback para `PostgresConnectionFactory`. Somente o DDL atual é aplicado em schema aleatório sem `public` no `search_path`. Cleanup usa rollback ou DROP exclusivamente do schema gerado, validado por padrão restrito; executores terminam antes da limpeza, com timeouts limitados. As mutações e a tabela que força falha na exclusão pertencem somente às fixtures. Scripts carregam variáveis de teste do `.env` apenas no processo, sem editar o arquivo ou imprimir valores. Nenhuma migração, reset ou escrita no banco de desenvolvimento foi executada.

**Comandos e resultados executados:**

- `.\gradlew.bat compileGroovy compileTestGroovy` — sandbox bloqueou o cache Gradle; repetição autorizada fora dele: **BUILD SUCCESSFUL**, compilação principal e de testes concluídas.
- `.\build\executar-t01.ps1` — sandbox bloqueou a política de scripts; execução autorizada com banco dedicado executou **291 testes, com 10 falhas** nos testes novos. Nove vieram de stubs que não substituíam a preparação inicial; uma, do encapsulamento de exceção JDBC pelo proxy do mock. Corrigida a preparação dos stubs e usada implementação JDBC real para injetar a falha. Essa execução não é contada como aprovação.
- `.\build\executar-t09-tests.ps1` — script temporário não versionado, derivado do carregador auditado, executou `.\gradlew.bat test --tests 'com.jpcode.facade.MatchFacadeTest' --tests 'com.jpcode.integracao.RetribuicaoMatchTest' --tests 'com.jpcode.service.EmpresaServiceTest' --tests 'com.jpcode.service.MatchServiceTest' --rerun-tasks`: **BUILD SUCCESSFUL, 62 testes, zero falhas, erros ou ignorados**, conferidos nos XMLs. Inclui 15 casos de fachada, 28 JDBC e 19 dos serviços abrangidos.
- `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/verificar-composicao-t06.groovy` — composição manual e igualdade exata das chaves dos dois DTOs anônimos **passaram**, sem abrir banco ou iniciar servidor.
- `.\build\executar-t07.ps1` — regressão final autorizada, com ambiente dedicado, executou `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01 --rerun-tasks`: **BUILD SUCCESSFUL, 300 testes, zero falhas, erros ou ignorados**, conferidos nos 17 XMLs. São 91 testes de serviços, 31 de servlet/contexto, 15 de fachada, 156 JDBC reais e sete de segurança do suporte. Inclui os dois casos finais de contexto divergente e interesse em outra vaga, totalizando **30 casos novos JDBC de T09**, além de compilação, JAR e JaCoCo.
- Revisão integral do diff, incluindo arquivos novos, e `git diff --check` — sem erros de whitespace. `git diff --exit-code -- docs/specs/recrutador/spec.md queriesLinketinder.sql frontend` — especificação, schema e frontend preservados. `git diff --cached --stat` — índice sem alterações. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.

**Verificação dos critérios de T09:**

| Critério/parcela dos requisitos | Evidência executada |
| --- | --- |
| CA-07/CA-08: interesse único para destinatários, sem match inicial | Repetição sequencial/concorrente mantém uma linha; empresa e autor consultam o mesmo candidato, vaga direta não é liberada ao recrutador; zero retribuições/matches antes da reciprocidade |
| CA-09/CA-10: retribuições válidas e isolamento por vaga | Empresa retribui em vaga direta/autoral; autor retribui na própria; candidato com interesse em duas vagas recebe match somente na retribuída; interesse apenas na primeira não autoriza retribuição na segunda |
| CA-11 e autorização: rejeições sem escrita | Unitários e JDBC rejeitam empresa alheia/inativa, colega da mesma empresa, recrutador de outra empresa/inativo, candidato inativo e ausência de interesse; consulta alheia é rejeitada inclusive após match |
| CA-12: repetição e concorrência | Duas threads coordenadas, conexões/transações distintas e timeout limitado confirmam uma retribuição e um match; repetição pelos dois atores conserva o primeiro ator; empresa divergente é rejeitada nos dois DAOs mesmo após conflito |
| Atomicidade e posse de recursos | Falha FK real no segundo INSERT reverte a retribuição, mantém interesse e permite repetir com sucesso; DAOs não fecham nem confirmam conexão emprestada e rejeitam autocommit; unitários verificam mesma conexão e ausência de commit nas rejeições |
| Locks e revalidação | Transação pausada após INSERT impede, em outra conexão, inativar empresa/recrutador/candidato, alterar autoria ou excluir interesse (`55P03`); dados parciais não aparecem externamente e a liberação permite confirmar o match |
| CA-18: continuidade empresarial e autoria histórica | Empresa recebe interessados e forma match com autor inativo, cuja identificação interna permanece; consultas/retribuições do autor inativo falham |
| Exclusão e integridade referencial | Exclusão JDBC remove somente dependências da vaga escolhida; FK adicional de fixture força falha e restaura vaga, competências, interesse, retribuição e match por rollback; INSERT direto sem reciprocidade é rejeitado por FK |
| Anonimato/regressão | JSON de interessados e vagas curtidas exige exatamente `id`, `descricao`, `competencias`; match de outra vaga não amplia essas chaves; regressão de T01–T08 e composição manual passaram |

**Conclusão e limites:** critérios específicos de T09 verificados, sem pendência ou bloqueio da tarefa. Contexto do ator é recebido explicitamente pelo backend; a ligação às sessões/rotas de curtidas permanece em T11. Não foram criadas rotas, executados testes HTTP reais novos, navegador ou frontend, nem implementados os contratos/consultas pós-match de T10. O aceite full-stack dos CA compartilhados continua nas tarefas posteriores. Somente o changelog e esta tarefa/status/checklist foram atualizados na documentação; demais tarefas e especificação preservadas. Nenhum `git add`, `git commit` ou `git push`. Consumo de tokens não disponível como medição confiável.

### T10 — Consultar matches e fechar caminhos de exposição indevida

- **Objetivo:** cumprir integralmente a matriz de visibilidade no backend.
- **Referência:** RN-04 a RN-06; seção 6; CA-06, CA-13 a CA-18.
- **Camada:** Backend.
- **Existentes:** `B/dto/Match/MatchEncontradoDTO.groovy`, `B/dao/match/MatchDAO.groovy`, `B/dao/contratos/MatchRepository.groovy`, `B/service/MatchService.groovy`, `B/controller/match/MatchController.groovy`, `B/api/servlet/{CandidatoServlet,EmpresaServlet,VagaServlet}.groovy`, `B/dao/relacionamento/CandidatoCurtirDAO.groovy`, testes de serviço correspondentes. Preservar sem ampliar `B/dto/{vaga/VagaAnonimaDTO,candidato/CandidatoAnonimoDTO}.groovy`.
- **Novos:** `T/api/servlet/VisibilidadeServletTest.groovy`, `T/integracao/ConsultaMatchTest.groovy`.
- **Dependências:** T07, T08, T09.
- **Passos:** acrescentar apenas nome/área do autor no contrato de match; JOIN opcional via vaga sem filtrar autor histórico; consulta recrutador filtra autoria, empresa filtra propriedade e candidato filtra titularidade. Corrigir chamada `buscar` para `buscarPorId` na consulta empresarial. Restringir perfil por ID ao próprio titular; bloquear listagem identificável pública e respostas de entidade de vaga. Manter descoberta/curtidas sempre anônimas, mesmo com match em outra vaga.
- **Validação:** comparar chaves exatas dos JSONs anônimos e ausência de credenciais em todos os retornos; testar perfis/IDs alternativos, outro recrutador da mesma empresa e autor inativo. Comparar todos os campos atuais de match mais os dois novos campos permitidos.
- **Conclusão:** a matriz da seção 6 vale na resposta HTTP e na consulta; match em A não libera contexto B.

**Estado da T10 em 08/11/2026: concluída e validada no escopo backend.** Branch `feature/recruiter`, checkout inicialmente limpo; dependências T07–T09 e entregas T01–T06 conferidas no código, contratos, testes e DDL atual. Aplicada a skill `skills/feature-com-spec/SKILL.md`; a autorização explícita deste pedido permite implementar T10 sem executar o checkpoint de commit. Especificação aprovada preservada.

**Implementação:** `MatchEncontradoDTO` conserva todos os campos anteriores e acrescenta somente `nomeRecrutador` e `areaRecrutamento`, anuláveis para vaga direta. O DAO compartilha o SELECT/mapeamento entre as três consultas de ator e obtém os dois campos por LEFT JOIN da autoria da vaga, sem exigir atividade do autor histórico. A consulta empresarial usa `buscarPorId` para competências do candidato. O filtro empresarial usa propriedade; o de candidato usa titularidade; o de recrutador usa autoria e atividade. A atividade do observador é exigida sem suprimir histórico de contrapartes inativas.

Serviço e controller expõem `consultarMatchesEmpresa`, `consultarMatchesCandidato` e `consultarMatchesRecrutador` com IDs operante/titular explícitos. O serviço rejeita IDs divergentes, titulares ausentes/inativos e recrutador com empresa inativa antes da consulta. Métodos legados de consulta delegam à validação de atividade; o construtor usado exclusivamente para gravação transacional continua disponível e não permite consulta sem os validadores. `ServiceConfig` injeta os repositórios existentes, preservando composição manual e a transação de T09.

`CandidatoServlet` e `EmpresaServlet` recebem `SessaoHttp` na composição existente. GET por ID exige sessão revalidada, perfil correspondente e ID próprio; listagens identificáveis respondem `403` sem consultar perfis. Ausência de sessão/conta inativa responde `401`, perfil/ID alheio `403`, ID inválido autenticado `400` e falha inesperada retorna erro genérico. A mesma guarda é aplicada ao PUT existente do perfil para não deixar um caminho alternativo sobre o ID protegido, sem ampliar operações ou contratos. O GET anônimo de vaga já corrigido em T08 e os DTOs anônimos não precisaram de alteração. Não foram criadas rotas de match/curtidas/recrutador nem implementada T11.

**Arquivos criados:** `T/api/servlet/VisibilidadeServletTest.groovy` e `T/integracao/ConsultaMatchTest.groovy`. **Modificados:** DTO, DAO, contrato, serviço e controller de match; `ServiceConfig`, `ApiServletConfig`, servlets de candidato/empresa e `T/service/MatchServiceTest.groovy`, além deste plano e `CHANGELOG.md`. Sem alteração de schema, frontend, dependências ou menus de terminal.

**Segurança do banco antes das escritas:** inspecionados setup/cleanup e todos os pontos de conexão da suíte JDBC. `SuportePostgres` exige as três variáveis `LINKETINDER_TEST_*`, rejeita configuração parcial, URL com parâmetros e bancos fora de `linketinder_test`/`linketinder_test_<sufixo>`, e confere `current_database()` em cada conexão. Não há fallback para a conexão de desenvolvimento. `SuporteContasPostgres` aplica somente o DDL atual e fixtures próprias em schema aleatório sem `public` no `search_path`; cleanup remove apenas esse schema com nome validado. Demais testes usam esse suporte ou rollback do schema transacional de `SuportePostgres`. O carregador local lê somente configuração de teste do `.env`, sem alterá-lo nem imprimir valores. Nenhuma migração, reset ou escrita de desenvolvimento foi executada.

**Comandos e resultados executados:**

- `.\gradlew.bat test --tests 'com.jpcode.service.MatchServiceTest' --tests 'com.jpcode.api.servlet.VisibilidadeServletTest'` — primeira tentativa bloqueada pelo cache Gradle no sandbox; repetição autorizada fora dele: **BUILD SUCCESSFUL, 50 testes, zero falhas, erros ou ignorados** (17 de serviço e 33 de servlet), confirmados nos XMLs. Compilação principal/de testes e JaCoCo concluídos.
- `.\build\executar-t07.ps1` — script local temporário já existente e auditado, inicialmente bloqueado pela política de scripts no sandbox. Repetição autorizada carregou apenas o ambiente dedicado e executou `.\gradlew.bat build --init-script .\build\classpath-t01.gradle exportarClasspathT01 --rerun-tasks`: **BUILD SUCCESSFUL, 356 testes, zero falhas, erros ou ignorados**, confirmados nos 19 XMLs. Inclui 105 testes de serviços, 64 de servlet/contexto, 15 de fachada, 165 JDBC reais e sete de segurança do suporte. São 56 casos adicionais sobre os 300 de T09: 14 de serviço, 33 de servlet e nove JDBC. Compilação, JAR e relatório JaCoCo concluídos.
- `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/verificar-http-t10.groovy` — script temporário não versionado iniciou Tomcat em porta efêmera com `ApiServletConfig` real. **Seis checagens HTTP passaram:** GET das duas listagens retorna `403`; GET e PUT por ID dos dois perfis sem sessão retornam `401`; respostas têm JSON com somente `erro` e mensagem esperada. Abertura de conexão de desenvolvimento foi substituída por falha explícita durante esse smoke, com contador final de **zero conexões**. Tomcat encerrado em `finally`. Não é teste de login/jornada autenticada em navegador.
- Revisão integral do diff e dos arquivos novos; `git diff --check` sem erros. `git diff --exit-code -- docs/specs/recrutador/spec.md queriesLinketinder.sql frontend` sem alterações; `git diff --cached --stat` vazio. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.

**Verificação dos critérios específicos de T10:**

| Critério/parcela dos requisitos | Evidência executada |
| --- | --- |
| CA-06/CA-14: conjunto por titular, propriedade ou autoria | JDBC com duas empresas, três recrutadores, duas pessoas candidatas e quatro vagas confirma conjuntos exatos; serviço rejeita ID alheio e titular ausente/inativo; outro recrutador da mesma empresa não recebe o candidato do match alheio |
| CA-13/CA-16: contrato completo e autoria correta | Comparação exata de todos os campos/valores do JSON de match autoral, incluindo competências e localização; retribuição empresarial apresenta nome/área do autor; vaga direta conserva as 21 chaves com ambos os campos novos nulos |
| CA-15: contratos anônimos e caminhos alternativos | JSON de descoberta por lista/ID, vagas curtidas e interessados exige somente `id`, `descricao`, `competencias`; perfis/listagens alternativos são bloqueados e perfil próprio conserva lista exata de campos sem credenciais |
| CA-17/RN-04: match em A não libera B | JDBC mantém a pessoa candidata anônima entre interessados de B; recrutador de B só identifica a outra pessoa candidata com match próprio em B; tentativa de consultar interessados de A é rejeitada; descoberta e curtidas permanecem anônimas |
| CA-18/RN-06: autoria histórica e conta inativa | Empresa forma e consulta match com autor inativo; nome/área históricos preservados; conta inativa não consulta; histórico de contrapartes inativas continua disponível aos titulares ativos |
| Credenciais, HTTP e regressão | Chaves exatas dos contratos de match/perfil/anônimos, erros genéricos, sessões ausentes/inativas e perfis/IDs incompatíveis testados; smoke HTTP real sem banco; regressão T01–T09, incluindo concorrência, rollback e integridade referencial, passou |

**Limites e parada:** nenhum bloqueio ou validação pendente de T10. A matriz foi verificada nas consultas reais, na serialização e nas servlets existentes; ligar os novos métodos de match às sessões/rotas e verificar a superfície HTTP completa pertence a T11. Navegador, frontend e aceite full-stack dos critérios compartilhados permanecem nas tarefas posteriores, sem declaração de conclusão antecipada. Nenhum `git add`, `git commit` ou `git push` executado. Consumo de tokens não disponível como medição confiável.

### T11 — Completar e verificar a superfície HTTP dos fluxos

- **Objetivo:** conectar capacidades de domínio às rotas de 3.5 e fornecer o contrato backend para os consumidores frontend; concluída no escopo HTTP.
- **Referência:** seção 8, “Capacidades HTTP necessárias”; seções 3, 6 e 7; CA-01 a CA-18, CA-25.
- **Camada:** Backend.
- **Existentes:** `B/api/servlet/{CandidatoServlet,EmpresaServlet,VagaServlet,CompetenciaServlet}.groovy`, `B/controller/referencia/ReferenciaController.groovy`, `B/service/ReferenciaService.groovy`, `B/dao/referencia/{PaisDAO,EstadoDAO}.groovy`, `B/config/{ApiServletConfig,ControllerConfig,TomcatConfig}.groovy`.
- **Novos:** `B/api/servlet/{RecrutadorServlet,AreaRecrutamentoServlet,ReferenciaServlet,CurtidaServlet,MatchServlet}.groovy`, `B/api/filter/CorsFilter.groovy`, `T/api/servlet/ContratoHttpTest.groovy`.
- **Dependências:** T06 a T10.
- **Passos:** registrar métodos/caminhos da tabela e rotear para controllers/serviços, sem SQL nas servlets; completar leitura de referências persistidas. Padronizar serialização JSON e erros sem vazamento; validar sessão/atividade/escopo em todo caminho protegido. Configurar CORS/preflight/cookies/origem de 3.4. Não registrar endpoints opcionais ou gravação isolada de match.
- **Validação:** testes de parsing, método/rota, campos inválidos, status e lista permitida de propriedades; mocks de servlet não substituem navegador real de T18. Verificar origem permitida, origem rejeitada e preflight.
- **Conclusão:** cada capacidade obrigatória possui rota e consumidor planejado compatíveis; nenhum endpoint antigo evita validação nova.

**Estado da T11 em 08/11/2026: concluída e validada no escopo HTTP backend.** Conferidos checkout inicialmente limpo em `feature/recruiter`, entregas T01–T10, contratos reais, composição manual, DDL atual e suportes de teste. Aplicada a skill `skills/feature-com-spec/SKILL.md`; a autorização explícita deste pedido prevalece sobre o checkpoint de commit. Nenhum `git add`, `git commit` ou `git push` executado.

**Implementação:** registradas as capacidades obrigatórias de 3.5: cadastro empresarial e perfil próprio de recrutador, áreas, países/estados, criação autoral e consultas internas de vagas, interesse inicial/curtidas, interessados, retribuição e matches dos três perfis. As servlets usam controllers existentes, sem SQL ou nova regra de domínio. Países e estados têm leitura pelo contrato de persistência, DAO, serviço e controller; competências continuam `string[]`. `JsonServlet` concentra parsing, validação de campos, métodos, serialização e erros HTTP. A empresa encaminha somente o POST de cadastro de recrutador ao handler correspondente, preservando os demais caminhos empresariais.

O ator vem exclusivamente de `SessaoHttp`, com revalidação de atividade. IDs delimitam recursos e titularidade; propriedade/autoria e reciprocidade continuam verificadas pelos serviços/DAOs de T08–T10. GET de descoberta de vagas exige candidato ativo e mantém o DTO anônimo; POST empresarial e PUT de vaga exigem empresa ativa, derivam seu ID da sessão e impedem injeção de autoria ou propriedade alheia. POSTs em subcaminhos de cadastro não criam contas. Perfis próprios e listagens mantêm as restrições de T10. Nenhum endpoint opcional de recrutador ou gravação direta de match foi disponibilizado.

`CorsFilter`, registrado no contexto existente, permite somente `http://127.0.0.1:5500` com credenciais, responde preflight sem exigir sessão e valida origem e conteúdo JSON nas mutações. Cookies preservam HttpOnly/SameSite=Lax, conferidos em HTTP real. Códigos compatíveis de sucesso permanecem `200`/`201`/`204`. Formato/campos/referências inválidos retornam `400`, sessão ausente/inativa `401`, escopo incompatível `403`, recurso/rota inexistente `404`, conflitos de identificadores/estado da retribuição `409` e falhas inesperadas `500` genérico; método não permitido retorna `405`. Ajustados erros antigos de cadastro/atualização e o erro empresarial antes escrito como representação de Map, agora JSON válido. Erros não reproduzem mensagens SQL ou dados enviados pelo cliente.

**Arquivos criados:** `B/api/filter/CorsFilter.groovy`; `B/api/servlet/{JsonServlet,RecrutadorServlet,AreaRecrutamentoServlet,ReferenciaServlet,CurtidaServlet,MatchServlet}.groovy`; `T/api/servlet/ContratoHttpTest.groovy`; `T/integracao/ContratoHttpPostgresTest.groovy`. **Modificados:** `B/api/servlet/{CandidatoServlet,EmpresaServlet,VagaServlet,CompetenciaServlet,LoginServlet}.groovy`, `B/config/ApiServletConfig.groovy`, `B/controller/referencia/ReferenciaController.groovy`, `B/service/ReferenciaService.groovy`, `B/dao/contratos/ReferenciaRepository.groovy`, `B/dao/referencia/{PaisDAO,EstadoDAO}.groovy`, `T/api/servlet/{VagaServletTest,VisibilidadeServletTest}.groovy`, `CHANGELOG.md` e este plano. Os dois testes anteriores recebem o contexto de candidato agora obrigatório. Sem mudanças no frontend, schema, DTOs anônimos, dependências ou terminal.

**Segurança do banco antes das escritas:** auditados todos os pontos de conexão e setup/cleanup da suíte. `SuportePostgres` exige URL/usuário/senha de teste, rejeita configuração parcial, parâmetros JDBC e bancos fora de `linketinder_test`/`linketinder_test_<sufixo>`, e confere `current_database()` em cada conexão. `SuporteContasPostgres` cria schema aleatório, usa apenas esse schema no `search_path` e limita `DROP SCHEMA` ao nome criado/validado. Os demais casos usam esse suporte ou rollback de schema transacional. A jornada HTTP injeta explicitamente todos os DAOs reais e a factory dedicada na composição de teste; nenhum caminho de conexão faz fallback ao desenvolvimento. O construtor de `DaoConfig` não abre conexão. Preparação, mutações de inatividade, contagens e limpeza usam exclusivamente a factory validada. Não foram executados exemplos SQL legados, migrações, resets ou escritas de desenvolvimento.

**Comandos e resultados executados:**

- `.\gradlew.bat compileGroovy compileTestGroovy` — bloqueado inicialmente pelo acesso ao cache Gradle no sandbox; repetição autorizada fora dele: **BUILD SUCCESSFUL**.
- `.\gradlew.bat test --tests 'com.jpcode.api.*'` — as rodadas iniciais identificaram construção incorreta de mocks/fixtures sem construtores e um PUT de teste sem JSON; corrigidos. A ampliação das rotas/testes também teve falhas de sintaxe Groovy/Spock corrigidas antes da validação final.
- `.\build\validar-t11.ps1` — carregador temporário local, ignorado pelo Git, que lê somente as três variáveis de teste do `.env`, valida a URL dedicada antes da execução e não imprime seus valores. Política de scripts bloqueou o sandbox; execução autorizada fora dele rodou `.\gradlew.bat test --tests 'com.jpcode.api.*' --tests 'com.jpcode.integracao.ContratoHttpPostgresTest'`: **BUILD SUCCESSFUL, 126 testes, zero falhas, erros ou ignorados**. Essa rodada precedeu seis casos adicionais de IDs inválidos e a ampliação da continuidade após inatividade, incluídos no build completo abaixo.
- `.\build\validar-t11.ps1 -Completo` — executou `.\gradlew.bat build --rerun-tasks` com a mesma configuração dedicada: **BUILD SUCCESSFUL, 424 testes, zero falhas, erros ou ignorados**, confirmados nos **21 XMLs** de `build/test-results/test/`. Inclui **67 novos casos HTTP** com controllers mockados e transporte/Tomcat/sessão reais, **uma nova jornada HTTP/JDBC real**, 64 testes anteriores de servlet, 105 de serviços, 15 de fachada, 165 JDBC anteriores e sete do suporte de segurança. Compilação, JAR e JaCoCo concluídos. Para reexecutar sem o carregador local, carregar explicitamente `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` no ambiente do processo e usar o comando Gradle correspondente; não publicar os valores.
- Revisão integral do diff, inclusive arquivos novos, e `git diff --check` — sem erros de whitespace; mudanças restritas à T11. Relatórios em `build/reports/tests/test/` e `build/reports/jacoco/test/`.

**Critérios específicos de T11 verificados:**

| Critério | Evidência observada |
| --- | --- |
| Parsing, método/rota, campos inválidos e códigos | HTTP real verifica corpos inválidos, IDs malformados, injeção de ator/empresa/autoria, métodos opcionais indisponíveis, rotas extras e erros genéricos; jornada real verifica `201`, `204`, `400`, `401`, `403` e `409`. |
| Propriedades permitidas e anonimato | Igualdade exata de chaves em descoberta por lista/ID, curtidas e interessados; perfil próprio de recrutador sem credenciais; contrato completo de match com somente nome/área do recrutador. Match em A mantém descoberta anônima e não libera o colega de B. |
| Sessão, atividade, titularidade e escopo | Ausência de sessão em cada superfície protegida; perfil/ID divergentes; empresa alheia; colega da mesma empresa; inativação após login e invalidação da sessão. Empresa continua consultando autoria histórica e forma novo match após inatividade do autor. |
| CORS, preflight e cookies | Origem explícita/credenciais, rejeição de origem divergente/ausente nas mutações, JSON obrigatório, preflight sem sessão e bloqueio de cabeçalhos não permitidos; cookies reais com HttpOnly/SameSite=Lax. |
| Rotas e consumidores planejados compatíveis | Catálogos mantêm nomes/siglas e formatos de 3.5; criação empresarial e autoral, perfil, interesse, retribuição e matches operam pela composição real. Curtida inicial não gera match, repetição não duplica e retribuição em outra vaga não ocorre sem interesse. |
| Integridade/transações preservadas | Jornada HTTP confirma N:N, ausência de vaga parcial com competência inválida, nenhuma retribuição/match nas falhas e unicidade após repetição. Regressão JDBC T01–T10 repete FKs, rollback, locks e concorrência; nenhuma transação foi transferida às servlets. |

**Limites e parada:** nenhum bloqueio ou validação pendente no escopo específico de T11. Os testes de transporte Java não executam um navegador; cookies/CORS no navegador e aceite full-stack dos CA compartilhados continuam em T18. Após a importação de master, a adaptação dos clientes/DTOs/serviços frontend existentes permanece para T12 e a extensão das jornadas/seletor para T13–T17; CA-25 não é declarado concluído no frontend. T12 e posteriores não implementadas, endpoints opcionais continuam excluídos, especificação aprovada e status das demais tarefas preservados. Consumo de tokens não disponível como medição confiável.

**Revisão da base frontend em 08/11/2026:** T11 continua concluída no escopo backend, com implementação e evidências acima preservadas. O frontend de master foi importado posteriormente e ainda usa contratos anteriores. Não repetir T11 nem modificar suas servlets/serviços/DAOs para acomodá-lo; a compatibilização é exclusivamente frontend em T12. O aceite em navegador continua em T18. As evidências históricas de HTTP/JDBC não comprovam o snapshot frontend importado.

### Frontend

### T12 — Compatibilizar a integração frontend existente com o backend de T11

- **Objetivo:** adaptar o frontend importado de master aos contratos já implementados, preservando sua estrutura e o backend de T01–T11.
- **Estado:** concluída e validada no escopo de transporte/contratos frontend em 08/11/2026, com evidências novas desta base. A implementação anterior permanece desconsiderada.
- **Referência:** seções 7 e 8; D-01, D-08; CA-01, CA-04, CA-15, CA-25; matriz de incompatibilidades da seção 2.
- **Camada:** exclusivamente frontend.
- **Existentes:** `F/ts/config/ServiceConfig.ts`; `F/ts/api/clientes/{CandidatoApiClient,EmpresaApiClient,VagaApiClient,CurtidaVagaApiClient,CurtidaCandidatoApiClient,MatchApiClient,ReferenciaApiClient,CompetenciaApiClient}.ts`; DTOs sob `api/dto/{candidato,empresa,vaga,match}/`; serviços HTTP correspondentes, incluindo `ReferenciaService`; estratégias/páginas consumidoras; modelos `Competencia`, `Pais` e `Estado`.
- **Novos:** `F/ts/api/clientes/{ApiClient,LoginApiClient,RecrutadorApiClient}.ts`; `F/ts/api/dto/login/SessaoResponseDTO.ts`; `F/ts/api/dto/recrutador/{CadastrarRecrutadorRequestDTO,RecrutadorViewDTO,AreaRecrutamentoResponseDTO}.ts`; `F/ts/services/RecrutadorService.ts`. Não criar duplicatas dos clientes/DTOs/serviços de candidato, empresa, match ou competências já presentes.
- **Dependências:** T11 concluída e snapshot de frontend importado/verificado.
- **Passos:** centralizar host/cookie/JSON/falhas no cliente base e adaptar todos os clientes e fetch diretos ativos, mantendo os serviços assíncronos e a composição existente. Consumir login com resposta real `{id,tipo}`; padronizar o tipo de sessão e seus consumidores (backend usa minúsculas), sem confiar no armazenamento como autorização. Tipar catálogo como `string[]`, corrigir os checkboxes/serviços consumidores sem inventar IDs e preservar objetos de competência nos DTOs de consulta. Revisar DTOs de perfil/datas, vaga interna (autoria) e match (somente nome/área adicionais do recrutador). Acrescentar contratos/serviços de recrutador e áreas, métodos específicos por ator e consultas de vagas/matches de autor. Corpos de curtida: somente `{idVaga}` ou `{idCandidato,idVaga}`; aceitar respostas sem corpo. Substituir `jaCurtiuVaga` por estado obtido da listagem anônima de vagas curtidas; eliminar chamadas ativas de verificação empresarial global, permitindo repetição idempotente por vaga. Não substituir a verificação ausente por leitura de perfil ou por comparação de nomes. Rastrear e impedir uso de `EmpresaCandidatoCurtido` com perfil alheio; os fluxos identificáveis usam `EmpresaMatch`. Adaptar consumidores necessários para compilar e aguardar sucesso, sem reconstruir telas já existentes.
- **Restrições:** não alterar backend, schema, endpoints ou configuração de segurança para aceitar o frontend antigo; não importar backend de master. Não implementar tela de recrutador/seletor nem remover indiscriminadamente código histórico. Dependência genuína do backend exige decisão do responsável, nunca fallback inseguro.
- **Validação:** `npx.cmd tsc --noEmit` e `npx.cmd gulp build`; conferir métodos/caminhos/campos contra servlets reais; checar fetch/credenciais e corpo/status, falha de rede, JSON inválido e sucesso vazio. Executar checagem reproduzível de transporte e, quando disponível, navegador com login candidato/empresa, perfil, descoberta, curtida e consulta de interessados/matches. Verificar ausência de chamadas ativas a `/verificar`, IDs de ator em corpos e buscas identificáveis alheias. Catálogo de RH/administração funciona como nomes nos formulários existentes. Qualquer teste que escreva exige auditoria prévia do banco dedicado; registrar limites concretos sem contar ausência de banco como aprovação.
- **Conclusão:** frontend e consumidores compilam; transporte e contratos correspondem a T11; nenhum caminho integrado depende de rotas inexistentes, confirmação local, validação empresarial global ou exposição de perfil alheio. Evidências novas pertencem a esta base; não reutilizar os resultados da T12 descartada.

**Entrega e rastreabilidade de T12 (08/11/2026):**

- Transporte compartilhado em `ApiClient`: API em `http://127.0.0.1:8080`, `credentials: "include"`, JSON de entrada, erros com status e mensagem backend ou fallback genérico; cadastros/curtidas não tentam ler JSON de sucesso vazio. Todos os clientes e as duas estratégias existentes passam por esse transporte. A origem frontend compatível continua `http://127.0.0.1:5500`, conforme T11.
- Login dos três perfis disponível no cliente com métodos por ator e resposta validada/restrita a `{id,tipo}`; estratégias e consumidores existentes usam os tipos minúsculos reais. O armazenamento continua somente estado de navegação. A entrada visual do recrutador pertence a T13.
- Contratos/serviço de recrutador e áreas, criação/consulta de vagas de autor e consulta de matches de autor acrescentados à composição existente. Retribuição possui métodos explícitos de empresa/recrutador, com o mesmo endpoint que deriva o ator da sessão.
- Interesse envia somente `{idVaga}`; retribuição somente `{idCandidato,idVaga}`. Descoberta consulta a lista anônima de vagas curtidas, sem verificações individuais inexistentes. A empresa não reconstrói retribuições por nomes/IDs de match: permite repetir por vaga e só confirma o card após sucesso HTTP. A entrada histórica `EmpresaCandidatoCurtido` delega a `EmpresaMatch`, sem ler perfil alheio.
- Catálogo seletivo é `string[]`; os dois formulários existentes renderizam e enviam nomes de RH/administração sem criar IDs de referências. Os identificadores DOM dos checkboxes são apenas locais. DTOs anônimos e competências de consulta `{id,nome}` foram preservados. A contagem do gráfico permanece por interesse em cada vaga.
- Vagas internas incluem ID/nome/área do autor anuláveis; matches acrescentam somente nome/área anuláveis, sem IDs artificiais de candidato/vaga ou perfil do recrutador. Perfis tipam `dataNascimento` como o objeto efetivamente serializado por `JsonOutput`; cadastros continuam ISO. O serviço empresarial adapta `localizacao → local` e envia nomes de competências, sem persistir `tipo`/`formacao`.

**Inventário dos arquivos de T12** (caminhos `F/ts` relativos a `frontend/src/ts`):

- Criados: `F/ts/api/clientes/{ApiClient,LoginApiClient,RecrutadorApiClient}.ts`; `F/ts/api/dto/login/SessaoResponseDTO.ts`; `F/ts/api/dto/recrutador/{CadastrarRecrutadorRequestDTO,RecrutadorViewDTO,AreaRecrutamentoResponseDTO}.ts`; `F/ts/services/RecrutadorService.ts`; `frontend/tests/transporte-t12.test.cjs`.
- Modificados: os oito clientes existentes listados no escopo de T12; `F/ts/api/dto/candidato/CandidatoViewDTO.ts`, `api/dto/vaga/{CadastrarVagaRequestDTO,VagaEmpresaResponseDTO}.ts` e `api/dto/match/MatchResponseDTO.ts`; `config/ServiceConfig.ts`; `services/{CompetenciaService,CurtidaCandidatoService,CurtidaVagaService,MatchService,VagaService}.ts`; `strategy/login/{LoginCandidatoStrategy,LoginEmpresaStrategy}.ts`; `pages/login/{LoginCandidato,LoginEmpresa}.ts`; `pages/candidato/{CadastroCandidatoPage,CandidatoPage,CandidatoVaga}.ts`; `pages/empresa/{EmpresaCandidato,EmpresaCandidatoCurtido,EmpresaPage,EmpresaVaga}.ts`; `CHANGELOG.md` e este plano.

**Comandos e resultados executados:**

| Verificação | Resultado e alcance |
| --- | --- |
| `npx.cmd tsc --noEmit` em `frontend` | Passou, inclusive na revisão final; TypeScript estrito em todos os consumidores. |
| `npx.cmd gulp build` em `frontend` | Passou; Webpack 5.110.3 compilou. Apenas aviso não impeditivo `DEP0180`. Saída gerada pelo build, sem edição manual de `dist`. |
| `node --test tests/transporte-t12.test.cjs` em `frontend` | 10 testes, 10 aprovados, zero falhas/ignorados na execução final. Executa clientes/serviços/estratégias e consumidores reais com fetch e DOM controlados, sem rede ou banco. Duas asserções inicialmente detectaram mensagens com acentuação danificada na escrita; corrigidas antes da execução final. |
| `.\gradlew.bat --gradle-user-home C:/Users/Mysterio/.gradle test --tests 'com.jpcode.api.*' --tests 'com.jpcode.service.*'` | `BUILD SUCCESSFUL`: 236 testes em 12 classes, zero falhas/erros/ignorados; relatório JaCoCo gerado. Inclui contratos HTTP em Tomcat real com controllers mockados, sessão, CORS, visibilidade e testes de serviços. Não seleciona `com.jpcode.integracao.*`. |
| Serialização com `groovy.ui.GroovyMain` e JARs declarados/cacheados `groovy`/`groovy-json` 5.0.8 | `JsonOutput.toJson(LocalDate.of(2000,1,2))` confirmou objeto com ano, mês, dia, calendário e demais propriedades tipadas no perfil; nenhuma conexão de banco. |
| Revisão integral do diff e `git diff --check` | Alterações limitadas a T12; sem erros de whitespace. Backend, SQL, build Gradle e especificação aprovada sem diff. |

O wrapper sem configuração explícita de cache falhou ao criar lock em `C:/.gradle` durante `--version`; a execução dos testes acima usou o cache local e permissão de execução concedida, concluindo normalmente. Tentativas iniciais de serialização selecionaram JARs de fontes; a checagem válida acima usou os JARs binários das versões declaradas.

**Critérios específicos verificados:**

| Critério de T12 | Evidência |
| --- | --- |
| Frontend e consumidores compilam e empacotam | TypeScript estrito e Gulp/Webpack aprovados. |
| Host/cookie, métodos/rotas/corpos e sucessos vazios correspondem a T11 | Servlets/registro/DTOs reais conferidos; testes de consultas, mutações e login pelos clientes/serviços compostos. |
| Falhas HTTP/rede/JSON não simulam sucesso | Testes de status 400/401/403/404/409/500, erros não JSON, rede indisponível, JSON de sucesso inválido e sessão inválida; estratégia não persiste nem navega em login rejeitado. |
| Nenhuma rota de verificação, identidade de ator no corpo ou reciprocidade global | Varredura do código e testes de descoberta por listagem, corpos exatos e mesmo candidato em duas vagas independentes. |
| Ausência de leitura identificável alheia e preservação do anonimato | Consumidor empresarial usa interessados anônimos e matches; entrada histórica delega ao componente de match. DTOs anônimos inalterados; regressão de visibilidade/backend aprovada. |
| Competências de catálogo como nomes e contratos internos/pós-match separados | Testes dos dois formulários com RH/administração, payload de vaga por nomes e gráfico por interesse; revisão dos DTOs de autoria/match/perfil contra produtores backend. |

**Banco, limites e parada:** nenhuma conexão/escrita de banco realizada. A suíte frontend usa fetch controlado; as classes backend selecionadas usam mocks, sem chamadas JDBC. O suporte `SuportePostgres` foi inspecionado e exige banco dedicado sem fallback, mas a suíte de integração persistida não foi selecionada nem executada. Não se declara nova validação JDBC de integridade/transações, que permanecem inalteradas. Não há ferramenta de navegador disponível nesta sessão; o DOM controlado não comprova cookies/CORS em navegador nem aceite full-stack. Esses aceites permanecem em T18, conforme T11 e os limites da tarefa; nenhum bloqueio no escopo específico de T12.

T13–T19 não implementadas nem marcadas como concluídas. O login automático herdado dos cadastros continua para adaptação em T13; não foi antecipado o seletor pesquisável nem telas/jornadas do recrutador. O arquivo preexistente não rastreado `docs/specs/recrutador/diagnostico-integracao.md` foi preservado. Especificação aprovada e demais tarefas preservadas; nenhum `git add`, `git commit` ou `git push` executado. A autorização explícita deste pedido prevalece sobre o checkpoint de commit da skill `feature-com-spec`. Consumo de tokens não disponível como medição confiável.

### T13 — Adaptar cadastros/login existentes e acrescentar entrada de recrutador

- **Objetivo:** completar a entrada dos três perfis aproveitando formulários e autenticação HTTP já existentes.
- **Estado:** implementação entregue em 09/11/2026; validação automatizada registrada abaixo. Conclusão pendente da verificação em navegador com backend e banco dedicado; checklist permanece desmarcado.
- **Referência:** seção 7; seção 8, “Interface”; RN-06 a RN-09; CA-03, CA-04, CA-19 a CA-22.
- **Camada:** Frontend.
- **Existentes:** `F/ts/strategy/login/{LoginStrategy,LoginCandidatoStrategy,LoginEmpresaStrategy}.ts`; `F/ts/pages/LoginPage.ts`; `F/ts/pages/login/{LoginCandidato,LoginEmpresa}.ts`; cadastros de candidato/empresa e `F/html/{index,cadastro-candidato,cadastro-empresa}.html`; `ReferenciaService`; contratos de T12.
- **Novos:** `F/ts/strategy/login/LoginRecrutadorStrategy.ts`, `F/ts/pages/login/LoginRecrutador.ts`.
- **Dependências:** T12; T14 completa a seleção pesquisável do candidato.
- **Passos:** conservar campos obrigatórios, senha, cálculo de idade e carregamento de país/estado existentes, validando seus contratos reais. Corrigir cadastro para navegar ao login após confirmação, substituindo a tentativa automática atual. Acrescentar opção recrutador sem autocadastro; integrar sua estratégia ao cliente de login de T12. Persistir somente `{id,tipo}` confirmado como navegação; tratar credenciais inválidas, sessão ausente/inativa, conflitos e erro de rede sem persistir senha. Rever validações frontend herdadas que restrinjam silenciosamente valores aceitos pelo backend; não criar nova regra de idade/domínio. Não recriar formulários ou voltar ao login por e-mail local.
- **Validação:** compilação/build; navegador com cadastros novos, referência inválida, conflito, login válido/inválido e inativo dos três perfis, refresh e falhas. Inspecionar sessão/rede e ausência de login automático após cadastro; informar quando a jornada recrutadora ainda depender de T17.
- **Conclusão:** cadastros e entrada existentes funcionam com a sessão real; opção de recrutador preparada sem inventar perfil local, credenciais persistidas ou nova regra de cadastro.

**Entrega de T13 (09/11/2026):**

- Cadastros existentes aguardam sucesso HTTP e encaminham a `index.html`, sem chamada de login ou gravação de identidade local. Preservados os campos obrigatórios, cálculo de idade, carregamento de países/estados e catálogo por nomes. Senha segue exatamente como digitada. Removidos limite local de idade 1–120 e exigência local de competência mínima: a servlet aceita idade não negativa e lista vazia; datas inválidas/futuras continuam rejeitadas no cálculo. Checkboxes permanecem para T14.
- Acrescentadas opção e Strategy de recrutador, consumindo `LoginApiClient.loginRecrutador`. Resposta validada de T12 persiste somente `{id,tipo}`; não há autocadastro nem ID fabricado. Sucesso do recrutador permanece na página de entrada com confirmação e aviso de indisponibilidade da área. Nenhuma rota/tela fictícia foi criada; navegação da jornada será integrada em T17.
- Tratamento compartilhado dos formulários de login mantém estratégias específicas por ator, valida campos, impede tentativas simultâneas e submissão nativa, remove navegação anterior na tentativa, apresenta erros e limpa o campo de senha. As páginas existentes de candidato/empresa validam o formato da sessão local e consultam o perfil no servidor antes de carregar os demais dados; `401`/`403` limpam a navegação e encaminham ao login. Armazenamento local não autentica nem autoriza operações.

**Arquivos** (relativos à raiz):

- Criados: `frontend/src/ts/strategy/login/LoginRecrutadorStrategy.ts`; `frontend/src/ts/pages/login/{LoginRecrutador,FormularioLogin,SessaoNavegacao}.ts`; `frontend/tests/entrada-t13.test.cjs`.
- Modificados: `frontend/src/html/index.html`; `frontend/src/ts/pages/LoginPage.ts`; `frontend/src/ts/pages/login/{LoginCandidato,LoginEmpresa}.ts`; `frontend/src/ts/pages/candidato/{CadastroCandidatoPage,CandidatoPage}.ts`; `frontend/src/ts/pages/empresa/{CadastroEmpresaPage,EmpresaPage}.ts`; `CHANGELOG.md` e este plano. Os dois auxiliares novos concentram somente apresentação de entrada e navegação das páginas existentes, sem nova camada de autorização.

**Validação e rastreabilidade:**

| Critério de T13 | Evidência e limite |
| --- | --- |
| Compilação e empacotamento | `npx.cmd tsc --noEmit` e `npx.cmd gulp build` em `frontend` aprovados; Webpack 5.110.3, apenas aviso não impeditivo `DEP0180`. `dist` somente gerado pelo build. |
| Cadastro confirmado sem login automático | `node --test tests/entrada-t13.test.cjs tests/transporte-t12.test.cjs`: 20 testes aprovados, zero falhas/ignorados. Páginas reais com DOM/fetch controlados verificam espera por `201` vazio, redirecionamento, payload exato, senha com espaços e ausência de POST de login/gravação local após cadastro. |
| Referências, idade e falhas de cadastro | Mesma suíte verifica nomes/siglas reais, RH/administração por nomes, lista vazia, idade zero/acima de 120, datas inválidas/futuras, carregamento bloqueado após falha, `400`, `409`, rede e submissões duplicadas. Não estabelece unicidade persistida de RN-07–RN-09/CA-19–CA-22 nem reutilização de histórico. |
| Entrada dos três perfis e sessão mínima | Mesma suíte executa as três páginas/estratégias pelo transporte real de T12, com respostas controladas válidas, `401`, rede/JSON inválido e tipo divergente; confirma somente ID/tipo no armazenamento e ausência de navegação fictícia. Resposta `401` simulada não comprova inatividade persistida. |
| Refresh, sessão ausente e rejeição pelo servidor | Mesma suíte verifica estado local ausente/malformado/perfil divergente e chamadas reais dos consumidores de perfil com respostas controladas `401`/`403`, sem consultas seguintes. Não comprova cookie/CORS em navegador ou refresh full-stack. |
| Regressão backend de API/serviços | `.\gradlew.bat --gradle-user-home C:/Users/Mysterio/.gradle test --tests 'com.jpcode.api.*' --tests 'com.jpcode.service.*' --rerun-tasks`: `BUILD SUCCESSFUL`, 236 testes em 12 classes, zero falhas/erros/ignorados nos XML novos; JaCoCo gerado. Inclui HTTP em Tomcat real com controllers mockados, login/sessão, visibilidade, serviços e hash em memória. Não seleciona integrações JDBC. |

As duas falhas da primeira execução da suíte nova identificaram a exigência local de competência mínima ainda presente; removida conforme a revisão do contrato, com asserções preservadas e execução final aprovada. A primeira tentativa Gradle restrita falhou por acesso negado ao lock no cache externo; execução autorizada concluiu com tarefas em cache. A execução final com `--rerun-tasks` produziu resultados novos, contabilizados acima. Revisão integral do diff, inclusive arquivos novos, e `git diff --check` sem erros. Backend, SQL, dependências, DTOs anônimos e especificação aprovada sem alterações. Regressão T12 inclui descoberta anônima e retribuição isolada por vaga.

**Banco, pendências e parada:** nenhuma conexão ou escrita de banco realizada. Variáveis `LINKETINDER_TEST_*` ausentes; não foi possível estabelecer uma conexão dedicada de teste. `SuportePostgres` inspecionado: exige configuração explícita e banco `linketinder_test[_sufixo]`, confere `current_database()`, sem fallback; setup/cleanup isolam schema e rollback. Integrações JDBC não selecionadas nem executadas. Integridade, transações, hash persistido, concorrência e reutilização de identificadores não são declarados novamente validados por esta tarefa frontend.

Não foi executado navegador real com backend/PostgreSQL dedicado. Permanecem pendentes os cenários de navegador previstos em T13: novos cadastros, referência inválida, conflito, login válido/inválido/inativo dos três perfis, refresh, falhas e inspeção de cookies/rede/armazenamento. As verificações automatizadas com DOM controlado não substituem esses cenários; por isso T13 permanece sem marcação de conclusão. Jornada recrutadora depende de T17 conforme o plano. Nenhuma implementação de T14 ou posteriores; demais tarefas e especificação preservadas. Nenhum `git add`, `git commit` ou `git push`; autorização deste pedido prevalece sobre o checkpoint de commit da skill `feature-com-spec`. Consumo de tokens sem medição confiável disponível.

### T14 — Substituir checkboxes existentes por seletor pesquisável reutilizável

- **Objetivo:** usar o catálogo backend já integrado em seleção pesquisável nos três formulários previstos.
- **Estado:** implementação entregue em 09/11/2026 nos formulários existentes; validação automatizada registrada abaixo. Conclusão pendente de navegador com backend/banco dedicado e persistência N:N; checklist permanece desmarcado. Integração no formulário recrutador permanece em T17.
- **Referência:** RN-10; seção 8, “Competências e contratos TypeScript”; CA-23 a CA-25.
- **Camada:** Frontend.
- **Existentes:** `F/ts/models/Competencia.ts` (classe, não enum), DTOs de consulta, `CompetenciaService` e `CompetenciaApiClient`; `F/ts/pages/candidato/CadastroCandidatoPage.ts`; `F/ts/pages/empresa/EmpresaVaga.ts`; `F/html/{cadastro-candidato,empresa}.html`; `F/css/style.css`.
- **Novos:** `F/ts/components/SeletorCompetencias.ts`.
- **Dependências:** T12, T13; integração no formulário recrutador em T17.
- **Passos:** reutilizar o catálogo `string[]` corrigido em T12; filtrar parte do nome, selecionar somente item existente, manter múltiplos nomes sem duplicatas e permitir remoção. Texto livre não selecionado não integra payload. Substituir a lógica dos checkboxes por componente compartilhado, sem fabricar objetos/IDs para o catálogo nem mudar os DTOs de referências das consultas. Não existe enum a remover na base importada.
- **Validação:** compilação/build; navegador com RH/administração, pesquisa parcial, duplicata, remoção, texto livre, lista vazia e falha. Verificar payload e, em banco dedicado auditado, persistência N:N.
- **Conclusão:** componente reutilizável seleciona referências por nome nos formulários, preservando o backend e sem duplicar lógica.

**Entrega de T14 (09/11/2026):**

- Criado `F/ts/components/SeletorCompetencias.ts`, recebendo o catálogo `string[]` já consultado pelos consumidores. Pesquisa por parte do nome sem distinguir maiúsculas/minúsculas, sugestões somente do catálogo, múltiplas seleções únicas e remoção. O payload usa uma cópia dos nomes selecionados; pesquisa livre nunca é incorporada. Cada instância tem estado próprio, sem IDs fabricados ou persistência local.
- Substituída a renderização/captura dos checkboxes em `CadastroCandidatoPage` e `EmpresaVaga` pelo mesmo componente. Catálogo vazio permite lista vazia, conforme os contratos existentes. Cadastro candidato continua bloqueado se referências falham; criação empresarial também fica bloqueada durante carregamento/falha do catálogo. Falha de submissão mantém seleção; sucesso de vaga limpa seleção e pesquisa.
- Incluídos rótulo da pesquisa, grupos de sugestões/seleção, mensagem de estado e botões nativos `type="button"`, com foco visível e remoção nomeada. Enter no campo de pesquisa não submete o formulário nem seleciona texto livre; sugestões podem ser acessadas por Tab e ativadas pelo teclado. Nomes são escritos com `textContent`.
- Mantidos os containers HTML existentes. Sem alterações de backend, DTOs, schema, dependências, sessão ou regras de negócio. Formulário recrutador não foi criado: seu consumo do componente pertence expressamente a T17.

**Arquivos:** criados `F/ts/components/SeletorCompetencias.ts` e `frontend/tests/seletor-t14.test.cjs`; modificados `F/ts/pages/candidato/CadastroCandidatoPage.ts`, `F/ts/pages/empresa/EmpresaVaga.ts`, `F/css/style.css`, `frontend/tests/{transporte-t12,entrada-t13}.test.cjs`, `CHANGELOG.md` e este plano. Os testes anteriores foram adaptados para selecionar sugestões reais em vez de simular checkboxes, preservando suas asserções de transporte/payload.

**Comandos e evidências:**

| Verificação | Resultado e alcance |
| --- | --- |
| `npx.cmd tsc --noEmit`, em `frontend` | Passou, TypeScript estrito. Tentativa inicial na raiz apenas exibiu ajuda por ausência de tsconfig; execução válida no diretório frontend concluiu com código zero. |
| `npx.cmd gulp build`, em `frontend` | Passou; Webpack 5.110.3 compilou. Aviso não impeditivo `DEP0180`; `dist` gerado somente pelo build. |
| `node --test tests/transporte-t12.test.cjs tests/entrada-t13.test.cjs tests/seletor-t14.test.cjs`, em `frontend` | Execução final: 28 testes aprovados, zero falhas/ignorados, incluindo oito novos casos T14. DOM/fetch controlados; sem navegador, rede ou banco. A primeira rodada detectou três falhas por acentuação corrompida na gravação via PowerShell; corrigidas antes da execução final. |
| `.\gradlew.bat --gradle-user-home C:/Users/Mysterio/.gradle test --tests 'com.jpcode.api.*' --tests 'com.jpcode.service.*' --rerun-tasks` | `BUILD SUCCESSFUL`: 236 testes em 12 XMLs, zero falhas/erros/ignorados; compilação e JaCoCo concluídos. Acesso inicial ao lock do cache externo negado no sandbox; repetição autorizada passou. Testes de serviços/servlets e HTTP em Tomcat com controllers mockados, sem selecionar integrações JDBC. |
| Revisão integral do diff, incluindo arquivos novos, e `git diff --check` | Sem erros de whitespace. Alterações restritas a componente, dois consumidores, estilos, testes e documentação T14; backend, SQL, contratos, dependências e especificação aprovada sem diff. |

**Aceite e limites por critério:**

| Critério | Evidência / pendência |
| --- | --- |
| CA-23: pesquisa parcial, múltiplas seleções, duplicatas, remoção e texto livre | Exercitados no componente real e nos payloads dos dois formulários com DOM controlado, incluindo Enter, nenhuma correspondência, catálogo duplicado e lista vazia. Interação e apresentação em navegador real pendentes. |
| CA-24: RH/administração sem catálogo fixo | Fetch controlado fornece esses nomes; sugestões, seleção e envio por nomes aprovados. Persistência real dessas referências ainda não executada nesta tarefa. |
| CA-25: catálogo backend e vínculos N:N | Consumidores existentes continuam consultando `/competencias` e enviando nomes pelos serviços/clientes reais, com credenciais. Contratos de referências e backend preservados. N:N não foi revalidado em banco; integração recrutadora pertence a T17, sem declarar aceite integral de CA-25. |
| Falhas, limpeza e compatibilidade | Testes verificam falha HTTP/rede de catálogo nos dois formulários, bloqueio sem POST, seleção preservada após erro de cadastro e limpeza somente após sucesso da vaga; regressão T12/T13 inclui sessão, descoberta anônima e retribuição por vaga. |

**Segurança do banco:** antes de considerar escritas, inspecionados o DDL atual e os suportes `SuportePostgres`/`SuporteContasPostgres`. Exigem as três variáveis explícitas, URL restrita a `linketinder_test[_sufixo]`, confirmação de `current_database()` em cada conexão e nenhum fallback para desenvolvimento. Setup usa schema aleatório exclusivo, sem `public`; cleanup faz rollback ou remove somente o schema de nome validado. As variáveis `LINKETINDER_TEST_*` estão ausentes no processo: nenhuma conexão/escrita ou integração JDBC executada, sem tentativa de usar desenvolvimento. Não se declara nova validação de transações/integridade persistida.

**Anonimato e autorização:** nenhum DTO, endpoint, consulta ou contexto autenticado foi alterado. Regressão backend executada verifica sessão, escopo e visibilidade; T12/T13 continuam verificando transporte com cookies, navegação e isolamento por vaga. Nomes do catálogo não são usados como identidade de ator nem para buscar perfis alheios. A composição `DaoConfig` encontrada em fixture HTTP não abre conexão: os controllers são substituídos por mocks. Essas evidências não substituem o aceite full-stack em navegador.

**Pendências e parada:** não há ferramenta de navegador disponível nesta sessão; DOM controlado não substitui navegador com backend/PostgreSQL. Permanecem pendentes os cenários de pesquisa/seleção/remoção/texto livre/lista vazia/falha em navegador e a comprovação de persistência N:N em banco dedicado auditado. Por isso T14 não é marcada como concluída. T13 mantém seu status anterior; T15 e posteriores não implementadas. A autorização explícita deste pedido prevalece sobre o checkpoint de commit da skill `feature-com-spec`; nenhum `git add`, `git commit` ou `git push` executado. Especificação aprovada preservada. Consumo de tokens sem medição confiável disponível.

### T15 — Adequar a jornada e os matches existentes do candidato

- **Objetivo:** estender a jornada candidata importada à visibilidade e aos matches com recrutador.
- **Referência:** RN-03 a RN-05; seção 6; CA-08, CA-10, CA-14 a CA-17.
- **Camada:** Frontend.
- **Existentes:** `F/ts/pages/candidato/{CandidatoPage,CandidatoPerfil,CandidatoVaga,CandidatoVagaCurtida,CandidatoComponentes,CandidatoMatch}.ts`; `F/html/candidato.html`; clientes/serviços compatibilizados em T12.
- **Novos:** nenhum componente de match obrigatório; `CandidatoMatch.ts` já existe.
- **Dependências:** T12, T13.
- **Passos:** conservar perfil próprio separado da descoberta e estado de curtidas pela listagem anônima. Cards continuam somente ID/descrição/competências, mesmo após match. Estender o componente existente para todos os campos autorizados do contrato de match e nome/área do autor quando presentes, sem buscar outro perfil. Revisar vazio/carregamento/erro, repetição e atualização visual após confirmação.
- **Validação:** compilação/build; navegador com vaga direta e duas vagas da mesma empresa; curtir não revela identidade; retribuir só A revela apenas o match de A. Inspecionar JSON, refresh, contrato completo e ausência de chamadas alternativas identificáveis.
- **Conclusão:** jornada existente funciona sob os contratos de T11 e apresenta o recrutador somente no match correspondente, sem recriar tela de matches.

### T16 — Estender a operação empresarial e cadastrar recrutadores

- **Objetivo:** acrescentar cadastro de autores e visibilidade interna de autoria à jornada empresarial existente.
- **Referência:** RN-01 a RN-06; seções 3, 6 e 8; CA-01, CA-05 a CA-07, CA-09, CA-13, CA-14, CA-18.
- **Camada:** Frontend.
- **Existentes:** `F/ts/pages/empresa/{EmpresaPage,EmpresaPerfil,EmpresaVaga,EmpresaCandidato,EmpresaMatch,EmpresaCandidatoCurtido}.ts`; `RelatorioCompetenciasService`, `CompetenciaChart`, `F/html/empresa.html` e estilos. Rastrear se a página histórica de curtidos ainda tem consumidor; não reativar busca de perfil alheio.
- **Novos:** `F/ts/pages/empresa/EmpresaRecrutador.ts`.
- **Dependências:** T12 a T14.
- **Passos:** acrescentar formulário empresarial de recrutador com área persistida, empresa derivada do contexto e confirmação HTTP. Estender listagem interna para distinguir vagas diretas e autorais e identificar autor histórico inativo. Reutilizar interessados anônimos por vaga e retribuição idempotente contextualizada de T12; usar `EmpresaMatch` para identificação e ampliar apresentação dos campos autorizados/nome/área do autor. O gráfico já conta competências anônimas por interesse: preservar e validar, alterando apenas o necessário ao contrato vigente. Não reconstruir reciprocidade global, buscar candidato por perfil alheio, criar gestão/reatribuição ou retirar componentes históricos sem análise.
- **Validação:** compilação/build; navegador com erro de área/cadastro, vagas diretas/autorais, retribuição empresarial nas duas origens, refresh e autoria após inatividade. Conferir gráfico com candidato interessado em duas vagas e isolamento empresarial.
- **Conclusão:** empresa acompanha todas as próprias vagas/matches e cadastra recrutadores com as referências existentes; gráfico mantém contagem por interesse, sem regressão de anonimato.

### T17 — Acrescentar jornada do recrutador com escopo próprio

- **Objetivo:** entregar criação de vagas, interessados e matches do autor ativo, reutilizando a integração consolidada.
- **Referência:** seções 3 e 8; RN-02 a RN-06, RN-10; CA-05 a CA-07, CA-09, CA-14, CA-17, CA-18, CA-25.
- **Camada:** Frontend.
- **Existentes:** `F/ts/app/App.ts`, estilos, cliente/serviço de recrutador de T12, login de T13, seletor de T14 e padrões/componentes de T15/T16.
- **Novos:** `F/html/recrutador.html`, `F/ts/pages/recrutador/{RecrutadorPage,RecrutadorVaga,RecrutadorInteressados,RecrutadorMatch}.ts`.
- **Dependências:** T13 a T16.
- **Passos:** acrescentar seleção de página, perfil próprio e criação de vaga com seletor. Empresa deriva do vínculo backend; consultas limitam-se ao autor e às próprias vagas. Identificação vem do contrato de match; conta inativa/escopo divergente produzem falha, sem fallback empresarial ou acesso às vagas do colega. Não acrescentar autocadastro, cadastro de terceiros, vaga direta empresarial ou endpoints opcionais.
- **Validação:** compilação/build; navegador com dois recrutadores da mesma empresa e outro de empresa distinta; IDs adulterados rejeitados no backend já existente; repetição por empresa/autor mantém match único; inatividade durante sessão bloqueia operação sem apagar autoria.
- **Conclusão:** recrutador opera somente seu escopo sobre o backend preservado, sem duplicar a camada de transporte.

### Integração e regressão

### T18 — Validar aceite completo e registrar evidências das entregas

- **Objetivo:** demonstrar os fluxos obrigatórios e as invariantes em execução real.
- **Referência:** CA-01 a CA-25; CA-26 condicional excluído; seções 1, 9, 11 e 12.
- **Camada:** Integração.
- **Existentes:** `build.gradle` (configuração de testes somente se necessária ao runtime real), testes acrescentados nas tarefas anteriores, `CHANGELOG.md`, `README.md` (apenas instruções afetadas de execução full-stack, sem credenciais).
- **Novos:** `T/integracao/FluxoHttpRecrutadorTest.groovy`, `docs/specs/recrutador/validacao.md` (roteiros/evidências por CA, criado somente na implementação).
- **Dependências:** T01 a T17.
- **Passos:** executar matriz da seção 5 com banco separado, Tomcat real e navegador. Teste HTTP pode usar `java.net.http.HttpClient`/cookie manager e Spock existentes; inicializar composição com ConnectionFactory de teste explicitamente injetada, sem usar banco fixo de desenvolvimento. Reutilizar a composição e o suporte de testes de T11; esta revisão não autoriza alterar o backend para validar o frontend. Se surgir impedimento real, registrar e solicitar decisão específica. Não presumir tarefa Gradle de integração já existente. Registrar comandos, status, contagens persistidas, falhas e correções sem credenciais. Atualizar changelog por entregas confirmadas e suas referências, sem reescrever histórico alheio.
- **Validação:** estratégia da seção 6; repetir concorrência em conexões independentes, rollback, autoria histórica, reutilização de identificadores, escopo e ausência de campos indevidos no JSON. Rodar regressão de serviços existentes e compilação de todos os consumidores, incluindo terminal, sem adaptar seus menus.
- **Conclusão:** CA-01 a CA-25 têm evidência reproduzível, nenhum obrigatório fica sem validação e nenhuma declaração de sucesso depende apenas de mock/compilação. Divergências impedem aceite até correção; CA-26 permanece explicitamente não aplicável.

### Limpeza final

### T19 — Analisar e remover código obsoleto após o aceite

- **Objetivo:** encerrar a transição para a nova versão removendo componentes substituídos e sem função vigente, sem regressão funcional, de segurança ou de integridade.
- **Referência:** seção 9 da especificação, “Impact on Existing Functionality”; limites das seções 1 e 7 deste plano; autorização adicional do responsável para esta tarefa final. Não acrescenta regras de negócio nem altera os critérios de aceite aprovados.
- **Camada:** Backend, frontend e composição, exclusivamente nos componentes comprovadamente obsoletos da feature.
- **Existentes:** revisar inicialmente `B/service/{EmpresaService,MatchService}.groovy`, `B/controller/{empresa/EmpresaController,match/MatchController}.groovy`, `B/facade/MatchFacade.groovy`, contratos/DAOs envolvidos nas operações substituídas, `B/config/{DaoConfig,ServiceConfig,ControllerConfig}.groovy`, `B/view/` e suas fábricas/configurações somente para rastrear consumidores; `F/ts/config/ServiceConfig.ts`, repositórios/factories históricos e seus consumidores efetivamente presentes após T12–T17 (RepositoryConfig não existe no snapshot importado; não recriá-lo para limpeza); testes correspondentes, `CHANGELOG.md` e `docs/specs/recrutador/validacao.md`. Esses caminhos são candidatos à análise, não uma lista de exclusão; confirmar o checkout e ampliar o inventário apenas para dependências diretamente relacionadas encontradas.
- **Novos:** nenhum arquivo obrigatório; registrar inventário e evidências em `docs/specs/recrutador/validacao.md`, criado em T18.
- **Dependências:** T01 a T18 concluídas e validadas. Executar por último, sem antecipar remoções durante as tarefas funcionais e sem reabrir ou renumerar tarefas concluídas.
- **Passos:** inventariar métodos, classes, contratos, imports, campos, construtores, injeções e testes substituídos. Para cada candidato, registrar localização, comportamento antigo, substituto validado, consumidores diretos/indiretos e decisão justificada. Rastrear também chamadas dinâmicas Groovy, composição manual, registros de rotas, seleção de páginas e consumidores de terminal; ausência de resultado em busca textual ou compilação isolada não prova ausência de uso. Remover incrementalmente componentes sem consumidores válidos e dependências que se tornaram desnecessárias; revisar especialmente métodos que apenas lançam exceção por legado e aliases sem necessidade vigente. Atualizar testes para exercer os fluxos públicos vigentes, preservando asserções de autorização, anonimato, atomicidade e idempotência; retirar testes cujo único objeto seja uma assinatura obsoleta removida. Não eliminar componentes apenas pela idade, pelo nome ou por usarem armazenamento local: sessão de navegação e qualquer uso ainda válido devem permanecer. Não remover ou adaptar menus/entrada do terminal, contratos ativos, funcionalidades válidas, schema ou dados. Se uma remoção depender de retirar/adaptar consumidor ainda utilizado, registrar o impedimento e solicitar decisão específica antes de alterar esse conjunto; manter a proteção contra operações inseguras, sem reativar comportamento antigo para satisfazer compatibilidade. Atualizar changelog somente com remoções efetivamente realizadas, sem apagar histórico das entregas anteriores.
- **Validação:** comparar comportamento e contratos com as evidências de T18; revisar o diff completo e procurar referências quebradas, dependências e injeções sem uso. Executar `.\gradlew.bat build --rerun-tasks` com ambiente JDBC dedicado e previamente auditado, além de `npx tsc --noEmit` e `npx gulp build` em `frontend`. Reexecutar testes JDBC/HTTP reais e as jornadas de candidato, empresa e recrutador da matriz de T18, incluindo rejeições de acesso, conta inativa, anonimato, repetição/concorrência e rollback. Confirmar compilação e composição dos consumidores mantidos, inclusive terminal, sem ampliar seus menus. Antes de qualquer escrita de teste, verificar todas as conexões, ausência de fallback e cleanup restrito ao banco/schema isolado. Documentar comandos, resultados e validações não executadas; falha ou ausência de evidência impede declarar a limpeza validada.
- **Conclusão:** inventário revisado, código comprovadamente obsoleto removido com suas dependências sem uso, nenhum consumidor válido quebrado e nenhum requisito ou invariante enfraquecido. Os fluxos aprovados continuam funcionando após a limpeza, com evidências reais atualizadas em `validacao.md`; especificação, status das tarefas anteriores e histórico do changelog preservados. Não marcar T19 concluída enquanto houver candidato identificado como obsoleto sem decisão, dependência não resolvida ou validação obrigatória pendente; registrar explicitamente os componentes mantidos por necessidade vigente, sem tratá-los como removidos.

## 5. Rastreabilidade de requisitos

### Regras e seções

| Referência da especificação | Tarefa(s) | Validação |
| --- | --- | --- |
| Seção 1, objetivo full-stack e histórico | T12–T18 | Fluxo persistido completo; changelog somente com entregas reais |
| Seção 2, obrigatório/opcional/limites | T01–T18 | Revisão de escopo; CA-26 excluído; nenhum framework, migração legada ou adaptação de menu |
| Seção 3, Actors and Permissions | T07–T11, T15–T18 | Matriz empresa/autor/outro recrutador/candidato e sessão inativa |
| RN-01 — Vínculo e cadastro | T01, T06, T11, T16, T18 | FKs, serviço, formulário e persistência |
| RN-02 — Propriedade e autoria da vaga | T02, T08, T11, T16–T18 | Vaga direta, autor, empresa divergente e autoria histórica |
| RN-03 — Interesse inicial e destinatários | T04, T09, T11, T15–T18 | Um interesse consultável pelos destinatários; nenhum match inicial |
| RN-04 — Retribuição contextualizada | T04, T09, T10, T15–T18 | Duas vagas da mesma empresa, retribuição independente |
| RN-05 — Match | T04, T09–T11, T15–T18 | FKs, atomicidade, concorrência, consultas e apresentação |
| RN-06 — Inatividade e responsabilidade | T01, T05–T11, T13, T16–T18 | Login/operação/atualização bloqueados; continuidade pela empresa |
| RN-07 — CPF | T03, T06, T13, T18 | Escopo por perfil, duplicidade ativa e reutilização inativa |
| RN-08 — E-mail | T03, T06, T13, T18 | Concorrência entre perfis, alteração e reutilização |
| RN-09 — CNPJ | T03, T06, T13, T18 | Unicidade ativa e novos IDs sem transferência histórica |
| RN-10 — Competências | T08, T12, T14, T16–T18 | Catálogo backend, seleção/remoção e relações N:N |
| Seção 5, Data Model | T01–T04, T06, T08–T10 | Inspeção DDL, JDBC real e mapeamentos |
| Seção 6, contratos e matriz de visibilidade | T07, T10–T12, T15–T18 | Chaves exatas no JSON e tentativas por rotas alternativas |
| Seção 7, Authentication and Security | T05–T07, T11–T13, T18 | Hash/salt, credenciais, sessão e conta ativa |
| Seção 8, capacidades HTTP/interface/contratos TS | T06–T17 | Rotas de 3.5, DTOs, compilação e navegador |
| Seção 9, Impact on Existing Functionality | T05, T08–T18 | Regressão de cadastros, login, curtidas, matches, gráfico e contratos |
| Seção 11, D-01 a D-07 | T01, T02, T04, T06, T08–T18 | Branch correta, relações e modalidades de match/visibilidade |
| Seção 11, D-08 a D-11 | T03, T05–T13, T16–T18 | Perfis separados, hash, unicidade ativa e autoria preservada |
| Seção 11, D-12 e D-13 | T01–T18 (limites) | Ausência dos opcionais/UI administrativa e de migração/adaptação de terminal |
| Seção 11, D-14 e D-15 | T05, T14, T17, T18 | Seletor aberto ao catálogo e parâmetros/medição PBKDF2 |
| Seções 12 e 13, exclusões e decisões técnicas adiadas | Seções 1, 3 e 9 deste plano; T18 | Revisão de limites; não presumir validação com Tutor ou autorização de commit |
| Seção 9, preservação dos fluxos após a remoção do legado; autorização adicional de limpeza final | T19, após T18 | Inventário de obsolescência e consumidores; remoção focada e repetição da matriz validada, sem novos requisitos de negócio |

### Critérios de aceite

| Referência da especificação | Tarefa(s) | Validação |
| --- | --- | --- |
| CA-01 | T01, T06, T11, T16, T18 | Cadastro empresarial pelo navegador, SELECT de referências/atividade e resposta sem credenciais |
| CA-02 | T01, T06, T11, T18 | FKs e rejeição de referências inválidas no serviço/HTTP |
| CA-03 | T05–T07, T13, T18 | Três perfis, senha inválida e conta inativa |
| CA-04 | T05–T07, T10–T13, T18 | Hash persistido, salts diferentes, nenhuma credencial em retorno/erro/log |
| CA-05 | T02, T08, T11, T16–T18 | Criação direta/autoral, tentativa com empresa divergente |
| CA-06 | T08, T10, T11, T16–T18 | Empresa vê todas; autor só próprias; colega rejeitado |
| CA-07 | T09, T11, T16–T18 | Uma linha de interesse, dois observadores quando aplicável |
| CA-08 | T04, T09, T15, T18 | Curtir sem retribuição: zero match, DTO anônimo |
| CA-09 | T09, T11, T16–T18 | Empresa nas duas origens e autor em vaga própria |
| CA-10 | T04, T09, T10, T15–T18 | Duas vagas curtidas, retribuição somente em A |
| CA-11 | T04, T09, T11, T18 | Interesse ausente/proprietária errada: nenhuma gravação de match |
| CA-12 | T04, T09, T18 | Threads/conexões independentes e contagem final única |
| CA-13 | T09, T10, T15–T18 | Empresa retribui vaga autoral; um match, nome/área do autor |
| CA-14 | T10, T11, T15–T18 | Consultas por cada titular e rejeição de ID alheio |
| CA-15 | T10–T12, T15–T18 | Igualdade exata de chaves anônimas; GET por ID/listagens alternativas |
| CA-16 | T10, T11, T15–T18 | Todos os campos autorizados de match; recrutador só nome/área |
| CA-17 | T09–T11, T15–T18 | Match em A, outro recrutador e descoberta B continuam restritos |
| CA-18 | T02, T05–T11, T13, T16–T18 | Fixture inativa, sessão bloqueada, empresa continua e autoria permanece |
| CA-19 | T03, T06, T13, T18 | CPF por perfil ativo; coincidência candidato/recrutador aceita |
| CA-20 | T03, T06, T13, T18 | Cadastros concorrentes em todos os pares de perfis, um único commit válido |
| CA-21 | T03, T06, T18 | Reutilização inativa com IDs novos, relações antigas intactas |
| CA-22 | T03, T06, T13, T18 | CNPJ duplicado entre empresas ativas rejeitado |
| CA-23 | T14, T17, T18 | Pesquisa parcial, múltipla seleção, duplicata, remoção e texto livre |
| CA-24 | T14, T16–T18 | Referências de RH/administração persistidas sem editar catálogo frontend |
| CA-25 | T08, T11–T14, T16–T18 | Três formulários e conferência JDBC das tabelas N:N |
| CA-26 | Não aplicável: opcional excluído na sequência aprovada | T11/T17/T18 verificam ausência dos endpoints/interface opcionais; não declarar o critério implementado |

Nenhum requisito obrigatório foi deixado sem tarefa. As validações manuais do DOM/seletor não têm automação frontend nesta proposta; isso é limitação de infraestrutura, não dispensa de executar o roteiro. Confirmação com Tutor e autorização de commits são evidências de processo, não resultados inferíveis por teste.

## 6. Estratégia de testes e validação

### Tipos de evidência

| Tipo | Procedimento planejado | O que demonstra |
| --- | --- | --- |
| Inspeção estática | Conferir rotas registradas, DTOs, chamadas, SQL parametrizado, todos os escritores de contas e todos os retornos HTTP | Cobertura dos caminhos e contratos; não prova concorrência ou funcionamento real |
| Compilação | Na raiz, `.\gradlew.bat compileGroovy compileTestGroovy`; em `frontend`, `npx tsc --noEmit` | Compatibilidade de tipos/classes e consumidores; Groovy dinâmico também exige execução para erros como o método de competências |
| Unitários | `.\gradlew.bat test --tests 'com.jpcode.service.*' --tests 'com.jpcode.facade.*' --tests 'com.jpcode.api.servlet.*'` depois de criados os pacotes novos | Regras/DTOs/erros isolados com mocks Spock |
| Integração JDBC/HTTP | `.\gradlew.bat test --tests 'com.jpcode.integracao.*'`, após criar os testes e fornecer banco separado | SQL, FKs, transações, locks, cookies, servlet real e persistência |
| Frontend manual | Servir `frontend/dist/`, abrir `http://127.0.0.1:5500/html/index.html`, usar API no mesmo host/porta 8080 | DOM, seletor, navegação, cookies/CORS e tratamento de falhas |
| Regressão/build | `.\gradlew.bat build`; em `frontend`, `npx gulp build` | Suíte configurada e empacotamento dos arquivos fonte |

Se dependências frontend precisarem ser instaladas, executar `npm ci` em `frontend`; não alterar versões. Não executar `npm test`, que é placeholder. Não editar `frontend/dist/`; ele é gerado. Backend é iniciado pela classe `com.jpcode.api.RestServer` com runtime classpath, não `gradlew run`.

O suporte novo de integração deve aceitar configuração explícita por variáveis de ambiente de teste, sem credenciais hardcoded, criar dados próprios em schema/banco isolado e limpar apenas suas fixtures. Banco não configurado deve produzir indicação explícita de integração não executada; nunca contar ausência de banco como aprovação. Execução completa de T18 exige banco disponível e todos os testes reais. Não executar exemplos SQL legados nem resetar dados existentes.

Preparar duas empresas, dois recrutadores de uma delas e outro da segunda, áreas distintas, candidatos e duas vagas da mesma empresa, além de uma direta. Usar senhas novas, identificadores únicos de fixture e referências existentes/criadas no ambiente de teste. Inatividade para teste é preparada diretamente nesse ambiente, sem criar interface de desativação.

Concorrência deve usar transações/conexões distintas, início coordenado e timeout limitado; verificar resultados e linhas após ambos terminarem. Incluir retribuição empresa/recrutador simultânea, e-mails concorrentes entre perfis e atualização versus cadastro. Injetar falha entre retribuição e match, confirmar rollback, repetir operação e verificar sucesso único. Mocks não estabelecem essas propriedades.

Validar JSON bruto com lista exata dos campos permitidos, inclusive em falhas, perfis próprios e caminhos alternativos. Testar sem sessão, sessão de outro perfil, ID adulterado, empresa divergente, colega da mesma empresa e conta inativada após login. Match deve conservar todos os campos autorizados existentes e acrescentar somente nome/área do recrutador.

Registrar evidência por CA em `validacao.md`: comando/ação, ambiente, resultado observado, resultado esperado e eventual falha. Não registrar senha/hash/salt ou credenciais JDBC. Relatórios existentes: testes em `build/reports/tests/test/`; JaCoCo em `build/reports/jacoco/test/`. Cobertura numérica não substitui qualidade das asserções ou integração.

Em T19, acrescentar ao mesmo documento o inventário de obsolescência, as decisões de remoção/preservação e a revalidação após a limpeza. As evidências de T18 são a referência anterior à remoção, não substituem a repetição dos testes e jornadas no código final. Nenhuma remoção fica autorizada apenas por passar na compilação.

**Estado nesta elaboração:** somente leitura/inspeção e revisão documental. Nenhum comando de compilação, teste, benchmark, banco ou execução do sistema foi realizado para declarar funcionalidade aprovada. Tokens consumidos não estão disponíveis como medição confiável nesta sessão.

## 7. Riscos e compatibilidade

- **Unicidade entre tabelas:** uma escrita que não obtenha os locks antes da validação compromete a garantia. Auditar os três DAOs e futuras extensões; a região serializada reduz paralelismo de gravação de contas. Validar custos com teste concorrente, sem alegar garantia para SQL administrativo externo.
- **Transações e fechamento:** DAO que abrir outra conexão ou fechar conexão recebida quebra atomicidade. Testar rollback e posse dos recursos. Definir ordem consistente de locks; não misturar derivação de senha com locks prolongados.
- **Dados antigos:** inserts legados de senha simples e curtida global não satisfazem o novo contrato. Usar instalação/teste isolado com novas fixtures; nenhuma tarefa converte, apaga ou reseta registros antigos. Preparação manual de ambiente limpo é pré-requisito de execução, não migração silenciosa.
- **Novas FKs:** preservam reciprocidade, mas exigem revisão do caminho de exclusão física de vaga. Não mudar cascades como atalho e não introduzir retenção/gestão de vagas fora do escopo.
- **Autenticação e navegador:** sessão frontend não autentica requisição. Testar cookie, origem, renovação e conta inativa em HTTP real. A sessão nativa proposta serve exclusivamente ao contexto dos fluxos, sem expandir o escopo para identidade compartilhada.
- **Exposição histórica:** não filtrar autor inativo no JOIN de apresentação; distinguir autor do executor da retribuição. Não serializar entidade para resolver ausência de DTO.
- **Compatibilidade da integração importada:** os serviços já são assíncronos. T12 deve adaptar todos os fetch/consumidores ativos ao host/cookie, corpos e rotas de T11, sem duplicar clientes ou voltar à composição local. Compilação do snapshot não detecta chamadas /verificar inexistentes, catálogo string[] interpretado como objetos, sessão com tipos divergentes ou busca indevida de perfil alheio. Esses casos impedem concluir T12 até correção e verificação; as jornadas completas permanecem em T13–T17.
- **Modelos divergentes:** `tipo`/`formacao` frontend não autorizam nova persistência. Campos faltantes do cadastro backend devem ser capturados explicitamente e referências enviadas no formato real.
- **Terminal compartilhado:** compilar o projeto completo e preservar consumidores quando possível; não expandir menus nem manter caminho inseguro para satisfazer compatibilidade. Alteração adicional de escopo deve ser trazida à revisão antes de implementada.
- **Remoção de legado em T19:** chamadas dinâmicas e composição explícita podem ocultar dependências. Inventariar e rastrear produtores/consumidores antes de excluir, trabalhar em incrementos revisáveis e revalidar os fluxos após a limpeza. Componente com uso válido não é obsoleto; dependência de terminal não autoriza sua retirada ou adaptação incidental. Pendências de decisão/validação devem ficar explícitas e impedem concluir T19.
- **Histórico documental:** entradas do changelog não substituem arquivos da branch. Acrescentar evidências das entregas futuras sem afirmar que integrações ausentes já existem.

## 8. Checklist de implementação

- [x] Especificação aprovada lida e checkout `feature/recruiter` conferido.
- [x] Código relevante, schema atual, build, testes e changelog inspecionados.
- [x] Sequência preliminar aprovada pelo responsável antes da geração deste arquivo.
- [ ] Documento completo revisado e aprovado pelo responsável.
- [ ] Validação com Tutor confirmada, conforme pendência registrada na especificação; não presumir confirmação.
- [ ] Commit da especificação explicitamente autorizado e concluído antes de implementar, conforme `skills/feature-com-spec/SKILL.md`, passo 7.
- [x] T01 — Referências e recrutadores (evidências e limites registrados na tarefa).
- [x] T02 — Autoria e integridade empresarial (evidências e limites registrados na tarefa).
- [x] T03 — Unicidades parciais (evidências e limites registrados na tarefa).
- [x] T04 — Schema de retribuição e match (evidências e limites registrados na tarefa).
- [x] T05 — Proteção de senhas (evidências e limites registrados na tarefa).
- [x] T06 — Cadastro e unicidade concorrente entre perfis (evidências e limites registrados na tarefa).
- [x] T07 — Login e contexto HTTP (evidências e limites registrados na tarefa).
- [x] T08 — Criação e consulta de vagas (evidências e limites registrados na tarefa).
- [x] T09 — Interesse, retribuição e match atômicos (evidências e limites registrados na tarefa).
- [x] T10 — Matches e matriz de visibilidade (evidências e limites registrados na tarefa).
- [x] T11 — Rotas, contratos e acesso pelo navegador (superfície HTTP validada; navegador real permanece em T18).
- [x] T12 — Compatibilizar clientes/DTOs/serviços e consumidores da integração importada com T11 (transporte e contratos validados na nova base; navegador/aceite full-stack permanecem em T18).
- [ ] T13 — Adaptar cadastros/login existentes e acrescentar entrada de recrutador (implementada; validação em navegador com banco dedicado pendente, conforme evidências da tarefa).
- [ ] T14 — Substituir checkboxes por seletor pesquisável de competências (implementada; navegador com banco dedicado e persistência N:N pendentes, conforme evidências da tarefa).
- [ ] T15 — Jornada do candidato.
- [ ] T16 — Jornada empresarial e gráfico.
- [ ] T17 — Jornada do recrutador.
- [ ] T18 — Aceite, regressão e evidências.
- [ ] T19 — Análise e remoção final de código obsoleto, após T18, com inventário e regressão completa.
- [ ] Changelog atualizado apenas com tarefas entregues; resultados de validação registrados.
- [ ] Nenhuma implementação ou commit iniciado com base apenas na aprovação da sequência.

Implementar uma tarefa por vez após os gates aplicáveis. Cada commit continua dependendo de autorização explícita; não solicitar nem executar commits nesta tarefa de planejamento.

## 9. Questões abertas ou bloqueios

Não há ambiguidade de negócio identificada. As incompatibilidades de transporte/contratos entre o frontend importado e o backend de T11, identificadas na seção 2, foram tratadas e verificadas no escopo de T12, com evidências próprias acima. A importação isoladamente não as resolvia; o backend foi preservado. A exclusão dos endpoints opcionais foi incluída na sequência aprovada; CA-26 não é lacuna do escopo obrigatório.

As decisões backend da seção 3 já implementadas e validadas em T01–T11 permanecem preservadas. A revisão autorizada começa na fronteira com o frontend e adapta T12–T18 ao snapshot importado; não reabre locks, transações, rotas, sessão nativa, parâmetros de hash ou schema. T12 validou compilação/build, transporte e contratos da nova base; jornadas completas e aceite em navegador continuam nas tarefas posteriores.

A confirmação de validação com o Tutor permanece não informada na especificação e não foi inferida da aprovação da sequência. Não bloqueia a redação solicitada do plano, mas deve ser registrada antes dos encaminhamentos de commit previstos na especificação. Disponibilidade de JDK compatível, PostgreSQL separado e referências preparadas é pré-requisito da validação futura, a conferir antes de executar as tarefas correspondentes.
