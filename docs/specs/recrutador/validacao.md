# Validação — Recrutador (T18)

Data: 09/11/2026. Branch: `feature/recruiter`.

Status: **T18 concluída e validada**, com evidências reproduzíveis para CA-01 a CA-25 no catálogo padronizado em maiúsculas confirmado pelo responsável. CA-26 não aplicável. T19 não iniciada.

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
