# Plano de implementação — Recrutador

Status: documento para revisão final; implementação não autorizada por este plano.

Branch verificada: `feature/recruiter`. Data: 07/11/2026.

## 1. Objetivo e escopo

Implementar incrementalmente a [especificação aprovada](spec.md), mantendo sua autoridade sobre requisitos e decisões de negócio. A sequência preliminar foi aprovada pelo responsável no diálogo. Este documento detalha essa sequência; sua criação não autoriza código, alterações de banco ou commits.

Abranger os fluxos obrigatórios das seções 2 a 10 da especificação, inclusive a integração HTTP ausente neste checkout. Não incluir os endpoints opcionais de edição de perfil e troca de senha do recrutador: CA-26 será registrado como condicional não aplicável. As atualizações já existentes de candidato/empresa continuam sujeitas às regras de hash, unicidade e inatividade.

Não adaptar menus de terminal, migrar credenciais ou dados de teste, criar administração de vagas/reatribuição, identidade central, frameworks ou novas dependências. Manter as assinaturas utilizadas pelo terminal quando possível, sem preservar operações que burlem a consistência exigida. Alterações internas compartilhadas precisam continuar compilando com os consumidores existentes.

## 2. Arquitetura atual e impacto técnico

### Evidências verificadas

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

**Todas as rotas abaixo são propostas ou adaptações futuras, salvo as explicitamente marcadas como existentes.** Campos permanecem em português/camelCase, datas de entrada em ISO (`YYYY-MM-DD`) e competências de entrada como nomes. O ator é obtido da sessão. IDs de rota delimitam recursos e são comparados ao ator e aos vínculos persistidos.

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

Criar clientes específicos e DTOs separados para perfil, descoberta, vaga interna e match. Serviços passam a `Promise`; todas as páginas consumidoras usam `async/await` e só exibem sucesso após resposta. Adaptar `localizacao → local`; `tipo` não tem persistência backend e não será inventado em respostas nem em schema. Remover sua exigência/apresentação persistida nos formulários integrados. Não enviar `formacao` como se fosse campo do cadastro HTTP atual.

Não usar modelo completo de candidato/vaga para descoberta anônima. Representar competências selecionáveis como nomes (`string`), sem catálogo enum; converter objetos `{id, nome}` dos DTOs de consulta para apresentação quando necessário. O gráfico usa interessados anônimos de cada vaga e conserva a contagem por interesse, sem identificar perfis.

Acrescentar testes Spock de serviços, fachada, servlets e JDBC usando as dependências existentes. Integração requer PostgreSQL separado, configurado explicitamente; não usar a conexão fixa do banco de desenvolvimento nos testes. Não criar dependência Testcontainers, framework frontend ou Postman obrigatório. Frontend será validado por compilação e roteiros manuais reproduzíveis, pois não existe suíte automatizada configurada. Não declarar validação manual como teste unitário.

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

### T11 — Completar e verificar a superfície HTTP dos fluxos

- **Objetivo:** conectar capacidades de domínio às rotas de 3.5 e ao navegador.
- **Referência:** seção 8, “Capacidades HTTP necessárias”; seções 3, 6 e 7; CA-01 a CA-18, CA-25.
- **Camada:** Backend.
- **Existentes:** `B/api/servlet/{CandidatoServlet,EmpresaServlet,VagaServlet,CompetenciaServlet}.groovy`, `B/controller/referencia/ReferenciaController.groovy`, `B/service/ReferenciaService.groovy`, `B/dao/referencia/{PaisDAO,EstadoDAO}.groovy`, `B/config/{ApiServletConfig,ControllerConfig,TomcatConfig}.groovy`.
- **Novos:** `B/api/servlet/{RecrutadorServlet,AreaRecrutamentoServlet,ReferenciaServlet,CurtidaServlet,MatchServlet}.groovy`, `B/api/filter/CorsFilter.groovy`, `T/api/servlet/ContratoHttpTest.groovy`.
- **Dependências:** T06 a T10.
- **Passos:** registrar métodos/caminhos da tabela e rotear para controllers/serviços, sem SQL nas servlets; completar leitura de referências persistidas. Padronizar serialização JSON e erros sem vazamento; validar sessão/atividade/escopo em todo caminho protegido. Configurar CORS/preflight/cookies/origem de 3.4. Não registrar endpoints opcionais ou gravação isolada de match.
- **Validação:** testes de parsing, método/rota, campos inválidos, status e lista permitida de propriedades; mocks de servlet não substituem navegador real de T18. Verificar origem permitida, origem rejeitada e preflight.
- **Conclusão:** cada capacidade obrigatória possui rota e consumidor planejado compatíveis; nenhum endpoint antigo evita validação nova.

### Frontend

### T12 — Criar clientes HTTP e adaptar composição assíncrona

- **Objetivo:** estabelecer contratos de transporte e tirar negócio do armazenamento local.
- **Referência:** seções 7 e 8; D-01, D-08; CA-01, CA-04, CA-15, CA-25.
- **Camada:** Frontend.
- **Existentes:** `F/ts/config/{RepositoryConfig,ServiceConfig}.ts`, `F/ts/services/{CandidatoService,EmpresaService,VagaService,CurtidaVagaService,CurtidaCandidatoService,RelatorioCompetenciasService}.ts`, `F/ts/models/{Candidato,Empresa,Vaga,Pessoa,Competencia}.ts` e consumidores existentes desses serviços nas páginas (adaptados nas T13 a T17).
- **Novos:** `F/ts/api/clientes/{ApiClient,CandidatoApiClient,EmpresaApiClient,VagaApiClient,RecrutadorApiClient,ReferenciaApiClient,CompetenciaApiClient,CurtidaApiClient,MatchApiClient,LoginApiClient}.ts`, `F/ts/api/dto/{CadastroRequestDTO,PerfilResponseDTO,VagaRequestDTO,VagaInternaResponseDTO,VagaAnonimaResponseDTO,CandidatoAnonimoResponseDTO,MatchResponseDTO,RecrutadorRequestDTO,ReferenciaResponseDTO,SessaoResponseDTO}.ts`, `F/ts/services/{RecrutadorService,MatchService,CompetenciaService}.ts`.
- **Dependências:** T11.
- **Passos:** implementar fetch/JSON/credenciais e falhas HTTP no cliente base, inclusive respostas sem corpo; criar DTOs para os corpos reais de 3.5. Substituir injeção de repositórios locais nos fluxos integrados por clientes; adaptar `Promise` e os consumidores necessários para manter compilação. Sem remoção indiscriminada de repositórios históricos. Eliminar consultas identificáveis usadas como validação frontend; backend decide conflitos. Mapear explicitamente competências/local e evitar campos sem persistência.
- **Validação:** `npx tsc --noEmit` e `npx gulp build`; inspeção dos imports e dos DTOs de descoberta para ausência de campos identificáveis; verificação manual de erro de rede e resposta vazia. Se necessário, concluir a adaptação mecânica de chamadas nas páginas nesta tarefa; as próximas completam o comportamento/interface.
- **Conclusão:** serviços e páginas compilam com contratos assíncronos; o caminho integrado não confirma gravação local como sucesso HTTP.

### T13 — Integrar cadastros existentes e login por três perfis

- **Objetivo:** permitir entrada real na aplicação com credenciais e referências backend.
- **Referência:** seção 7; seção 8, “Interface”; RN-06 a RN-09; CA-03, CA-04, CA-19 a CA-22.
- **Camada:** Frontend.
- **Existentes:** `F/ts/strategy/login/{LoginStrategy,LoginCandidatoStrategy,LoginEmpresaStrategy}.ts`, `F/ts/pages/LoginPage.ts`, `F/ts/pages/login/{LoginCandidato,LoginEmpresa}.ts`, `F/ts/pages/{candidato/CadastroCandidatoPage,empresa/CadastroEmpresaPage}.ts`, `F/html/{index,cadastro-candidato,cadastro-empresa}.html`, `F/ts/app/App.ts`.
- **Novos:** `F/ts/strategy/login/LoginRecrutadorStrategy.ts`, `F/ts/pages/login/LoginRecrutador.ts`.
- **Dependências:** T12; T14 completa seleção de competências do candidato.
- **Passos:** capturar senha no login e cadastros; acrescentar campos obrigatórios faltantes de candidato; carregar países/estados; mudar Strategy para login assíncrono com e-mail/senha. Acrescentar opção recrutador sem autocadastro. Gravar `{id,tipo}` somente após login confirmado; cadastro leva ao login. Tratar conflito/conta inativa/falha de rede sem persistir credenciais em localStorage.
- **Validação:** compilação/build; manual com cadastros novos, login válido/inválido e inativo dos três perfis, refresh e erros; inspecionar armazenamento/rede para credenciais apenas na entrada prevista, nunca em respostas ou sessão frontend.
- **Conclusão:** navegação segue o perfil autenticado e não há login por e-mail local ou ID fabricado.

### T14 — Substituir catálogo fixo por seletor pesquisável reutilizável

- **Objetivo:** usar competências persistidas em todos os formulários previstos.
- **Referência:** RN-10; seção 8, “Competências e contratos TypeScript”; CA-23 a CA-25.
- **Camada:** Frontend.
- **Existentes:** `F/ts/models/{Competencia,Candidato,Vaga}.ts`, `F/ts/pages/candidato/CadastroCandidatoPage.ts`, `F/ts/pages/empresa/EmpresaVaga.ts`, `F/html/{cadastro-candidato,empresa}.html`, `F/css/style.css`; DTOs/clientes de T12.
- **Novos:** `F/ts/components/SeletorCompetencias.ts`.
- **Dependências:** T12, T13; integração no formulário recrutador em T17.
- **Passos:** carregar `GET /competencias`; filtrar parte do nome no navegador, selecionar por item existente, manter conjunto de nomes sem duplicatas e remover seleção. Entrada não selecionada não integra payload. Usar `string[]` no domínio frontend apropriado e revisar todos os consumidores do enum; não criar competência livre ou busca backend. Preparar componente para os três formulários, sem dependência nova.
- **Validação:** compilação/build; manual com competência de RH/administração preparada no banco, pesquisa parcial, duplicata, remoção, texto livre, lista vazia e falha de carregamento. Verificar payload e relações N:N após submissão.
- **Conclusão:** enum não determina opções válidas e componente pode ser reutilizado pelos três formulários sem cópia de lógica.

### T15 — Integrar descoberta, interesses e matches do candidato

- **Objetivo:** apresentar jornada candidata pelos contratos corretos de visibilidade.
- **Referência:** RN-03 a RN-05; seção 6; CA-08, CA-10, CA-14 a CA-17.
- **Camada:** Frontend.
- **Existentes:** `F/ts/pages/candidato/{CandidatoPage,CandidatoPerfil,CandidatoVaga,CandidatoVagaCurtida,CandidatoComponentes}.ts`, `F/html/candidato.html`, clientes/serviços de T12.
- **Novos:** `F/ts/pages/candidato/CandidatoMatch.ts`.
- **Dependências:** T12, T13.
- **Passos:** carregar perfil próprio separado de vagas anônimas; usar somente ID/descrição/competências nos cards e interesses, sem consulta de vaga completa. Confirmar curtida após backend e manter anonimato na descoberta após match. Acrescentar seção de matches persistidos e seus campos, nome/área do autor quando aplicável. Representar vazio/carregamento/erro e impedir confirmação antecipada.
- **Validação:** compilação/build; navegador com vaga direta e duas vagas da mesma empresa; curtir não revela identidade, retribuir só A revela apenas contrato de match de A; inspecionar respostas também, não apenas DOM.
- **Conclusão:** candidato não obtém identificação fora do match correspondente e refresh mantém o estado persistido.

### T16 — Integrar operação empresarial, cadastro de recrutador e gráfico

- **Objetivo:** permitir que empresa conduza todas as próprias vagas e cadastre autores.
- **Referência:** RN-01 a RN-06; seções 3, 6 e 8; CA-01, CA-05 a CA-07, CA-09, CA-13, CA-14, CA-18.
- **Camada:** Frontend.
- **Existentes:** `F/ts/pages/empresa/{EmpresaPage,EmpresaPerfil,EmpresaVaga,EmpresaCandidato,EmpresaCandidatoCurtido}.ts`, `F/ts/services/RelatorioCompetenciasService.ts`, `F/ts/charts/CompetenciaChart.ts`, `F/html/empresa.html`, `F/css/style.css`.
- **Novos:** `F/ts/pages/empresa/EmpresaRecrutador.ts`.
- **Dependências:** T12 a T14.
- **Passos:** acrescentar formulário empresarial de recrutador com área do backend; empresa de contexto não editável como terceiro arbitrário. Criar vaga direta e listar todas as próprias, distinguindo autores históricos. Carregar interessados por vaga como anônimos e retribuir com candidato/vaga; substituir reconstrução local de reciprocidade por consulta de matches. Recalcular gráfico a partir das competências anônimas por interesse, sem buscar perfis completos. Não acrescentar gestão/reatribuição/edição de recrutadores.
- **Validação:** compilação/build; navegador com erro de área/cadastro, vaga direta e de recrutador, retribuição empresarial nas duas, refresh e autoria após inatividade. Conferir contagem do gráfico contra interesses por vaga, inclusive candidato interessado em duas vagas.
- **Conclusão:** empresa acompanha todas as próprias vagas/matches; gráfico e cards não dependem de acesso identificável indevido.

### T17 — Criar experiência do recrutador com escopo próprio

- **Objetivo:** entregar a jornada de criação, interessados e matches do autor ativo.
- **Referência:** seções 3 e 8; RN-02 a RN-06, RN-10; CA-05 a CA-07, CA-09, CA-14, CA-17, CA-18, CA-25.
- **Camada:** Frontend.
- **Existentes:** `F/ts/app/App.ts`, `F/css/style.css`; cliente/serviço recrutador de T12 e seletor de T14.
- **Novos:** `F/html/recrutador.html`, `F/ts/pages/recrutador/{RecrutadorPage,RecrutadorVaga,RecrutadorInteressados,RecrutadorMatch}.ts`.
- **Dependências:** T13, T14, T15, T16 (reutilizar padrões/contratos já validados).
- **Passos:** acrescentar rota de página, perfil próprio e criação de vaga com seletor; empresa deriva do vínculo backend. Listar somente próprias vagas/interessados/matches. Usar respostas de match para identificação; tratar conta inativa e recurso fora de escopo como falha. Não disponibilizar cadastro de terceiros, vaga direta empresarial ou telas opcionais.
- **Validação:** compilação/build; dois recrutadores da mesma empresa e outro de empresa distinta; alterar IDs em requisições deve ser rejeitado no servidor. Repetição por empresa/recrutador mantém um match. Inativar fixture durante sessão bloqueia operação sem apagar autoria na empresa.
- **Conclusão:** recrutador opera apenas suas vagas e não ganha acesso pela existência de match em outra vaga da mesma empresa.

### Integração e regressão

### T18 — Validar aceite completo e registrar evidências das entregas

- **Objetivo:** demonstrar os fluxos obrigatórios e as invariantes em execução real.
- **Referência:** CA-01 a CA-25; CA-26 condicional excluído; seções 1, 9, 11 e 12.
- **Camada:** Integração.
- **Existentes:** `build.gradle` (configuração de testes somente se necessária ao runtime real), testes acrescentados nas tarefas anteriores, `CHANGELOG.md`, `README.md` (apenas instruções afetadas de execução full-stack, sem credenciais).
- **Novos:** `T/integracao/FluxoHttpRecrutadorTest.groovy`, `docs/specs/recrutador/validacao.md` (roteiros/evidências por CA, criado somente na implementação).
- **Dependências:** T01 a T17.
- **Passos:** executar matriz da seção 5 com banco separado, Tomcat real e navegador. Teste HTTP pode usar `java.net.http.HttpClient`/cookie manager e Spock existentes; inicializar composição com ConnectionFactory de teste explicitamente injetada, sem usar banco fixo de desenvolvimento. Se a composição precisar de construtor adicional para esse fim, manter o construtor atual e documentar a alteração focada. Não presumir tarefa Gradle de integração já existente. Registrar comandos, status, contagens persistidas, falhas e correções sem credenciais. Atualizar changelog por entregas confirmadas e suas referências, sem reescrever histórico alheio.
- **Validação:** estratégia da seção 6; repetir concorrência em conexões independentes, rollback, autoria histórica, reutilização de identificadores, escopo e ausência de campos indevidos no JSON. Rodar regressão de serviços existentes e compilação de todos os consumidores, incluindo terminal, sem adaptar seus menus.
- **Conclusão:** CA-01 a CA-25 têm evidência reproduzível, nenhum obrigatório fica sem validação e nenhuma declaração de sucesso depende apenas de mock/compilação. Divergências impedem aceite até correção; CA-26 permanece explicitamente não aplicável.

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

**Estado nesta elaboração:** somente leitura/inspeção e revisão documental. Nenhum comando de compilação, teste, benchmark, banco ou execução do sistema foi realizado para declarar funcionalidade aprovada. Tokens consumidos não estão disponíveis como medição confiável nesta sessão.

## 7. Riscos e compatibilidade

- **Unicidade entre tabelas:** uma escrita que não obtenha os locks antes da validação compromete a garantia. Auditar os três DAOs e futuras extensões; a região serializada reduz paralelismo de gravação de contas. Validar custos com teste concorrente, sem alegar garantia para SQL administrativo externo.
- **Transações e fechamento:** DAO que abrir outra conexão ou fechar conexão recebida quebra atomicidade. Testar rollback e posse dos recursos. Definir ordem consistente de locks; não misturar derivação de senha com locks prolongados.
- **Dados antigos:** inserts legados de senha simples e curtida global não satisfazem o novo contrato. Usar instalação/teste isolado com novas fixtures; nenhuma tarefa converte, apaga ou reseta registros antigos. Preparação manual de ambiente limpo é pré-requisito de execução, não migração silenciosa.
- **Novas FKs:** preservam reciprocidade, mas exigem revisão do caminho de exclusão física de vaga. Não mudar cascades como atalho e não introduzir retenção/gestão de vagas fora do escopo.
- **Autenticação e navegador:** sessão frontend não autentica requisição. Testar cookie, origem, renovação e conta inativa em HTTP real. A sessão nativa proposta serve exclusivamente ao contexto dos fluxos, sem expandir o escopo para identidade compartilhada.
- **Exposição histórica:** não filtrar autor inativo no JOIN de apresentação; distinguir autor do executor da retribuição. Não serializar entidade para resolver ausência de DTO.
- **Assincronismo:** a alteração dos serviços alcança páginas, gráfico e estratégias. Manter compilação em T12 e completar jornadas em T13–T17; evitar chamadas com Promise ignorada e sucesso antes do backend.
- **Modelos divergentes:** `tipo`/`formacao` frontend não autorizam nova persistência. Campos faltantes do cadastro backend devem ser capturados explicitamente e referências enviadas no formato real.
- **Terminal compartilhado:** compilar o projeto completo e preservar consumidores quando possível; não expandir menus nem manter caminho inseguro para satisfazer compatibilidade. Alteração adicional de escopo deve ser trazida à revisão antes de implementada.
- **Histórico documental:** entradas do changelog não substituem arquivos da branch. Acrescentar evidências das entregas futuras sem afirmar que integrações ausentes já existem.

## 8. Checklist de implementação

- [x] Especificação aprovada lida e checkout `feature/recruiter` conferido.
- [x] Código relevante, schema atual, build, testes e changelog inspecionados.
- [x] Sequência preliminar aprovada pelo responsável antes da geração deste arquivo.
- [ ] Documento completo revisado e aprovado pelo responsável.
- [ ] Validação com Tutor confirmada, conforme pendência registrada na especificação; não presumir confirmação.
- [ ] Commit da especificação explicitamente autorizado e concluído antes de implementar, conforme `skills/feature-com-spec/SKILL.md`, passo 7.
- [ ] T01 — Referências e recrutadores.
- [ ] T02 — Autoria e integridade empresarial.
- [ ] T03 — Unicidades parciais.
- [ ] T04 — Schema de retribuição e match.
- [ ] T05 — Proteção de senhas.
- [ ] T06 — Cadastro e unicidade concorrente entre perfis.
- [ ] T07 — Login e contexto HTTP.
- [ ] T08 — Criação e consulta de vagas.
- [ ] T09 — Interesse, retribuição e match atômicos.
- [ ] T10 — Matches e matriz de visibilidade.
- [ ] T11 — Rotas, contratos e acesso pelo navegador.
- [ ] T12 — Clientes/DTOs e serviços assíncronos.
- [ ] T13 — Cadastros e login frontend.
- [ ] T14 — Seletor de competências.
- [ ] T15 — Jornada do candidato.
- [ ] T16 — Jornada empresarial e gráfico.
- [ ] T17 — Jornada do recrutador.
- [ ] T18 — Aceite, regressão e evidências.
- [ ] Changelog atualizado apenas com tarefas entregues; resultados de validação registrados.
- [ ] Nenhuma implementação ou commit iniciado com base apenas na aprovação da sequência.

Implementar uma tarefa por vez após os gates aplicáveis. Cada commit continua dependendo de autorização explícita; não solicitar nem executar commits nesta tarefa de planejamento.

## 9. Questões abertas ou bloqueios

Não há ambiguidade de negócio identificada que impeça este plano. A exclusão dos endpoints opcionais foi incluída na sequência aprovada; CA-26 não é lacuna do escopo obrigatório.

As decisões técnicas de seção 3, incluindo locks, transação, rotas, sessão nativa e parâmetros do hash, ficam sujeitas à revisão final deste documento. Nenhum bloqueio de implementação foi demonstrado por execução, pois esta fase não executa o sistema.

A confirmação de validação com o Tutor permanece não informada na especificação e não foi inferida da aprovação da sequência. Não bloqueia a redação solicitada do plano, mas deve ser registrada antes dos encaminhamentos de commit previstos na especificação. Disponibilidade de JDK compatível, PostgreSQL separado e referências preparadas é pré-requisito da validação futura, a conferir antes de executar as tarefas correspondentes.
