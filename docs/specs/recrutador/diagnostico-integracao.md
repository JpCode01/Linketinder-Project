# Reconciliação da base frontend — 08/11/2026

## Decisão executada

Por solicitação explícita do responsável, foi desconsiderada a implementação anterior de T12 e importada **somente a pasta frontend de origin/master**, preservando o backend atual de T01–T11. O plano volta ao estado anterior a T12, com essa tarefa e as seguintes revisadas para reutilizar a integração existente.

Nenhum merge, rebase, cherry-pick, staging, commit ou push foi realizado. A branch de trabalho continua sendo `feature/recruiter`. `master` não foi modificada; não houve importação de backend, schema ou testes backend da master.

## Origem e preservação

| Item | Estado verificado |
| --- | --- |
| HEAD da feature | `c0afdb6` — entrega de T11 |
| Master local | `97aa939` — preservada |
| Origin/master após fetch | `da273790fb0c10235981aa78fabe29b9a28310cf` |
| Integração já incorporada em origin/master | Merge `99e1ab4`, conteúdo da branch de integração em `465b24f`; diferença posterior somente no README |
| Base comum da feature e origin/master | `97aa939`; a feature não contém o histórico da integração |
| Snapshot importado | 92 arquivos de `frontend/`, byte a byte iguais ao archive de `da27379` |
| Arquivos exclusivos da base substituída | 17 arquivos retirados após backup, evitando misturar clientes/DTOs e testes da T12 descartada com a integração de master |
| Backend e demais arquivos protegidos | 140 arquivos conferidos por SHA-256: `src/`, SQL, build Gradle e especificação sem alteração |
| T01–T10 no plano | Bloco completo preservado em comparação com HEAD |
| T11 | Backend e evidências mantidos; adicionada apenas orientação documental de compatibilização frontend |
| T12 | Pendente na nova base; testes/resultados da tentativa descartada não contam para sua conclusão |

A importação usou `git archive --format=zip <commit> frontend` e escrita apenas no diretório frontend da feature. Não usou checkout de branch nem atualização do índice. Arquivos de `node_modules/` e `dist/` não foram copiados ou apagados na importação; `dist` foi gerado depois pelo build normal.

Backup local, ignorado pelo Git:

`build/frontend-antes-importacao-20261108T211819903785Z/`

Contém o frontend anterior (sem dependências/saída gerada), documentação anterior, diff binário, status, ZIP do snapshot e `manifest.json` com referências, lista dos arquivos importados/removidos e hashes protegidos. A cópia anterior permite recuperar a tentativa de T12 sem mantê-la ativa no projeto.

## Incompatibilidades concretas e tratamento no plano

Importação fiel não equivale a compatibilidade funcional com o backend atual. Os pontos abaixo foram identificados pela leitura de produtores/consumidores reais e não serão corrigidos por retrocesso do backend.

| Área | Frontend importado | Backend vigente / encaminhamento |
| --- | --- | --- |
| Host e sessão | Clientes e estratégias usam `localhost:8080` sem `credentials: "include"` | T11 exige sessão nativa e origem `http://127.0.0.1:5500`; T12 unifica host/cookie em todos os fetch ativos |
| Tipo de sessão | Estratégias gravam tipos em maiúsculas, páginas esperam maiúsculas | Login backend retorna minúsculas; T12 padroniza contrato e consumidores conjuntamente |
| Interesse candidato | Corpo inclui `idCandidato` e consulta `/curtidas/vagas/verificar` | Backend deriva candidato da sessão, aceita somente `{idVaga}` e não registra essa verificação; T12 usa a listagem anônima de vagas curtidas |
| Retribuição | Corpo inclui `idEmpresa`; botão consulta `/curtidas/candidatos/verificar` sem vaga | Backend recebe somente `{idCandidato,idVaga}`, deriva ator da sessão e garante repetição idempotente; T12 elimina o caminho de verificação global sem criar endpoint |
| Status/corpo | Clientes possuem tratamentos repetidos de falha | T12 conserva `201` vazio nos cadastros e `204` nas curtidas, sem presumir retorno de ID/mensagem; trata `401`, `403` e `409` |
| Catálogo | `CompetenciaApiClient` declara `Competencia[]`; checkboxes usam `.id`/`.nome` | `CompetenciaService.listarCompetencias` backend retorna `List<String>`; T12 corrige catálogo para nomes e adapta consumidores, sem IDs inventados. T14 implementa pesquisa e seleção reutilizável |
| Perfis e datas | DTO candidato declara nascimento como string; existem modelos com campos que não fazem parte da resposta de perfil | T12 confere JSON efetivo e tipa perfil separadamente, sem ampliar resposta backend; não inventar CPF/CNPJ ausentes no perfil para satisfazer modelos |
| Vaga interna e match | DTOs ainda não incluem recrutador | T12 acrescenta os campos já expostos por T11 somente nos contratos adequados; T15/T16 estendem as apresentações existentes |
| Cadastros/login | Formulários já têm senha, referências e cadastro HTTP; tentam login automático após cadastrar | T13 reaproveita formulários, ajusta o encaminhamento ao login previsto no plano e acrescenta entrada de recrutador |
| Interessados/curtidos | `EmpresaCandidatoCurtido` contém leitura de perfil alheio; chamadas ativas de verificação global precisam ser retiradas | T12 rastreia consumidores e impede uso identificável indevido; identificação por `EmpresaMatch`, sem liberar perfil público nem inferir contexto por nomes |
| Componentes existentes | `CandidatoMatch` e `EmpresaMatch` já existem; gráfico já usa interessados anônimos por vaga | T15/T16 estendem e validam o que existe; não recriam componentes/gráfico nem reintroduzem enum de competências |
| Recrutador | Sem contratos/telas frontend próprios | T12 acrescenta contratos/serviços; T13 entrada; T16 cadastro pela empresa; T17 jornada, usando somente rotas existentes |

O plano não exige novamente construção de uma integração a partir de repositórios locais, nem cria camada HTTP concorrente. Reutiliza clientes específicos, DTOs por domínio, serviços assíncronos, estratégias e composição manual da base importada.

## Validação desta operação

- `git fetch origin`: concluído, referências remotas atualizadas; master local/HEAD preservados.
- Conferência do snapshot: mesmos 92 caminhos e mesmos bytes do archive, sem arquivo-fonte adicional da T12 descartada.
- Conferência de preservação: hashes dos 140 arquivos protegidos e referências Git sem alteração; nenhum caminho backend/SQL/spec aparece no diff.
- `npx.cmd tsc --noEmit`, em `frontend`: código 0.
- `npx.cmd gulp build`, em `frontend`: código 0, Webpack compilado. Aviso não impeditivo `DEP0180` de `fs.Stats`.
- `git diff --check`: aponta sete ocorrências de whitespace em linhas importadas de master. Foram preservadas para manter o snapshot fiel; não são divergências introduzidas pela reconciliação. A documentação revisada passa na checagem de whitespace.

Nenhum teste de banco, conexão JDBC, setup/cleanup, migração ou reset foi executado. Nenhuma validação em navegador real foi realizada; TypeScript/Gulp não provam cookie/CORS, autorização ou fluxo persistido. Os 16 testes da T12 descartada não foram reutilizados e não fazem parte do frontend importado.

## Ponto de retomada

A importação e a revisão documental estão concluídas. A compatibilização funcional está explicitamente **pendente em T12**, com critérios novos sobre esta base. Não avançar para T13 nem declarar o frontend compatível com T11 antes de executar e validar essa tarefa. T11 não precisa ser refeita para acomodar contratos antigos; eventual dependência backend genuína exige decisão específica do responsável.
