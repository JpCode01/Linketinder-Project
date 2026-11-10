# Validação — Recrutador (T18)

Data: 09/11/2026. Branch: `feature/recruiter`.

Status: **T18 concluída e validada**, com evidências reproduzíveis para CA-01 a CA-25 no catálogo padronizado em maiúsculas confirmado pelo responsável. CA-26 não aplicável. A limpeza e revalidação de T19 estão registradas ao final; as evidências anteriores permanecem como histórico.

## Ambiente e segurança do banco

- Windows, Java 25.0.3, Gradle Wrapper 9.3.0 e Node.js 24.18.0. Tomcat e demais bibliotecas nas versões declaradas no projeto.
- Chrome instalado, executado em modo headless via CDP nativo do navegador e WebSocket nativo do Node, sem dependência nova. Cada ator usa contexto isolado de cookies/localStorage.
- Frontend gerado por Gulp/Webpack em `http://127.0.0.1:5500`; Tomcat real em `http://127.0.0.1:8080` para a jornada de navegador. As portas devem estar livres; os testes não encerram servidores preexistentes.
- As três variáveis `LINKETINDER_TEST_JDBC_URL`, `LINKETINDER_TEST_DB_USER` e `LINKETINDER_TEST_DB_PASSWORD` existem no `.env`. Foram carregadas exclusivamente para o processo Gradle, respeitando precedência das variáveis já definidas. O suporte de testes usa `System.getenv()`; não lê `.env` automaticamente.
- Antes das escritas, consulta somente de leitura confirmou que `current_database()` correspondia ao banco dedicado configurado e que havia zero schemas temporários de teste. Nenhum valor de credencial foi registrado.
- `SuportePostgres` exige URL sem parâmetros com banco `linketinder_test` ou sufixo permitido e confere `current_database()` em **cada** conexão. Não usa `PostgresConnectionFactory` como fallback.
- `SuporteContasPostgres` cria schema aleatório `linketinder_test_<32 hex>`, aplica somente o DDL entre o marcador atual e `-- Inserções`, prepara referências próprias e restringe `search_path` ao schema isolado. Conexões concorrentes usam a mesma factory auditada; cleanup valida o nome e remove somente esse schema. Testes de schema de conexão única usam rollback.
- O suporte HTTP foi extraído mecanicamente de T11: composição real de serviços/controllers/servlets e DAOs reais, com todos os getters de `DaoConfig` usados por `ServiceConfig` substituídos pela factory dedicada. O `Stub` configura apenas essa composição; não simula respostas HTTP, regras, JDBC ou transações.
- O processo Node/Chrome não recebe variáveis `DB_*` ou `LINKETINDER_TEST_*`. Senhas de fixture são sintéticas. Logs de diagnóstico imprimem apenas passos, códigos HTTP e contagens; a verificação de hashes/salts retorna booleanos/contagens.

## Preparação e reprodução

Os testes criam seus próprios dados e referências, sem aplicar exemplos SQL legados. Na jornada completa: duas empresas, dois recrutadores da primeira e um da segunda, duas áreas, um candidato, uma vaga direta e três autorais. Competências canônicas: `RH` e `ADMINISTRAÇÃO`; áreas de recrutamento permanecem referências distintas.

Na raiz, carregar somente a configuração de teste do `.env` para o processo PowerShell que executará Gradle:

```powershell
$camposTeste = @('LINKETINDER_TEST_JDBC_URL', 'LINKETINDER_TEST_DB_USER', 'LINKETINDER_TEST_DB_PASSWORD')
Get-Content -LiteralPath .env -Encoding UTF8 | ForEach-Object {
    $linhaTeste = $_.Trim()
    $indiceTeste = $linhaTeste.IndexOf('=')
    if ($indiceTeste -gt 0) {
        $campoTeste = $linhaTeste.Substring(0, $indiceTeste).Trim()
        if ($campoTeste -in $camposTeste -and
            -not [Environment]::GetEnvironmentVariables('Process').Contains($campoTeste)) {
            $valorTeste = $linhaTeste.Substring($indiceTeste + 1).Trim()
            if ($valorTeste.Length -ge 2 -and
                (($valorTeste.StartsWith('"') -and $valorTeste.EndsWith('"')) -or
                 ($valorTeste.StartsWith("'") -and $valorTeste.EndsWith("'")))) {
                $valorTeste = $valorTeste.Substring(1, $valorTeste.Length - 2)
            }
            [Environment]::SetEnvironmentVariable($campoTeste, $valorTeste, 'Process')
        }
    }
}
$env:LINKETINDER_TEST_BROWSER = 'C:/Program Files/Google/Chrome/Application/chrome.exe'
```

Usar o caminho do Chrome/Chromium instalado. O navegador precisa suportar headless/CDP; Node deve oferecer WebSocket nativo (Node 24 utilizado nesta execução). O teste marca ausência de banco ou navegador como **não executado**, nunca como aprovação. O script de navegador deve ser iniciado pelo teste Groovy para usar o Tomcat e o schema isolados.

```powershell
Push-Location frontend
npx.cmd tsc --noEmit
npx.cmd gulp build
node --test tests/transporte-t12.test.cjs tests/entrada-t13.test.cjs tests/seletor-t14.test.cjs tests/candidato-t15.test.cjs tests/empresa-t16.test.cjs tests/recrutador-t17.test.cjs
Pop-Location
.\gradlew.bat test --tests 'com.jpcode.integracao.FluxoHttpRecrutadorTest' --tests 'com.jpcode.integracao.ContratoHttpPostgresTest' --no-daemon
.\gradlew.bat build --rerun-tasks --no-daemon
```

No agente, Gradle exigiu execução fora do sandbox para acessar seu cache. As primeiras chamadas a `npx` foram bloqueadas pela política de scripts PowerShell; os comandos `npx.cmd` acima passaram sem alterar essa política. `npm test` não foi utilizado, pois é placeholder. Dependências frontend já estavam instaladas; nenhum `npm ci` ou atualização de dependências foi necessário.

## Execuções e decisões desta rodada

1. Antes das adições T18, `gradlew.bat build --rerun-tasks --no-daemon`, com banco dedicado, passou: **433 testes, zero falhas, zero ignorados**, incluindo os testes JDBC/HTTP existentes e compilação dos consumidores do terminal. `npx.cmd tsc --noEmit`, `npx.cmd gulp build` e as seis suítes Node passaram (**55 testes frontend**). Avisos de acesso nativo do Java e depreciação `fs.Stats` do empacotamento não impediram a execução.
2. Primeira compilação do novo teste falhou por sintaxe de mensagem em asserção Spock; corrigida para `assert condição: mensagem`, sem alteração de produção. Uma execução intermediária foi interrompida antes de produzir relatório novo; não foi contabilizada como aprovação.
3. Primeira jornada Chrome cadastrou empresas e três recrutadores, mas não confirmou cadastro candidato com a competência de fixture `Administração`. Reprodução HTTP/JDBC isolada confirmou **400**, **uma conta candidata** e **um vínculo** (`RH`); a segunda competência não foi resolvida. Isso ocorreu porque a consulta existente converte o argumento para maiúsculas e compara exatamente com o nome armazenado. O schema desta reprodução foi removido pelo cleanup dedicado.
4. O responsável rejeitou alterar a busca e confirmou a padronização em maiúsculas. **Decisão preservada:** não alterar backend, não migrar/normalizar dados existentes e não modificar a especificação. As fixtures passaram a armazenar `ADMINISTRAÇÃO`, mantendo a pesquisa frontend parcial `admi` para comprovar o seletor. O aceite usa o catálogo padronizado confirmado; a falha anterior permanece registrada e não foi apresentada como aprovação.
5. Na primeira reexecução com catálogo canônico, a jornada passou por cadastros, quatro vagas, curtidas e três matches, mas o teste aguardava a página protegida intermediária mesmo quando o redirecionamento por 403 já havia ocorrido. Ajustada somente a espera para observar diretamente a página de login; nenhum comportamento da aplicação alterado.
6. Reexecução direcionada de `FluxoHttpRecrutadorTest` e `ContratoHttpPostgresTest`: **BUILD SUCCESSFUL; três testes, zero falhas, zero ignorados**. O log do Chrome terminou em `ACEITE_NAVEGADOR_T18_OK`; as verificações JDBC e a continuidade após inatividade também passaram. O cenário e todas as asserções de T11 foram comparados ao HEAD e preservados integralmente após a extração do suporte.
7. Regressão final: `.\gradlew.bat build --rerun-tasks --no-daemon` — **BUILD SUCCESSFUL**, **435 testes**, **zero falhas, erros ou ignorados**; 175 testes no pacote `com.jpcode.integracao` (inclui também as checagens sem conexão de segurança do suporte). Todos os consumidores, inclusive terminal, recompilados. Relatório JaCoCo gerado. A repetição final de `npx.cmd tsc --noEmit`, `npx.cmd gulp build` e das seis suítes Node também passou: **55 testes frontend**, zero falhas/ignorados. `node --check frontend/tests/aceite-t18.cjs` e `git diff --check` passaram.
8. Consulta final confirmou o banco dedicado correto, mas encontrou um schema residual após a execução interrompida. Auditoria constatou 13 tabelas da fixture, nenhuma empresa/recrutador/candidato/vaga, apenas países/estados/áreas/competências de teste, proprietário correspondente ao usuário de teste, nenhuma FK externa e nenhuma outra conexão ao banco. Removido somente esse schema aleatório validado, em transação com timeout de lock e revalidação de ausência de contas, vagas e conexões. Consulta pós-cleanup confirmou **zero schemas temporários residuais**. Uma tentativa de consulta de referências via psql encontrou incompatibilidade de codificação do terminal; repetida com SQL ASCII e `chr()` para os caracteres acentuados, sem alterar dados. Não houve limpeza de dados de desenvolvimento.

Contagens e observações confirmadas na execução direcionada:

| Momento | Esperado | Observado |
| --- | --- | --- |
| Cadastro HTTP isolado com catálogo canônico | 201, um candidato e dois vínculos | 201, um candidato e dois vínculos |
| Após os formulários e logins da jornada | Seis contas ativas, hashes PBKDF2 e seis salts distintos; três recrutadores nas empresas/áreas certas | Verificação SQL agregada verdadeira, sem expor hashes ou salts |
| Quatro vagas criadas pelas interfaces | Quatro vagas e oito vínculos N:N; candidato com dois vínculos | 4 / 8 / 2 |
| Interesse inicial | Quatro interesses únicos, zero match antes de retribuir | 4 / 0 no HTTP; repetição confirmou 204 |
| Retribuições empresarial/autoral/direta | Três retribuições e três matches; repetição não aumenta contagem | 3 / 3, inclusive após repetição pelo autor |
| Inatividade do autor após login HTTP | Sessão e login 401; empresa continua e match conserva nome/área históricos | 401 / 401; nova retribuição 204, quatro matches totais, nome/área históricos preservados |
| Cookies no navegador | HttpOnly, SameSite=Lax, inacessível a `document.cookie`, renovação em novo login | Todas as asserções verdadeiras em Chrome real |

## Matriz reproduzível de aceite

As evidências JDBC citadas abaixo são testes reais, com schemas isolados. “HTTP T11” refere-se a `ContratoHttpPostgresTest`, com persistência real. `ContratoHttpTest` complementa com transporte Tomcat real e controllers mockados para parsing, métodos, CORS e falhas controladas. Testes unitários e DOM/fetch controlados são regressão complementar. A nova jornada Chrome/Tomcat/JDBC e a regressão final completa passaram.

| CA | Procedimento e resultado esperado | Evidência e estado |
| --- | --- | --- |
| 01 | Empresa cadastra recrutadores no formulário; vínculo único empresarial/área, ativos, sem credenciais na resposta | Chrome confirmou os três cadastros; SQL confirmou atividade e vínculos empresariais/áreas. `UnicidadeContaTest` e HTTP T11 passaram. |
| 02 | Rejeitar referências inexistentes e vínculos múltiplos/incompatíveis | `SchemaRecrutadorTest`, `UnicidadeContaTest` e HTTP T11 passaram; FKs, campos estranhos e área inválida. |
| 03 | Login dos três perfis somente com senha válida e conta ativa | JDBC de senhas, recrutador e HTTP T11 passaram; Chrome autenticou os três perfis e recebeu 401 para senha errada nos três, sem sessão em consultas protegidas; autor inativado rejeitado em HTTP. |
| 04 | Hash e salt individual; contratos e erros sem credenciais | `SenhaPersistenciaTest`, `UnicidadeContaTest`, serviços/servlets passaram; SQL confirmou seis contas com hashes e salts distintos. Erros de login contêm somente `erro`; perfis/matches seguem listas permitidas. |
| 05 | Vaga direta sem autor; autoral com empresa do vínculo; divergência rejeitada | `VagaAutoriaTest`, schema e HTTP T11 passaram; Chrome criou quatro vagas, confirmou `[null, 1, 2]` na empresa e rejeitou injeção de empresa divergente com 400. |
| 06 | Empresa vê todas as próprias; autores apenas suas vagas/interessados/matches | JDBC/HTTP T11 passaram; Chrome confirmou listas dos três autores, rejeições 403 de colega/outro vínculo e redirecionamento por localStorage adulterado. |
| 07 | Empresa e autor observam o mesmo interesse, sem duplicação; direta só empresarial | `RetribuicaoMatchTest` e HTTP T11 passaram; Chrome confirmou igualdade do interesse para empresa/autor; SQL confirmou quatro interesses únicos. |
| 08 | Curtida inicial não gera match nem identificação | JDBC/HTTP T11 passaram; Chrome confirmou quatro curtidas, zero match e descoberta/interessados com chaves anônimas exatas. |
| 09 | Retribuição por empresa em direta/autoral e por autor em própria vaga | `RetribuicaoMatchTest` passou nas duas ordens e vaga direta; Chrome formou os três matches por esses caminhos, confirmados no SQL. |
| 10 | Match somente na vaga A, mantendo B independente | JDBC/HTTP T11 passaram; Chrome verificou um match após retribuir A, nenhum match do colega B e interessados de B anônimos. |
| 11 | Sem interesse ou empresa incompatível, nenhum match/retribuição | JDBC/HTTP T11 passaram com contagens zero; Chrome confirmou 409/403 e lista de matches ainda vazia após as tentativas. |
| 12 | Repetição e concorrência não duplicam interesse/retribuição/match | `RetribuicaoMatchTest` passou com duas conexões, início coordenado e contagem final um; Chrome/HTTP repetiram interesse e retribuição por outro ator, mantendo 4 interesses/3 retribuições/3 matches. |
| 13 | Empresa retribui na vaga autoral: match único com nome/área do autor | `ConsultaMatchTest` e HTTP T11 passaram; Chrome apresentou `Autor T18` no match formado pela empresa; SQL confirmou unicidade. |
| 14 | Consultas por titular e rejeição de IDs alheios | JDBC/HTTP T11 passaram; Chrome confirmou três matches para candidato/empresa, um para cada autor habilitado, zero para outro vínculo e 403 para IDs alheios. |
| 15 | Chaves anônimas exatamente `id`, `descricao`, `competencias`, inclusive caminhos alternativos | JDBC/HTTP T11 passaram, inclusive GET por ID e perfis alheios; Chrome comparou chaves exatas de descoberta/curtidas antes/depois de match e interessados. |
| 16 | Todos os campos autorizados no match; recrutador somente nome/área | `ConsultaMatchTest` e contratos HTTP passaram; Chrome comparou todas as chaves de match e cada valor autorizado nos cards de candidato, empresa e autor, com autoria anulável em vaga direta. |
| 17 | Match A não identifica candidato no contexto de B/colega, nem amplia descoberta | JDBC/HTTP T11 passaram; Chrome confirmou contexto B ainda anônimo, rejeitou colega na vaga A e manteve descoberta anônima após os três matches. |
| 18 | Autor inativado perde sessão/login; empresa continua operando e preserva autoria | JDBC/HTTP T11 passaram; HTTP pós-jornada T18 confirmou 401/401 do autor e nova retribuição empresarial 204 com nome/área históricos. |
| 19 | CPF único por perfil ativo; coincidência candidato/recrutador permitida | `UnicidadeContaTest` e schema passaram; Chrome cadastrou e autenticou candidato e recrutador com CPF coincidente. |
| 20 | E-mail ativo único nos três perfis, inclusive concorrência | `UnicidadeContaTest` passou para todos os pares, atualizações versus cadastros e conexões independentes. |
| 21 | Reutilização após inatividade com IDs novos, sem transferir histórico | `UnicidadeContaTest` e schema passaram para os três perfis e identificadores. |
| 22 | CNPJ não se repete entre empresas ativas | `UnicidadeContaTest` e schema passaram, incluindo concorrência e UPDATE. |
| 23 | Pesquisa parcial, seleção múltipla, remoção, sem duplicata ou texto livre cadastrado | 55 checagens frontend passaram; Chrome pesquisou `admi`, selecionou dois nomes, confirmou ausência de sugestão duplicada, removeu/reselecionou e deixou texto livre; SQL confirmou só dois vínculos. |
| 24 | Competências RH/administração persistidas e selecionáveis sem catálogo frontend fixo | Fixture inicial fora do padrão falhou; decisão de maiúsculas registrada. Chrome e HTTP/JDBC passaram com `RH`/`ADMINISTRAÇÃO` adicionadas somente ao schema de teste. |
| 25 | Três formulários usam catálogo HTTP; vínculos N:N preservados | HTTP isolado confirmou um candidato/dois vínculos; Chrome usou os três formulários, SQL confirmou dois vínculos candidatos e oito vínculos de quatro vagas. |
| 26 | Opcional excluído | Não aplicável. Sem PUT de recrutador/perfil/senha no registro de rotas; Chrome recebeu 405 nas duas tentativas, sem implementar o opcional. |

Além dos CAs: `RetribuicaoMatchTest` passou na falha JDBC entre INSERTs com rollback completo e repetição bem-sucedida, proteção por locks e exclusão transacional. `UnicidadeContaTest` passou em rollback/liberação de locks, disputa de identificadores e falhas reais de INSERT/UPDATE. Esses cenários foram repetidos com sucesso na regressão final após as adições T18. CORS/preflight/origem rejeitada foram repetidos nos testes de transporte Tomcat; cookies/credenciais/origem permitida e renovação de sessão também foram verificados no Chrome.

## Artefatos e limites

- Testes/relatórios: `build/test-results/test/`, `build/reports/tests/test/` e `build/reports/jacoco/test/`. Gradle substitui relatórios a cada seleção; a execução final completa deve ser a referência final.
- Navegador: `build/navegador-t18.log`; perfil Chrome temporário próprio em `build/chrome-t18-*`, sem usar perfil pessoal. O teste encerra somente seu processo de navegador, servidor estático e Tomcat.
- Não há declaração de inspeção visual humana ou de validação com o Tutor. O Chrome executa a interface real e verifica DOM, cookies, navegação, chamadas reais e resultados, sem mocks.
- Nenhum bloqueio permanece no escopo de T18 com o catálogo canônico confirmado. Nomes de competências fora dessa padronização não foram normalizados automaticamente; sua rejeição e a gravação parcial observada na fixture inicial permanecem documentadas, sem alteração do comportamento que o responsável determinou preservar.
- Backend de produção, schema, dependências, especificação e menus do terminal preservados. T19 não iniciada; nenhum `git add`, `git commit` ou `git push` executado.
- Consumo de tokens sem medição confiável disponível.

## Complemento de T18 — Bug dos ícones de curtida (09/11/2026)

Após a conclusão inicial, o responsável encontrou e autorizou corrigir a perda do estado dos botões antes de T19. A causa estava na reconstrução dos cards empresarial/autoral com `false`, sem consulta de retribuição persistida. O candidato já consultava vagas curtidas; faltava um estilo explícito para o estado confirmado nos três perfis.

- Backend: `GET /curtidas/candidatos/vaga/{idVaga}/retribuidos` responde `200` com lista de IDs de candidatos ativos retribuídos naquela vaga (`[]` quando vazia). Sessão ativa e validação existente de propriedade/autoria são obrigatórias; consulta de estado não amplia DTOs anônimos nem acessa perfis identificáveis. A empresa e o autor veem a mesma retribuição, mesmo quando registrada pelo outro ator autorizado. Banco, INSERTs, transações e unicidade permanecem preservados.
- Frontend: consulta antes de renderizar os interessados, associação por candidato/vaga, coração preenchido, fundo vermelho e botão desabilitado tanto após confirmação HTTP quanto após recarga. Falha na consulta não assume ausência de curtida; falha da mutação permite nova tentativa. Candidato reutiliza a lista anônima de vagas curtidas.
- `npx.cmd tsc --noEmit` e `npx.cmd gulp build`: passaram. `node --test tests/*.test.cjs`: **57 testes, zero falhas/ignorados**. Regressões verificam recarga, ausência de segundo POST, independência entre vagas e tratamento de falhas.
- `.\gradlew.bat build --rerun-tasks --no-daemon`, carregando somente `LINKETINDER_TEST_*` conforme roteiro acima e usando Chrome: **BUILD SUCCESSFUL, 439 testes, zero falhas/erros/ignorados**. HTTP/JDBC verificaram a nova rota vazia e preenchida, repetição sem duplicatas, isolamento por vaga, `401` sem sessão, `403` para candidato/empresa alheia/colega, `400` para ID inválido, `404` para vaga ausente e continuidade empresarial com autor inativo. Quatro cenários unitários novos validam o escopo da fachada antes da consulta.
- Aceite Chrome real: nova navegação de candidato, empresa, autor e colega recupera o coração preenchido, a cor computada vermelha e o botão desabilitado; inclui retribuição empresarial reconhecida pelo recrutador autor. Log: `T18 regressão: candidato, empresa e autores restauram cor vermelha, coração preenchido e bloqueio após recarga.` e `ACEITE_NAVEGADOR_T18_OK` em `build/navegador-t18.log`.
- `node --check frontend/tests/aceite-t18.cjs` e `git diff --check`: passaram. A execução inicial de Gradle no sandbox foi bloqueada pelo cache; repetida com permissão fora do sandbox. `postman status` não é suportado pelo CLI instalado; a verificação HTTP foi executada pelas suítes existentes de Tomcat/JDBC e Chrome, sem inicializar workspace ou adicionar dependências.

Este complemento registra a alteração pontual de backend autorizada para o bug após as evidências originais de T18, que permanecem acima como histórico. T19 não iniciada; alterações preexistentes preservadas; nenhum staging, commit ou push executado.

## T19 — Inventário e revalidação da limpeza final (09/11/2026)

**Estado: T19 concluída e validada.** Branch conferida: `feature/recruiter`; checkout inicialmente limpo. Escopo autorizado: somente T19. Aplicada `skills/feature-com-spec/SKILL.md`, prevalecendo a proibição expressa de staging/commit/push. A especificação não foi modificada. As referências T01–T18 acima descrevem a situação anterior à limpeza.

### Inventário, consumidores e decisões

Convenções: `B = src/main/groovy/com/jpcode`, `T = src/test/groovy/com/jpcode`, `F = frontend/src/ts`. As expansões entre chaves representam arquivos ou métodos individuais.

| Localização / comportamento anterior | Substituto vigente e consumidores rastreados | Decisão T19 |
| --- | --- | --- |
| `F/repository/CadastroRepository.ts`; `candidato/{CandidatoRepository,CurtidaVagaRepository}.ts` e `contrato/{CandidatoConsultaRepository,CurtidaVagaConsultaRepository}.ts`; `empresa/{EmpresaRepository,CurtidaCandidatoRepository}.ts` e `contrato/{EmpresaConsultaRepository,CurtidaCandidatoConsultaRepository}.ts`; `vaga/VagaRepository.ts` e `contrato/VagaConsultaRepository.ts` (todos sob `repository/`). Gravação e consulta de negócio em Storage, IDs locais. | `main.ts → App → páginas → ServiceConfig → serviços → ApiClients`. Cadastros, perfis, vagas e interesses usam HTTP; logins usam Strategies HTTP. Imports antigos se restringem ao próprio conjunto local; nenhuma factory de repositório é composta, registrada ou selecionada por página, rota, teste ou configuração atual. Webpack parte de `main.ts`; testes carregam os módulos reais explicitamente. | Removidos os 11 arquivos; não substituídos por outra implementação local. |
| `F/factory/storage/{StorageFactory,LocalStorageFactory}.ts`. Factory de Storage para os repositórios antigos. | Únicos consumidores são os cinco repositórios removidos; nenhum consumidor em sessão, Strategy, ServiceConfig ou teste. | Removidos ambos. `localStorage` de navegação permanece nas Strategies e em `SessaoNavegacao`. |
| `F/models/{Candidato,Empresa,Pessoa,Vaga,CurtidaCandidato,CurtidaVaga}.ts`. Modelos usados pelo armazenamento local. | Referenciados somente pelos repositórios removidos, seus contratos e entre si. Fluxos atuais usam DTOs HTTP específicos de entrada, perfil, descoberta, vaga interna e match. | Removidos os seis arquivos. Mantidos `Competencia`, `Pais` e `Estado`, referências usadas nos contratos/páginas atuais. |
| `F/exceptions/{CnpjJaCadastradoException,CpfJaCadastradoException,EmailJaCadastradoException}.ts`, `candidato/{CandidatoJaCurtidoException,CandidatoNaoEncontradoException}.ts`, `empresa/EmpresaNaoEncontradaException.ts`, `vaga/{VagaJaCurtidaException,VagaNaoEncontradaException}.ts` (subdiretórios sob `exceptions/`). Validações/exceções do antigo fluxo local. | Nenhum import, instanciação ou carregamento dinâmico atual. Transporte usa `ApiError`; os três logins ainda usam `LoginException`. Conflitos e repetição são tratados pelos contratos HTTP/backend. | Removidos os oito arquivos; `LoginException` mantida. |
| `F/pages/empresa/EmpresaCandidatoCurtido.ts`. Alias de `exibirMatchesEmpresa`, mantido na transição T12. | `App → EmpresaPage → EmpresaMatch.exibirMatchesEmpresa`; nenhum consumidor da reexportação na aplicação. A única referência era uma asserção de igualdade de funções em `transporte-t12.test.cjs`. | Arquivo e somente essa asserção removidos. Testes públicos de apresentação/consulta de matches em T15–T18 preservados. |
| `F/pages/empresa/EmpresaPage.ts`: import e instância de `EmpresaService` sem leitura. | Perfil usa `EmpresaApiClient`; cadastro usa `CadastroEmpresaPage → ServiceConfig.criarEmpresaService`. | Removidas somente importação/instanciação sem uso; serviço e factory de cadastro mantidos. |
| `F/pages/empresa/EmpresaVaga.configurarCriacaoVaga`: parâmetro `idEmpresa` não lido após T12. | Empresa operante vem da sessão backend; `EmpresaPage`, teste de transporte e teste do seletor eram os três chamadores. A checagem adicional `tsc --noUnusedLocals --noUnusedParameters` identificou o parâmetro. | Removidos parâmetro e argumentos dos três consumidores, sem mudar payload, chamada HTTP ou autorização. Repetidos build e testes frontend após o ajuste; revalidação real registrada abaixo. |
| `B/service/EmpresaService.curtirCandidato(Long,Long)`. Assinatura global que apenas lançava exceção. | Nenhum consumidor de produção; teste exclusivo verificava o bloqueio. `CurtidaServlet → MatchController.retribuirInteresse{Empresa,Recrutador} → MatchFacade`, com ator/candidato/vaga e transação, é o fluxo vigente. Terminal usa outro método, de três IDs, no controller. | Método e teste exclusivo removidos; nenhum caminho global reativado. |
| `B/service/EmpresaService`: campo, import, parâmetro e atribuição de `CandidatoCurtidasConsultaRepository`. | Campo não era lido após bloqueio da consulta sem ator. Serviço composto em `ServiceConfig`, `EmpresaServiceTest` e `SenhaPersistenciaTest`; consultas autorizadas usam o repositório em `MatchFacade`. | Injeção removida e os três construtores consumidores ajustados. Contrato/DAO continuam ativos na fachada. |
| `B/controller/match/MatchController.salvar` e `B/service/MatchService.salvar(Long,Long,Long)`. Entrada direta bloqueada desde T09. | Controller não tem consumidor em servlets, terminal ou composição; serviço só era chamado pelo controller e teste exclusivo. Não há rota de gravação direta. Fachada usa `salvarNaTransacao → MatchRepository.salvar(Connection,...)`. | Duas assinaturas e teste exclusivo removidos. Escrita transacional, FKs, validações, rollback e conflitos idempotentes preservados. |
| `B/dao/contratos/VagaRepository.salvar(Vaga)` e `B/dao/vaga/VagaDAO.salvar(Vaga)`. Alias para persistir com lista vazia. | `VagaService.gravarVaga → salvarComCompetencias`, inclusive quando a seleção é vazia; nenhum consumidor do alias em serviços, controllers, terminal, composição ou testes. | Declaração e alias removidos; criação transacional com competências preservada. |
| `B/exception/{candidato/CandidatoLoginException,empresa/EmpresaLoginException}`: construtores `(String email,String senha)` que apenas delegavam ao construtor vazio. | Serviços e fixtures HTTP instanciam o construtor vazio; terminal apenas captura os tipos. Únicos usos com argumentos eram duas asserções por serviço. | Removidos os construtores e essas quatro asserções. Testes de login público incorreto/inativo e mensagem genérica exata permanecem. |
| Imports em `B/api/servlet/{CandidatoServlet,EmpresaServlet}` (`JsonSlurper`, `HttpServlet`), `CompetenciaServlet` (`JsonOutput`, `HttpServlet`), `LoginServlet` (`HttpServlet`) e `T/api/servlet/ContratoHttpTest` (`RecrutadorViewDTO`, `IdentificadorEmUsoException`). | Parsing/JSON e herança comuns estão em `JsonServlet`; nenhum uso restante desses imports nos arquivos. | Removidos nove imports; nenhuma alteração dos handlers, códigos HTTP ou contratos. |
| Comentários em Groovy de produção/testes, TS, CJS, HTML e CSS. | Inspeção dos delimitadores antes da edição; preservados strings, regex, URLs, padrões de rotas, conteúdo renderizado, seletores, asserções e fixtures. | Removidos todos os comentários desses fontes, conforme orientação adicional de T19. Trinta e nove arquivos modificados contêm exclusivamente essa remoção; outros seis também tinham comentários e ajustes inventariados acima. SQL/documentação e scripts fornecidos pelo Gradle permanecem fora da limpeza: o plano proíbe alterar schema, e os marcadores SQL delimitam o DDL usado pelo suporte. |

Componentes expressamente mantidos:

| Componente | Consumidor atual e necessidade |
| --- | --- |
| `EmpresaController/EmpresaService.buscarCandidatosQueCurtiram(Long)` | `MenuCurtirCandidato` ainda chama essa assinatura sem ator. Mantida a rejeição por exceção, sem acessar interessados. Não é código sem consumidor; removê-la exigiria adaptar o terminal, fora do escopo. O teste do bloqueio foi mantido e verifica ausência de acesso à retribuição. |
| `MatchController/MatchFacade.curtirCandidato(Long,Long,Long)` | `MenuCurtirCandidato` chama o controller; delegação empresarial segura/contextualizada na fachada. Alias necessário à compatibilidade do terminal. |
| `MatchController/MatchService.verMatchesPor{Empresa,Candidato}` | `MenuEmpresaCrud` e `MenuCandidatoCrud`; delegam à consulta vigente com validação de atividade. |
| `EmpresaController/EmpresaService.buscarCandidatosCurtidos`, `RetribuicaoRepository.buscarCurtidas` e implementação em `EmpresaCurtirDAO` | `MenuCurtirCandidato.verCandidatosCurtidos`; consulta anônima ainda usada, sem nova rota HTTP. Factory/injeção de retribuição continuam necessárias. |
| `VagaController/VagaService.criarVaga`, `listarVagas`, CRUD e referências/competências | Consumidores dos menus de vaga/candidato/empresa e serviços ativos. Não retirar por serem anteriores à feature. |
| `ReferenciaRepository.buscar()` padrão; construtor de `MatchService` com repositórios opcionais; construtor padrão de `VagaDAO` | Contrato compartilhado de referências e consumidores reais das fachadas/testes JDBC. Construtor de match só para escrita não libera consulta sem os validadores. Não substituir contratos ativos como efeito da limpeza. |
| `ServiceConfig` frontend, incluindo factories de empresa/login; Strategies e sessão de navegação | Cadastro empresarial e testes de transporte atuais; `LoginException` e `localStorage` de `sessao` continuam necessários. Nenhum `RepositoryConfig` existe ou foi criado. |
| `DaoConfig`, `ControllerConfig`, `MenuFactory`, configurações e menus do terminal, `Main`, `RestServer`, registros HTTP e seleção de páginas | Composição manual e entradas vigentes. `RestServer` é entrada externa pela IDE/classpath mesmo sem uma chamada textual a partir de outra classe. Nenhum menu alterado. |
| DAOs de contas, autoria, referências, retribuição e match; DTOs; schema e dependências declaradas | Consumidores vigentes e garantias de persistência/visibilidade. Sem atualização de bibliotecas, remoção de JARs históricos, mudança de schema/dados, arquitetura ou negócio. |

O rastreamento incluiu os caminhos dinâmicos Groovy `consultarMatches${tipo}`, `buscarMatchesPor${tipo}` e invocação dos métodos HTTP pelos testes; todos continuam apontando a métodos mantidos. Não há carregador reflexivo da aplicação que selecione os componentes removidos. A decisão combina leitura de produtores/consumidores, composição, entradas externas, rotas, bundle e testes; não decorre apenas de busca textual/compilação. Nenhum candidato inventariado fica sem decisão, e nenhuma remoção depende de adaptar um consumidor válido.

### Segurança e execução após as remoções

Reauditados setup, cleanup e todas as origens de conexão dos testes. `SuportePostgres` permanece a única abertura JDBC real da integração e verifica configuração explícita, URL sem parâmetros, nome dedicado permitido e `current_database()` em cada conexão. `SuporteContasPostgres` usa schema aleatório validado e `search_path` sem `public`; cleanup apenas desse schema. Casos de schema de conexão única usam rollback. `SuporteHttpPostgres` injeta todos os DAOs/getters usados pela composição com essa factory; sem fallback. O teste de configuração da factory usa driver controlado, e testes unitários/HTTP de contrato usam mocks sem conexão real.

O carregador temporário `build/validar-t19.ps1` lê somente as três variáveis de teste do `.env` para o processo, preserva a precedência do ambiente e não imprime valores. Antes do Gradle, valida novamente o formato da URL e consulta `current_database()` e schemas residuais usando `psql -X -w` com host/porta/banco/usuário explícitos. A consulta não escreve dados. Exige banco correto e zero schemas aleatórios residuais. `DB_*` da aplicação não é usado. Chrome usa perfil/contextos próprios e não recebe configuração JDBC. Nenhum dado/schema de desenvolvimento foi alterado.

Primeira regressão após as remoções iniciais: `.\build\validar-t19.ps1`, executando `.\gradlew.bat build --rerun-tasks --no-daemon`, passou com **437 testes em 23 suítes, zero falhas/erros/ignorados**, incluindo 175 no pacote de integração e `ACEITE_NAVEGADOR_T18_OK`. A contagem anterior ao T19 era 439; retirados somente os dois casos exclusivos das assinaturas bloqueadas removidas. Essa rodada precede os últimos ajustes de imports, construtores legados de exceção e alias `VagaDAO.salvar`; a execução final é registrada abaixo.

### Resultado final e critérios de conclusão

| Comando/verificação efetivamente executado | Resultado e alcance |
| --- | --- |
| `.\build\validar-t19.ps1`, que executa `.\gradlew.bat build --rerun-tasks --no-daemon` com configuração dedicada e Chrome | Execução final, após o último ajuste frontend: **BUILD SUCCESSFUL em 4m13s; 437 testes em 23 suítes; zero falhas, erros ou ignorados**, conferidos nos XMLs. Todas as cinco tarefas efetivamente executadas; compilação Groovy/testes, JAR e JaCoCo gerados. Uma rodada intermediária após os últimos ajustes backend também passou; a última rodada repetiu tudo com o bundle frontend final. |
| `npx.cmd tsc --noEmit`, em `frontend` | Passou após a limpeza inicial. |
| `npx.cmd tsc --noEmit --noUnusedLocals --noUnusedParameters`, em `frontend` | Checagem adicional inicialmente identificou somente `idEmpresa` não lido em `configurarCriacaoVaga`; após remover parâmetro/argumentos, **passou** no código final. Nenhuma opção do projeto foi alterada. |
| `npx.cmd gulp build`, em `frontend` | **Passou**, inclusive após o último ajuste; Webpack 5.110.3 compilou. Somente aviso não impeditivo `DEP0180` de `fs.Stats`. `dist/` gerado pelo build. |
| `node --test tests/transporte-t12.test.cjs tests/entrada-t13.test.cjs tests/seletor-t14.test.cjs tests/candidato-t15.test.cjs tests/empresa-t16.test.cjs tests/recrutador-t17.test.cjs`, em `frontend` | **57 testes passaram**, zero falhas/cancelados/ignorados. Repetição final com os mesmos arquivos e `--test-reporter=dot` passou após ajustar o parâmetro. DOM/fetch controlados; mensagens de erro esperadas nos cenários negativos. |
| `node --check frontend/tests/aceite-t18.cjs` | Passou, código zero. |
| `java -cp <conteúdo de build/runtime-t01.txt> groovy.ui.GroovyMain build/composicao-t19.groovy` | **COMPOSICAO_T19_OK**: instancia composição real e `MenuFactory`, cria os dois menus e confere wiring e ausência das assinaturas removidas. Abertura de conexão substituída por erro/contador; **zero conexões**. Valida composição e compatibilidade do terminal, sem alegar jornada interativa ou adaptar menus. |
| `.\build\validar-t19.ps1 -SomenteAuditoria`, antes e após as execuções | **AUDITORIA_T19_OK**: `current_database()` corresponde ao banco dedicado permitido; **zero schemas temporários residuais**, inclusive após o último build. Nenhuma escrita nessas consultas. |
| Revisão integral de `git diff`, `git diff --check`, buscas de referências/comentários e comparação com HEAD dos arquivos protegidos | Sem erros de whitespace ou referências ativas aos arquivos removidos; zero comentários nos fontes da aplicação/testes. Especificação, SQL, AGENTS, dependências, rotas/composição não afetada e menus preservados. Índice Git vazio; nenhum staging/commit/push. |

O script PowerShell local foi inicialmente bloqueado pela política do sandbox. Auditoria e Gradle foram executados com a permissão solicitada fora dele, sem alterar políticas permanentes. O build emite o aviso de acesso nativo do Java já existente, sem falha. Nenhum teste foi apresentado como aprovado quando bloqueado ou não executado.

Os 175 casos no pacote de integração incluem sete checagens de segurança do suporte sem banco, 65 de schema, 44 de unicidade/concorrência de contas, 30 de retribuição/transação, 11 de autoria, nove de consultas de match, seis de senha persistida e três HTTP/JDBC (dois em `FluxoHttpRecrutadorTest`, um em `ContratoHttpPostgresTest`). As demais suítes cobrem serviços, fachada, configuração e HTTP/Tomcat de contratos com controllers mockados. A jornada Chrome/Tomcat/JDBC real terminou em `ACEITE_NAVEGADOR_T18_OK` no log final `build/navegador-t18.log`.

| Critério/invariante de T19 | Evidência final |
| --- | --- |
| Inventário completo e remoções justificadas, incluindo dependências e chamadas dinâmicas | Tabela de decisões acima, leitura de produtores/consumidores/composição/rotas/entradas e diff integral. Cada componente removido tem substituto vigente; componentes atuais explicitamente mantidos. |
| Preservação de contratos e anonimato | `ConsultaMatchTest`, `ContratoHttpPostgresTest` e Chrome comparam chaves exatas de descoberta/curtidas/interessados e todos os campos permitidos dos matches; A não identifica candidato em B. Nenhum DTO anônimo foi ampliado. |
| Autorização, atividade e autoria histórica | Suítes de serviço, JDBC, HTTP e Chrome rejeitam colega, outra empresa, titular/ID adulterado e sessão inválida; inativação rejeita login/sessão e empresa continua formando match com autor histórico. |
| Integridade, concorrência, rollback e idempotência | `RetribuicaoMatchTest` (30), `UnicidadeContaTest` (44) e schema repetidos em PostgreSQL dedicado, incluindo falha entre INSERTs, exclusão transacional, locks, disputa entre conexões e nenhuma duplicação. |
| Jornadas atuais e regressão do bug de curtidas | Chrome final exercita cadastros/logins, vaga direta/autoral, seletor nos três formulários, N:N, interessados e matches; recarga mantém coração preenchido/vermelho/desabilitado para candidato, empresa e autor. SQL confere quatro vagas/oito vínculos, dois vínculos candidatos, quatro interesses/três matches, com quarto match empresarial após inatividade. |
| Terminal e composição mantidos | Compilação completa e smoke real de composição dos menus sem banco; nenhuma mudança nos menus/factory/entrada. Consulta sem ator continua bloqueada. |
| Segurança do banco e encerramento | Configuração e cada conexão auditadas antes das escritas; schemas exclusivos, sem fallback; consulta pós-execução confirma zero resíduos. Nenhum schema/dado de desenvolvimento alterado. |
| Documentação, escopo e parada | T19 marcada somente após as verificações finais; changelog acrescenta somente remoções reais. Spec e histórico T01–T18 preservados, sem tarefa posterior ou operação Git de escrita. |

Sem bloqueio ou validação obrigatória pendente de T19. Mantido o limite anterior do catálogo canônico em maiúsculas, sem alterar busca, migrar dados ou reabrir a decisão do responsável. Não houve inspeção visual humana ou validação com Tutor; o aceite executado é automatizado em Chrome real. Consumo de tokens sem medição confiável disponível.

### Arquivos da entrega T19

Nenhum fonte versionado novo. Relação integral dos arquivos removidos/modificados; os grupos e motivos estão no inventário acima. Scripts auxiliares de auditoria/composição permanecem somente em `build/`, ignorado pelo Git.

| Estado | Arquivo (relativo à raiz) |
| --- | --- |
| Modificado | `CHANGELOG.md` |
| Modificado | `docs/specs/recrutador/validacao.md` |
| Modificado | `frontend/src/css/style.css` |
| Modificado | `frontend/src/html/candidato.html` |
| Modificado | `frontend/src/html/empresa.html` |
| Modificado | `frontend/src/ts/api/clientes/ApiClient.ts` |
| Modificado | `frontend/src/ts/api/dto/candidato/CandidatoViewDTO.ts` |
| Modificado | `frontend/src/ts/components/SeletorCompetencias.ts` |
| Removido | `frontend/src/ts/exceptions/CnpjJaCadastradoException.ts` |
| Removido | `frontend/src/ts/exceptions/CpfJaCadastradoException.ts` |
| Removido | `frontend/src/ts/exceptions/EmailJaCadastradoException.ts` |
| Removido | `frontend/src/ts/exceptions/candidato/CandidatoJaCurtidoException.ts` |
| Removido | `frontend/src/ts/exceptions/candidato/CandidatoNaoEncontradoException.ts` |
| Removido | `frontend/src/ts/exceptions/empresa/EmpresaNaoEncontradaException.ts` |
| Removido | `frontend/src/ts/exceptions/vaga/VagaJaCurtidaException.ts` |
| Removido | `frontend/src/ts/exceptions/vaga/VagaNaoEncontradaException.ts` |
| Removido | `frontend/src/ts/factory/storage/LocalStorageFactory.ts` |
| Removido | `frontend/src/ts/factory/storage/StorageFactory.ts` |
| Removido | `frontend/src/ts/models/Candidato.ts` |
| Removido | `frontend/src/ts/models/CurtidaCandidato.ts` |
| Removido | `frontend/src/ts/models/CurtidaVaga.ts` |
| Removido | `frontend/src/ts/models/Empresa.ts` |
| Removido | `frontend/src/ts/models/Pessoa.ts` |
| Removido | `frontend/src/ts/models/Vaga.ts` |
| Removido | `frontend/src/ts/pages/empresa/EmpresaCandidatoCurtido.ts` |
| Modificado | `frontend/src/ts/pages/empresa/EmpresaPage.ts` |
| Modificado | `frontend/src/ts/pages/empresa/EmpresaVaga.ts` |
| Modificado | `frontend/src/ts/pages/login/FormularioLogin.ts` |
| Modificado | `frontend/src/ts/pages/login/SessaoNavegacao.ts` |
| Removido | `frontend/src/ts/repository/CadastroRepository.ts` |
| Removido | `frontend/src/ts/repository/candidato/CandidatoRepository.ts` |
| Removido | `frontend/src/ts/repository/candidato/CurtidaVagaRepository.ts` |
| Removido | `frontend/src/ts/repository/candidato/contrato/CandidatoConsultaRepository.ts` |
| Removido | `frontend/src/ts/repository/candidato/contrato/CurtidaVagaConsultaRepository.ts` |
| Removido | `frontend/src/ts/repository/empresa/CurtidaCandidatoRepository.ts` |
| Removido | `frontend/src/ts/repository/empresa/EmpresaRepository.ts` |
| Removido | `frontend/src/ts/repository/empresa/contrato/CurtidaCandidatoConsultaRepository.ts` |
| Removido | `frontend/src/ts/repository/empresa/contrato/EmpresaConsultaRepository.ts` |
| Removido | `frontend/src/ts/repository/vaga/VagaRepository.ts` |
| Removido | `frontend/src/ts/repository/vaga/contrato/VagaConsultaRepository.ts` |
| Modificado | `frontend/tests/aceite-t18.cjs` |
| Modificado | `frontend/tests/candidato-t15.test.cjs` |
| Modificado | `frontend/tests/empresa-t16.test.cjs` |
| Modificado | `frontend/tests/entrada-t13.test.cjs` |
| Modificado | `frontend/tests/recrutador-t17.test.cjs` |
| Modificado | `frontend/tests/seletor-t14.test.cjs` |
| Modificado | `frontend/tests/transporte-t12.test.cjs` |
| Modificado | `src/main/groovy/com/jpcode/api/SessaoHttp.groovy` |
| Modificado | `src/main/groovy/com/jpcode/api/servlet/CandidatoServlet.groovy` |
| Modificado | `src/main/groovy/com/jpcode/api/servlet/CompetenciaServlet.groovy` |
| Modificado | `src/main/groovy/com/jpcode/api/servlet/EmpresaServlet.groovy` |
| Modificado | `src/main/groovy/com/jpcode/api/servlet/JsonServlet.groovy` |
| Modificado | `src/main/groovy/com/jpcode/api/servlet/LoginServlet.groovy` |
| Modificado | `src/main/groovy/com/jpcode/api/servlet/RecrutadorServlet.groovy` |
| Modificado | `src/main/groovy/com/jpcode/config/ServiceConfig.groovy` |
| Modificado | `src/main/groovy/com/jpcode/controller/match/MatchController.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/conta/ValidacaoContaDAO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/contratos/RetribuicaoRepository.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/contratos/VagaRepository.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/match/MatchDAO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/recrutador/RecrutadorDAO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/relacionamento/CandidatoCurtirDAO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/vaga/CompetenciasVagaDAO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dao/vaga/VagaDAO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/database/PostgresConnectionFactory.groovy` |
| Modificado | `src/main/groovy/com/jpcode/dto/recrutador/RecrutadorViewDTO.groovy` |
| Modificado | `src/main/groovy/com/jpcode/exception/candidato/CandidatoLoginException.groovy` |
| Modificado | `src/main/groovy/com/jpcode/exception/empresa/EmpresaLoginException.groovy` |
| Modificado | `src/main/groovy/com/jpcode/facade/MatchFacade.groovy` |
| Modificado | `src/main/groovy/com/jpcode/service/EmpresaService.groovy` |
| Modificado | `src/main/groovy/com/jpcode/service/MatchService.groovy` |
| Modificado | `src/main/groovy/com/jpcode/service/RecrutadorService.groovy` |
| Modificado | `src/main/groovy/com/jpcode/service/SenhaService.groovy` |
| Modificado | `src/main/groovy/com/jpcode/service/VagaService.groovy` |
| Modificado | `src/test/groovy/com/jpcode/api/servlet/ContratoHttpTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/api/servlet/VisibilidadeServletTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/ConsultaMatchTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/ContratoHttpPostgresTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/FluxoHttpRecrutadorTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/RetribuicaoMatchTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/SchemaRecrutadorTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/SenhaPersistenciaTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/SuporteContasPostgres.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/SuporteHttpPostgres.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/SuportePostgres.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/UnicidadeContaTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/integracao/VagaAutoriaTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/service/CandidatoServiceTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/service/EmpresaServiceTest.groovy` |
| Modificado | `src/test/groovy/com/jpcode/service/MatchServiceTest.groovy` |
| Modificado | `docs/specs/recrutador/plano.md` |
