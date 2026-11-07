# Especificação — Recrutador

Status: aprovado pelo responsável; validação com o Tutor pendente de confirmação.

Branch de referência: `feature/recruiter`.

Data: 07/11/2026.

A especificação foi aprovada explicitamente pelo responsável após a revisão. Essa aprovação não representa confirmação de validação com o Tutor nem autorização para commit ou início de planejamento e implementação.

## 1. Context and Objective

Introduzir o perfil Recrutador no Linketinder, permitindo que uma empresa tenha profissionais vinculados a ela para criar vagas e conduzir o recrutamento dessas vagas. O recrutador possui cadastro e autenticação próprios; não constitui uma segunda conta empresarial.

A empresa permanece proprietária das vagas e acompanha todos os interessados e matches relacionados a elas. O recrutador atua nas próprias vagas. O candidato continua descobrindo vagas anonimamente e só recebe identificação da outra parte após um match válido.

Esta especificação descreve o comportamento futuro da funcionalidade full-stack na branch atual, inclusive os fluxos HTTP e frontend ainda inexistentes. Não é um retrato de funcionalidades já implementadas nem um plano técnico.

O `CHANGELOG.md` na raiz do projeto, cuja criação será realizada pelo responsável, será a referência para o registro da evolução do projeto. O planejamento posterior deverá considerar esse arquivo para registrar as alterações entregues na implementação da feature, mantendo a relação com os requisitos desta especificação.

### Referências verificadas no checkout

- `AGENTS.md` e `skills/feature-com-spec/SKILL.md`: convenções e processo SDD.
- `src/main/groovy/com/jpcode/config/ApiServletConfig.groovy`: registro atual de servlets de candidatos, empresas, vagas e competências.
- `src/main/groovy/com/jpcode/config/{DaoConfig,ServiceConfig,ControllerConfig}.groovy`: composição manual.
- `src/main/groovy/com/jpcode/model/core/{Pessoa,Candidato,Empresa,Vaga}.groovy`: entidades atuais.
- `src/main/groovy/com/jpcode/facade/MatchFacade.groovy` e `service/MatchService.groovy`, sob o mesmo pacote-base: fluxo atual de match.
- `src/main/groovy/com/jpcode/dao/relacionamento/{CandidatoCurtirDAO,EmpresaCurtirDAO}.groovy` e `dao/match/MatchDAO.groovy`: persistência das interações.
- `src/main/groovy/com/jpcode/dto/vaga/{VagaAnonimaDTO,VagaEmpresaDTO}.groovy`, `dto/candidato/CandidatoAnonimoDTO.groovy` e `dto/Match/MatchEncontradoDTO.groovy`: contratos atuais de visibilidade.
- `frontend/src/ts/config/RepositoryConfig.ts`, `strategy/login/` e `pages/`: frontend atualmente baseado em repositórios locais.
- `frontend/src/ts/models/Competencia.ts`, `pages/candidato/CadastroCandidatoPage.ts` e `pages/empresa/EmpresaVaga.ts`: catálogo fixo e seleção atual por checkboxes.
- `src/test/groovy/com/jpcode/service/`: testes de serviços com Spock e mocks.
- `queriesLinketinder.sql`: exclusivamente a seção iniciada pelo marcador exato `-- Versão Atualizada para Criação do projeto`.

## 2. Scope

### Obrigatório

- Cadastro de recrutador pela empresa, com formulário no perfil empresarial, envio HTTP e persistência backend.
- Autenticação por e-mail e senha para candidato, empresa e recrutador, com armazenamento de hash para todas as senhas.
- Vínculo de cada recrutador com uma empresa e uma área de recrutamento.
- Criação de vagas por empresa e recrutador, distinguindo propriedade, autoria e responsabilidade operacional.
- Consulta das próprias vagas pelo recrutador e de todas as vagas da empresa pela conta empresarial.
- Consulta de candidatos que curtiram essas vagas, retribuição por vaga e formação de match único.
- Consulta de matches e exposição de dados conforme a seção 6.
- Unicidade de CPF, CNPJ e e-mail considerando contas ativas e os escopos definidos na seção 4.
- Seletor pesquisável de competências, alimentado pelo banco e reutilizado no cadastro de candidato e na criação de vagas por empresa e recrutador.
- Construção dos fluxos full-stack necessários ao funcionamento desses casos de uso no checkout atual.

### Opcional

Endpoints de edição do próprio perfil do recrutador e troca de sua senha. Sua ausência não impede o aceite do escopo obrigatório. O consumo desses endpoints pelo frontend não faz parte desta entrega. A inclusão opcional deve ser registrada no planejamento posterior; não pressupõe gestão de outros recrutadores.

### Limites

Preservar Groovy, JDBC, servlets, composição manual e TypeScript estrito. Não introduzir frameworks ou dependências sem aprovação correspondente. Não adaptar a versão de terminal. Não converter dados de teste existentes nem elaborar estratégia de migração de credenciais legadas.

## 3. Actors and Permissions

As capacidades abaixo são regras funcionais aplicadas pela lógica das operações e pelas consultas backend. Não se exige um subsistema de autorização, identidade compartilhada ou framework de segurança.

| Ator | Capacidades nesta funcionalidade | Escopo dos dados |
| --- | --- | --- |
| Candidato | Autenticar, consultar vagas anônimas, curtir uma vaga e consultar seus matches | Seus próprios interesses e matches; descoberta sem identificação empresarial ou de recrutadores |
| Empresa | Autenticar, cadastrar recrutadores, criar vagas, consultar interessados, retribuir curtidas e consultar matches | Todas as vagas e matches pertencentes à sua empresa, com ou sem recrutador |
| Recrutador | Autenticar, criar vagas, consultar interessados, retribuir curtidas e consultar matches | Somente suas próprias vagas e os respectivos interessados e matches |

O recrutador recebe as mesmas informações gerais de vaga disponibilizadas à empresa no contexto interno, limitadas às próprias vagas. Pertencer à mesma empresa não libera acesso às vagas ou matches de outro recrutador.

O cadastro de recrutadores é realizado pela empresa, não por autocadastro público nem por outro recrutador. Não se cria um perfil adicional de administrador empresarial.

## 4. Business Rules

### RN-01 — Vínculo e cadastro

Uma empresa pode possuir vários recrutadores. Cada recrutador pertence a exatamente uma empresa e trabalha em exatamente uma área de recrutamento.

O formulário empresarial envia os dados do recrutador ao backend. O backend valida as referências e restrições, protege a senha e persiste o cadastro. Todo cadastro inicial de candidato, empresa ou recrutador começa ativo.

### RN-02 — Propriedade e autoria da vaga

Toda vaga pertence a uma empresa, independentemente de quem a criou.

- Criação direta pela empresa: não há recrutador autor.
- Criação pelo recrutador: a empresa proprietária é aquela à qual ele pertence, e sua autoria deve ser persistida.
- Não é válida uma vaga com empresa proprietária diferente da empresa do recrutador autor.
- A autoria não pode ser removida apenas porque o recrutador ficou inativo.
- A empresa deve conseguir distinguir vagas diretas de vagas com recrutador e identificar esse profissional no contexto interno.

Autoria não concede acesso do candidato à identidade do recrutador antes do match.

### RN-03 — Interesse inicial e destinatários

O interesse inicial é uma curtida do candidato em uma vaga específica. A expressão “candidatos que curtiram” corresponde aos interessados nessa vaga.

| Origem da vaga | Destinatários do interesse |
| --- | --- |
| Criada diretamente pela empresa | Empresa proprietária |
| Criada por recrutador ativo | Empresa proprietária e recrutador autor |
| Criada por recrutador posteriormente inativado | Empresa proprietária continua recebendo e conduzindo os interesses; a autoria histórica permanece |

Uma mesma curtida pode ser consultada pelos destinatários previstos sem representar dois interesses independentes. Não se exige duplicar sua persistência nem gerar notificações para representar o recebimento.

### RN-04 — Retribuição contextualizada

A empresa pode retribuir o interesse de um candidato em qualquer vaga de sua propriedade, inclusive as criadas por recrutadores. O recrutador pode retribuir o interesse em suas próprias vagas.

A retribuição deve identificar o candidato e a vaga de origem. Não existe curtida global de candidato que produza reciprocidade em todas as vagas de uma empresa.

Um candidato pode curtir várias vagas da mesma empresa. Cada vaga constitui um contexto independente: retribuir na vaga A não forma match na vaga B nem identifica o candidato a recrutadores de B.

### RN-05 — Match

O match requer interesse prévio do candidato naquela vaga e retribuição da empresa proprietária ou do recrutador autor apto a operar.

Existe um único registro lógico de match por candidato e vaga, associado à empresa proprietária. Há duas modalidades de apresentação:

- Vaga direta empresarial: match sem recrutador.
- Vaga criada por recrutador: o mesmo match empresarial inclui as informações permitidas desse recrutador.

A participação do recrutador não cria um segundo match independente. A empresa consulta todos os seus matches. O recrutador consulta os matches das próprias vagas. A identidade do recrutador associado decorre da autoria da vaga, mesmo quando a empresa realizou a retribuição.

Repetir a curtida ou a retribuição, inclusive por empresa e recrutador sobre a mesma interação, não cria matches duplicados. Não pode haver match persistido sem os vínculos de interesse e retribuição correspondentes. A técnica para garantir consistência e concorrência será definida no planejamento.

### RN-06 — Inatividade e responsabilidade

Contas inativas não podem autenticar nem ter seu cadastro modificado, incluindo perfil ou senha.

A inatividade do recrutador não impede a continuidade das vagas pela empresa. A responsabilidade operacional passa à empresa, que continua recebendo interessados e pode retribuir suas curtidas. Isso não altera a propriedade, já empresarial, nem apaga o recrutador que criou ou administrou a vaga.

Os matches continuam permitindo à empresa identificar o recrutador histórico, mesmo inativo. Identificação histórica não representa permissão de acesso para a conta inativa.

A administração futura de vagas poderá permitir edição, remoção e alteração de responsável em tela própria. Essa tela e o mecanismo de reatribuição a outro recrutador não pertencem à presente entrega.

### RN-07 — CPF

- CPF deve ser único entre candidatos ativos.
- CPF deve ser único entre recrutadores ativos.
- O mesmo CPF pode pertencer simultaneamente a um candidato ativo e a um recrutador ativo.
- Um CPF existente apenas em conta inativa não impede novo cadastro no mesmo perfil.
- Não existe unicidade global de CPF entre candidatos e recrutadores.

### RN-08 — E-mail

O mesmo e-mail não pode pertencer simultaneamente a duas contas ativas, considerando candidatos, empresas e recrutadores em conjunto.

Recrutadores utilizam e-mail corporativo. Se também possuírem conta ativa de candidato, devem utilizar outro e-mail pessoal nessa conta. A exigência de e-mail corporativo não implica integração de verificação de domínio.

E-mail associado somente a contas inativas pode ser reutilizado. Um histórico associado a uma conta antiga não pode passar à nova conta por igualdade de e-mail.

### RN-09 — CNPJ

CNPJ deve ser único entre empresas ativas. Uma nova conta empresarial pode reutilizar CNPJ e e-mail de uma empresa inativa, respeitada também a unicidade de e-mail entre todos os perfis ativos.

A reutilização de CNPJ ou e-mail não transfere automaticamente vagas, recrutadores ou matches da conta antiga. Os relacionamentos continuam definidos por IDs e FKs.

### RN-10 — Competências

O banco é a fonte das competências selecionáveis, abrangendo tecnologia, recursos humanos, administração e outras áreas. Não se restringe o domínio ao enum tecnológico atual do frontend.

O usuário digita parte do nome, recebe sugestões de competências existentes, seleciona uma delas e a inclui no formulário. O seletor permite múltiplas competências, remoção das selecionadas e evita seleções duplicadas. Texto digitado sem seleção de item existente não constitui competência válida para cadastro.

Competências permanecem referências N:N de candidato e vaga. Área de recrutamento é uma referência distinta: não substitui competências nem exige hierarquia, classificação de competências ou algoritmo de recomendação.

## 5. Data Model

Este é o modelo lógico esperado. Nomes físicos novos, mecanismos SQL, alterações de contratos detalhadas e sequência de implementação serão tratados posteriormente, sem criar migração nesta fase.

### Recrutador

| Campo lógico | Regra |
| --- | --- |
| `id` | Identificador próprio e chave primária |
| `nome` | Nome do profissional |
| `id_area_recrutamento` | FK obrigatória para uma única área de recrutamento |
| `descricao` | Descrição do perfil profissional |
| `email` | E-mail corporativo; exclusividade entre contas ativas dos três perfis |
| `cpf` | Exclusividade entre recrutadores ativos; pode coincidir com candidato |
| `data_nascimento` | Data de nascimento do recrutador |
| `senha` | Representação protegida por hash, com parâmetros necessários à verificação; nunca texto simples |
| `id_empresa` | FK obrigatória para a empresa à qual pertence |
| `ativo` | Estado da conta; verdadeiro no cadastro inicial |

### Área de recrutamento

Possui somente `id` e `nome`. Uma área pode estar vinculada a vários recrutadores. Não existe relação N:N entre recrutador e área.

### Vaga

Preservar os dados atuais de vaga e sua FK empresarial obrigatória. Acrescentar uma referência opcional ao recrutador autor:

- Ausência de recrutador significa autoria direta empresarial.
- Presença de recrutador exige existência do profissional e correspondência com a empresa proprietária.
- Preservar a referência histórica após inatividade.

A empresa proprietária continua explícita também nas vagas diretas. A referência de recrutador registra autoria, não uma segunda propriedade. Não introduzir campos redundantes sem necessidade de integridade demonstrada.

### Curtidas e matches

- Preservar o vínculo candidato–vaga do interesse inicial e sua unicidade.
- A retribuição deve passar a conter o contexto da vaga, corrigindo a persistência empresarial global atual.
- Distinguir o recrutador autor da vaga do ator que executa a retribuição. Não inferir que foram necessariamente a mesma pessoa ou perfil.
- O match relaciona candidato, empresa proprietária e vaga, com recrutador histórico quando aplicável, sem duplicação por destinatário.
- A forma de obter ou persistir a associação do recrutador no match será definida no planejamento, preservando o comportamento desta especificação.
- As referências não podem permitir match de candidato com empresa diferente da proprietária da vaga.

### Integridade de identificadores

As restrições atuais de CPF, CNPJ e e-mail não representam integralmente as regras desejadas: são `UNIQUE` por tabela e não condicionadas a atividade.

Para unicidade entre ativos dentro de uma tabela, índices únicos parciais são uma alternativa compatível com PostgreSQL. Para e-mail entre tabelas, `UNIQUE` independentes não bastam. É necessária uma garantia que também cubra concorrência entre cadastros e eventuais alterações.

| Alternativa analisada | Resultado para o escopo |
| --- | --- |
| Identidade central compartilhada pelos perfis | Não adotada; o responsável excluiu essa estrutura do escopo |
| Validação de aplicação sem sincronização entre escritas | Não garante, isoladamente, a exclusividade concorrente |
| Mecanismo de banco entre tabelas, com sincronização adequada | Alternativa a avaliar no planejamento, mantendo os perfis separados |
| Validação nas operações com serialização transacional efetiva de todas as escritas relevantes | Alternativa a avaliar no planejamento; exige abranger todos os caminhos de gravação |

A especificação exige o resultado de integridade, sem antecipar uma dessas técnicas como decisão aprovada. As mensagens de validação não substituem essa garantia.

## 6. Anonymity and Data Visibility

Anonimato até match válido é uma invariante. A resposta backend deve respeitá-lo; ocultação no DOM não é suficiente. A autoria da vaga e o vínculo empresarial não liberam identificação antes do match.

### Contratos permitidos

- Vaga anônima: somente `id`, `descricao` e `competencias`, preservando `VagaAnonimaDTO`.
- Candidato anônimo: somente `id`, `descricao` e `competencias`, preservando `CandidatoAnonimoDTO`.
- Match: dados previstos em `MatchEncontradoDTO`, acrescidos, quando houver recrutador, somente de seu nome e nome da área de atuação no contrato de apresentação. Não expor CPF, e-mail, nascimento ou descrição do recrutador ao candidato por esse motivo.
- Senhas, hashes e salts nunca integram respostas de consulta, inclusive de perfil próprio ou match.

Os dados existentes de match abrangem nome/sobrenome, e-mail, CPF, descrição, idade, país/estado e competências do candidato; nome, CNPJ, e-mail corporativo, descrição e país/estado da empresa; nome, descrição e competências da vaga.

### Matriz de visibilidade

| Observador e contexto | Antes de match naquela vaga | Depois de match válido naquela vaga |
| --- | --- | --- |
| Candidato, sobre a vaga e a outra parte | ID da vaga, descrição e competências; sem nome da vaga, empresa, localização empresarial, autoria ou dados de recrutador | Identificação e contato empresarial, dados da vaga e demais campos do contrato de match; nome e área do recrutador autor quando houver |
| Empresa proprietária, sobre o candidato interessado | ID, descrição e competências do candidato | Dados do candidato previstos no contrato de match |
| Recrutador autor ativo, sobre o candidato interessado | ID, descrição e competências do candidato, somente nas próprias vagas | Dados do candidato previstos no contrato de match, somente nos matches das próprias vagas |
| Outro recrutador da mesma empresa | Sem acesso ao contexto da vaga alheia | O match não libera acesso à vaga ou à identidade do candidato nesse contexto |
| Empresa proprietária, sobre vagas e autores internos | Dados internos das próprias vagas e identificação do recrutador associado, quando houver | Mantém a identificação interna, inclusive do recrutador histórico inativo |
| Conta autenticada, sobre o próprio cadastro | Dados do próprio perfil; eventual edição depende do escopo opcional e da atividade | O match não amplia o contrato de perfil próprio nem expõe credenciais |
| Conta inativa | Sem login ou operação como titular autenticado | Histórico pode ser consultado pelas partes habilitadas; a conta inativa não recupera acesso |

Um match em uma vaga não amplia a descoberta anônima de outras vagas nem identifica o candidato a outros recrutadores. A identificação deve ser apresentada pelo contrato de match correspondente.

As consultas por ID e listagens de perfis não podem funcionar como caminhos alternativos para obter a identificação da outra parte antes do match. Aplicar as regras pelos contratos, lógica e consultas backend dos fluxos full-stack.

## 7. Authentication and Security

### Autenticação

Os três perfis autenticam por e-mail e senha. O backend verifica a senha informada contra o hash da conta ativa. Identificação apenas por e-mail no frontend não constitui o login especificado.

Preservar a organização frontend por Strategy de login, adaptando-a ao fluxo HTTP e incluindo o perfil recrutador. A representação `{ id, tipo }` em `sessao` pode continuar como estado de navegação da interface; ela não é uma estrutura compartilhada de identidade.

Aplicar os limites funcionais de empresa, recrutador e candidato na lógica das operações e consultas. Não incluir Spring Security, RBAC genérico ou projeto de infraestrutura de autorização nesta feature.

### Proteção das senhas

- Aplicar hash a qualquer senha cadastrada no sistema, independentemente do perfil.
- Usar derivação de senha com salt aleatório individual e fator de trabalho apropriado, permitindo verificar a senha no login.
- Não usar criptografia reversível para armazenamento de senhas.
- Guardar os parâmetros necessários à verificação e à evolução do fator de trabalho junto à representação do hash.
- Não retornar senha, hash ou salt em DTOs de saída, logs, mensagens de erro ou respostas HTTP.
- Remover a senha das mensagens de exceção de login atuais nos fluxos abrangidos.
- Se a troca de senha opcional for incluída, aplicar as mesmas regras e impedir alteração de conta inativa.

Referência técnica proposta para revisão: PBKDF2-HMAC-SHA256 pelas APIs Java, com salt individual obtido de gerador seguro e fator inicial de pelo menos 600.000 iterações, sujeito à avaliação de desempenho no ambiente de execução. A escolha exata dos parâmetros será consolidada no planejamento aprovado.

Essa opção atende à intenção de utilizar recursos Java sem framework adicional. A API Java disponibiliza `SecretKeyFactory`; a OWASP documenta PBKDF2-HMAC-SHA256 e seu fator de trabalho. Referências: [Java SecretKeyFactory](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/javax/crypto/SecretKeyFactory.html) e [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

Não há requisito de migração ou reset de credenciais de registros existentes: os dados atuais são de teste e foram excluídos desse trabalho pelo responsável.

## 8. Frontend and API Impact

### Fronteira atual e resultado esperado

O frontend atual usa repositórios locais e as rotas HTTP existentes cobrem apenas parte dos casos de uso. Os novos fluxos devem consumir o backend e persistir os dados de negócio no PostgreSQL. Não tratar rotas de outra branch como já disponíveis.

Preservar a separação `Página → Service → ApiClient → HTTP → Servlet → Controller → Service → Repository/DAO`. Os clientes HTTP necessários ainda serão definidos e construídos, concentrando comunicação e tratamento de falhas. Preservar os contratos existentes que forem compatíveis com as regras aprovadas; corrigir os que hoje expressam a curtida empresarial global ou exposição inadequada.

### Capacidades HTTP necessárias

| Capacidade futura | Contrato funcional |
| --- | --- |
| Cadastrar recrutador | Receber seus dados no contexto empresarial, validar empresa e área, aplicar unicidade e hash, persistir ativo e responder sem credenciais |
| Consultar áreas de recrutamento | Disponibilizar IDs e nomes persistidos para seleção de uma área no cadastro |
| Autenticar por perfil | Receber e-mail/senha, validar conta ativa e permitir navegação ao perfil correspondente |
| Criar vaga | Receber dados da vaga e competências existentes; distinguir criação empresarial de criação por recrutador e validar o vínculo |
| Consultar vagas internas | Todas da empresa ou somente as próprias do recrutador, conforme o ator |
| Consultar vagas anônimas | Retornar exclusivamente o contrato anônimo ao candidato |
| Curtir vaga | Registrar interesse candidato–vaga sem duplicação |
| Consultar candidatos que curtiram | Retornar candidatos anônimos no escopo da empresa ou do recrutador autor |
| Retribuir interesse | Identificar candidato e vaga, distinguir a atuação empresarial da atuação do recrutador e formar match único |
| Consultar matches | Restringir o conjunto ao candidato, à empresa ou às próprias vagas do recrutador; aplicar o contrato pós-match |
| Editar perfil/trocar senha de recrutador | Opcional; sem interface nesta entrega e sem modificação de conta inativa |

Essa tabela descreve capacidades futuras, não declara endpoints existentes. Métodos, caminhos e contratos novos detalhados serão definidos no planejamento a partir dos registros reais de servlets, sem criar uma especificação de rotas fictícias já implementadas.

### Interface

- Acrescentar criação de recrutador ao perfil empresarial, com seleção de área e envio dos dados ao backend.
- Acrescentar login e experiência do recrutador, com criação e consulta das próprias vagas, interessados e matches.
- Permitir à empresa acompanhar suas vagas e matches, identificando o recrutador quando houver, sem exigir a futura tela administrativa de edição/reatribuição de vagas.
- Exibir o nome e a área do recrutador ao candidato somente no match correspondente.
- Reutilizar o seletor pesquisável nos três formulários de competências previstos.
- Manter erros de carregamento e validação dos fluxos HTTP tratáveis pela interface, sem simular sucesso apenas no armazenamento local.

### Competências e contratos TypeScript

`GET /competencias` já retorna nomes do banco como `string[]`, suficiente para carregar o catálogo pequeno e filtrar sugestões no frontend. Não é necessário um mecanismo de busca backend para esse comportamento.

O enum `Competencia` atual não representa um catálogo aberto a outras profissões. Adaptar explicitamente seus consumidores, incluindo formulários, modelos e apresentação de competências, mantendo a tipagem estrita. O cadastro backend continua recebendo nomes de competências e resolvendo suas referências persistidas. Não trocar automaticamente nomes por IDs nos contratos existentes.

O mapeamento entre o modelo frontend de vaga, que usa `localizacao` e `tipo`, e o backend atual, que usa `local` e não possui `tipo` na entidade, deve respeitar os contratos reais. A feature não autoriza adicionar automaticamente campos ao banco ou aos DTOs anônimos para igualar modelos distintos.

## 9. Impact on Existing Functionality

| Fluxo atual | Comportamento exigido após a feature |
| --- | --- |
| Cadastro e login de candidato/empresa | Continuam disponíveis no full-stack, com senha protegida e unicidade entre ativos |
| Curtida inicial candidato–vaga | Preservar o contexto existente da vaga e a ausência de match nessa etapa |
| Curtida empresarial global | Corrigir para retribuição contextualizada por candidato e vaga; comportamento atual é erro, não regra a preservar |
| Persistência de match | Preservar unicidade e repetição sem duplicação; exigir reciprocidade e propriedade corretas |
| Vaga criada diretamente pela empresa | Continua válida sem recrutador; somente a empresa recebe o interesse |
| Vaga criada por recrutador | Permanece empresarial, com autoria registrada e atendimento conjunto do interesse conforme atividade |
| Descoberta anônima | Preservar DTOs anônimos e impedir consulta alternativa identificável antes do match |
| Catálogo de competências | Substituir dependência do catálogo fixo frontend por competências existentes no banco |
| Dados de teste e terminal | Sem adaptação ou migração nesta entrega |

Não preservar exposição identificável indevida ou login apenas por e-mail como compatibilidade de negócio. O comportamento de apresentação pública passa a seguir a matriz da seção 6.

Não alterar cascades, introduzir exclusão física de recrutadores ou decidir retenção histórica por efeito colateral desta especificação. A referência de autoria necessária aos matches deve permanecer disponível após inatividade.

## 10. Acceptance Criteria

Os critérios abaixo são observáveis e devem orientar a validação futura. Nenhum deles é declarado implementado ou testado por este documento.

| ID | Cenário e resultado esperado | Regras relacionadas |
| --- | --- | --- |
| CA-01 | Empresa cadastra recrutador pelo frontend; o backend persiste os dados, uma empresa, uma área e estado ativo, sem devolver credenciais | RN-01 |
| CA-02 | Empresa ou área inexistente impede cadastro; recrutador não pode pertencer a duas áreas ou duas empresas | RN-01 |
| CA-03 | Candidato, empresa e recrutador ativos autenticam somente com e-mail e senha válidos; conta inativa não autentica | RN-06; seção 7 |
| CA-04 | Senhas dos três perfis são armazenadas como hash; senhas iguais em contas distintas recebem salts individuais; respostas e erros não revelam credenciais | Seção 7 |
| CA-05 | Vaga criada diretamente pela empresa não tem recrutador; vaga criada por recrutador registra autor e sua empresa; combinação entre empresas diferentes é rejeitada | RN-02 |
| CA-06 | Empresa vê todas as próprias vagas; recrutador vê somente as próprias e não consulta interessados ou matches de vaga de outro recrutador | Seção 3 |
| CA-07 | Curtida em vaga de recrutador aparece para a empresa e para o autor ativo sem duplicar o interesse; curtida em vaga direta não é atribuída a recrutador | RN-03 |
| CA-08 | Curtida inicial do candidato, sem retribuição, não cria match nem libera identificação | RN-03; RN-05 |
| CA-09 | Empresa retribui em vaga direta ou de recrutador; recrutador retribui em sua própria vaga; ambos os caminhos formam o mesmo tipo de match único | RN-04; RN-05 |
| CA-10 | Candidato curte duas vagas da mesma empresa; retribuição somente na primeira gera match exclusivamente para ela | RN-04 |
| CA-11 | Retribuição sem interesse prévio naquela vaga ou com empresa incompatível não cria match | RN-05 |
| CA-12 | Repetição de curtidas e retribuições, inclusive concorrentes pela empresa e pelo recrutador, não duplica interesses ou matches | RN-05 |
| CA-13 | Match de vaga com recrutador é um único registro lógico empresarial e inclui nome/área do autor mesmo se a empresa retribuiu | RN-05; seção 6 |
| CA-14 | Empresa consulta todos os seus matches; recrutador consulta somente os das próprias vagas; candidato consulta os seus | RN-05; seção 6 |
| CA-15 | Antes do match, respostas de vagas e candidatos anônimos contêm somente ID, descrição e competências; consultas alternativas não liberam identificação | Seção 6 |
| CA-16 | Após match, são apresentados os campos permitidos pelo contrato; do recrutador, o candidato recebe somente nome e área, sem CPF, nascimento ou e-mail | Seção 6 |
| CA-17 | Match em uma vaga não libera identidade do candidato a outro recrutador nem amplia os DTOs de descoberta anônima | RN-04; seção 6 |
| CA-18 | Dado recrutador inativo, a empresa continua vendo sua autoria histórica, recebendo interessados e formando matches em suas vagas; a conta inativa permanece sem operar | RN-06 |
| CA-19 | Duplicar CPF entre candidatos ativos ou entre recrutadores ativos é rejeitado; mesmo CPF em um candidato ativo e um recrutador ativo é permitido | RN-07 |
| CA-20 | E-mail já usado por conta ativa de qualquer perfil impede novo uso nos outros perfis e no mesmo perfil, inclusive em cadastros concorrentes | RN-08 |
| CA-21 | CPF, e-mail ou CNPJ existente somente em conta inativa pode ser reutilizado conforme seu escopo, sem transferir relacionamentos históricos para a nova conta | RN-07 a RN-09 |
| CA-22 | Duas empresas ativas não compartilham CNPJ | RN-09 |
| CA-23 | O seletor sugere competências do banco por parte do nome, permite seleção múltipla sem repetição e remoção; texto não selecionado não cria competência | RN-10 |
| CA-24 | Competências cadastradas de RH, administração ou outras áreas podem ser selecionadas e persistidas sem alteração do catálogo fixo do frontend | RN-10 |
| CA-25 | Os formulários de candidato e vagas de empresa/recrutador utilizam o catálogo backend e preservam os vínculos N:N | RN-10; seção 8 |
| CA-26 | Se incluídos os endpoints opcionais, edição do próprio perfil e troca de senha respeitam unicidade, hash e impedimento de alteração de conta inativa | RN-06 a RN-08; seção 7 |

## 11. Decisions Made

As decisões de negócio abaixo foram confirmadas pelo responsável durante o esclarecimento, e a especificação foi aprovada integralmente após a revisão. Não foi informada discussão ou validação com o Tutor para nenhuma delas.

| ID | Decisão | Fundamentação | Alternativas consideradas | Estado no diálogo | Tutor |
| --- | --- | --- | --- | --- | --- |
| D-01 | Usar `feature/recruiter` e construir os fluxos full-stack ausentes | A feature deve descrever o resultado futuro na mesma branch | Usar outra branch de integração; limitar-se ao comportamento atual | Confirmada pelo responsável | Não informado |
| D-02 | Empresa 1:N recrutadores; recrutador ligado a uma área | Representar vínculo empresarial e especialização única | Recrutador como outra empresa; múltiplas áreas | Confirmada pelo responsável | Não informado |
| D-03 | Match único por candidato–vaga, empresarial, com recrutador quando aplicável | Empresa acompanha todas as vagas e seus responsáveis | Dois registros independentes de match | Confirmada pelo responsável | Não informado |
| D-04 | Empresa e recrutador autor podem retribuir interesse na vaga | A empresa também conduz vagas criadas por recrutadores | Retribuição exclusiva do autor | Confirmada pelo responsável | Não informado |
| D-05 | Corrigir curtida empresarial global para contexto por vaga | Candidato pode se interessar por diferentes vagas da mesma empresa | Manter reciprocidade global | Confirmada pelo responsável como correção necessária | Não informado |
| D-06 | Empresa vê todas as próprias vagas/matches; recrutador somente os próprios | Delimitar operação e visibilidade | Compartilhar todos os matches entre recrutadores | Confirmada pelo responsável | Não informado |
| D-07 | Candidato vê apenas nome e área do recrutador no match | Definir os campos de identificação liberados | Expor perfil completo ou e-mail/CPF do recrutador | Confirmada pelo responsável | Não informado |
| D-08 | Autenticação por e-mail/senha com hash para todos; sem estrutura compartilhada de identidade ou subsistema de autorização | Escopo definido para os fluxos do projeto; limites aplicados por lógica e consultas | Identidade central; framework de segurança | Confirmada pelo responsável | Não informado |
| D-09 | Unicidade de CPF por perfil e e-mail entre perfis somente para ativos; CNPJ somente entre empresas ativas | Permitir novo cadastro após inativação, inclusive nova conta empresarial | Reserva permanente de identificadores inativos | Confirmada pelo responsável | Não informado |
| D-10 | Cadastro inicial ativo; conta inativa não autentica nem é modificada | Regra expressa de ciclo de vida | Permitir login ou edição de conta inativa | Confirmada pelo responsável | Não informado |
| D-11 | Empresa assume a operação de vagas do recrutador inativo e preserva autoria | Manter continuidade e identificação histórica | Apagar autoria ou impedir continuidade empresarial | Confirmada pelo responsável | Não informado |
| D-12 | Somente endpoints opcionais de edição do próprio perfil e troca de senha; sem consumo frontend nesta entrega | Delimitação final do escopo | Gestão completa e interfaces de edição/desativação agora | Confirmada pelo responsável | Não informado |
| D-13 | Terminal e adaptação dos dados de teste fora do escopo | Foco exclusivo no funcionamento full-stack | Adaptar terminal e migrar credenciais legadas | Confirmada pelo responsável | Não informado |
| D-14 | Seletor pesquisável usa competências existentes no banco e abrange outras profissões | Requisito original, mantido após esclarecimento | Enum fixo tecnológico; competência livre; recomendação avançada | Requisito solicitado | Não informado |
| D-15 | PBKDF2-HMAC-SHA256 como referência técnica de hash | Recursos Java e salt/fator de trabalho sem framework adicional | Outras funções de derivação com dependências adicionais | Proposta técnica para revisão; parâmetros a consolidar no planejamento | Não informado |

## 12. Out of Scope

- Implementação, plano técnico, migrations, alteração de scripts SQL e commits nesta fase de especificação.
- Adaptação funcional ou expansão dos menus de terminal.
- Migração, conversão ou reset dos dados e senhas de teste existentes.
- Estrutura de identidade compartilhada, framework de segurança ou subsistema genérico de autorização.
- Tela futura de administração de vagas, suas operações de edição/remoção e reatribuição de recrutador responsável.
- Gestão de outros recrutadores por recrutadores.
- Interface para edição de perfil e troca de senha de recrutador nesta entrega.
- Fluxos de convite, confirmação por e-mail, recuperação de senha e comprovação de domínio corporativo.
- Gestão de áreas e competências por nova interface administrativa; não é necessário novo CRUD para representar as referências.
- Motores de recomendação, ranking, compatibilidade por competências ou filtros avançados.
- Transferência de histórico por reutilização de CPF, CNPJ ou e-mail.
- Novas regras de reativação, transferência de recrutador entre empresas ou efeitos automáticos de inatividade empresarial sobre as contas vinculadas.
- Exposição de dados adicionais nos DTOs anônimos ou perfil completo de recrutador ao candidato.

## 13. Open Questions

Não restam perguntas críticas de negócio abertas para a redação deste escopo. As confirmações do responsável estão registradas na seção 11.

| Item | Estado | Encaminhamento |
| --- | --- | --- |
| Revisão e aprovação integral desta especificação | Aprovada pelo responsável | Aprovação explícita registrada após revisão |
| Discussão e validação com o Tutor | Não informadas | Aguardar confirmação explícita; não presumir validação |
| Inclusão dos endpoints opcionais de perfil/senha | Opcional, não bloqueia o escopo obrigatório | Registrar escolha no planejamento futuro |
| Caminhos HTTP novos, mecanismo de unicidade entre tabelas, persistência detalhada da retribuição/match e parâmetros finais do hash | Decisões técnicas deliberadamente adiadas | Definir somente no planejamento autorizado, preservando os requisitos deste documento |

Após confirmação de validação com o Tutor, qualquer commit da especificação ainda depende de autorização explícita. A aprovação registrada não inicia planejamento ou implementação.
