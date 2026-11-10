const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const http = require('node:http')
const { spawn } = require('node:child_process')
const { once } = require('node:events')
const { setTimeout: pausa } = require('node:timers/promises')

const origem = 'http://127.0.0.1:5500'
const raiz = path.resolve(__dirname, '../dist')
const artefatos = fs.mkdtempSync(path.resolve('build/chrome-t18-'))
const senha = 'senha-t11'
const anonimo = ['competencias', 'descricao', 'id']
const camposMatch = ['id', 'nomeCandidato', 'sobrenomeCandidato', 'emailCandidato', 'cpfCandidato',
    'descricaoCandidato', 'idadeCandidato', 'estadoCandidato', 'paisCandidato', 'competenciasCandidato',
    'nomeEmpresa', 'cnpjEmpresa', 'emailCorporativo', 'descricaoEmpresa', 'paisEmpresa', 'estadoEmpresa',
    'nome', 'descricao', 'competencias', 'nomeRecrutador', 'areaRecrutamento'].sort()
let navegador, socket, id = 0
const pendentes = new Map()
const sessoes = new Map()
const servidor = http.createServer((req, res) => {
    const destino = path.resolve(raiz, '.' + new URL(req.url, origem).pathname)
    if (!destino.startsWith(raiz + path.sep) || !fs.existsSync(destino) || !fs.statSync(destino).isFile()) {
        res.writeHead(404).end(); return
    }
    res.setHeader('Content-Type', ({ '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.css': 'text/css' })[path.extname(destino)] || 'application/octet-stream')
    fs.createReadStream(destino).pipe(res)
})

function cdp(method, params = {}, sessionId) {
    const numero = ++id
    return new Promise((resolve, reject) => {
        const timer = setTimeout(() => { pendentes.delete(numero); reject(Error('Timeout CDP: ' + method)) }, 15000)
        pendentes.set(numero, { resolve, reject, timer })
        socket.send(JSON.stringify({ id: numero, method, params, ...(sessionId ? { sessionId } : {}) }))
    })
}

async function avaliar(aba, expression) {
    const resultado = await cdp('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true }, aba.sessao)
    if (resultado.exceptionDetails) throw Error('Falha ao executar ação no navegador')
    return resultado.result.value
}

async function esperar(aba, expression, descricao) {
    const limite = Date.now() + 20000
    while (Date.now() < limite) {
        try { if (await avaliar(aba, expression)) return } catch (erro) {
            if (!/context|Cannot find|Inspected target/i.test(erro.message)) throw erro
        }
        await pausa(100)
    }
    throw Error('Timeout funcional: ' + descricao)
}

async function abrir(aba, pagina) {
    await cdp('Page.navigate', { url: origem + '/html/' + pagina + '.html' }, aba.sessao)
    await esperar(aba, `location.pathname === '/html/${pagina}.html' && document.readyState === 'complete'`, 'abrir ' + pagina)
}

async function criarAba() {
    const { browserContextId } = await cdp('Target.createBrowserContext')
    const { targetId } = await cdp('Target.createTarget', { url: 'about:blank', browserContextId })
    const { sessionId } = await cdp('Target.attachToTarget', { targetId, flatten: true })
    const aba = { sessao: sessionId, contexto: browserContextId, erros: 0, alertas: [] }
    sessoes.set(sessionId, aba)
    await cdp('Page.enable', {}, sessionId)
    await cdp('Runtime.enable', {}, sessionId)
    return aba
}

async function preencher(aba, campos) {
    await avaliar(aba, `(() => { const campos = ${JSON.stringify(campos)}; for (const [id, valor] of Object.entries(campos)) {
        const campo = document.getElementById(id); if (!campo) throw Error('Campo ausente');
        campo.value = valor; campo.dispatchEvent(new Event('input', { bubbles: true }));
        campo.dispatchEvent(new Event('change', { bubbles: true }));
    } })()`)
}

async function clicar(aba, seletor) {
    assert(await avaliar(aba, `(() => { const elemento = document.querySelector(${JSON.stringify(seletor)});
        if (!elemento || elemento.disabled) return false; elemento.click(); return true })()`), 'Elemento habilitado: ' + seletor)
}

async function requisitar(aba, metodo, rota, dados) {
    return avaliar(aba, `(async () => { const resposta = await fetch('http://127.0.0.1:8080' + ${JSON.stringify(rota)}, {
        method: ${JSON.stringify(metodo)}, credentials: 'include',
        ${dados === undefined ? '' : `headers: { 'Content-Type': 'application/json' }, body: ${JSON.stringify(JSON.stringify(dados))},`}
    }); const texto = await resposta.text(); return { status: resposta.status, dados: texto ? JSON.parse(texto) : null } })()`)
}

async function consultar(aba, rota) {
    const resposta = await requisitar(aba, 'GET', rota)
    assert.equal(resposta.status, 200, 'GET ' + rota)
    return resposta.dados
}

async function autenticar(aba, perfil, email) {
    await abrir(aba, 'index')
    await preencher(aba, { email, senha })
    await clicar(aba, '#login-' + perfil)
    await esperar(aba, `location.pathname.endsWith('/${perfil}.html') && document.getElementById('profile-name')?.textContent.includes('T18')`, 'login ' + perfil)
    const sessao = await avaliar(aba, 'JSON.parse(localStorage.getItem("sessao"))')
    assert.deepEqual(Object.keys(sessao).sort(), ['id', 'tipo'])
    assert.equal(sessao.tipo, perfil)
    const { cookies } = await cdp('Storage.getCookies', { browserContextId: aba.contexto })
    assert(cookies.some(c => c.name === 'JSESSIONID' && c.httpOnly && c.sameSite === 'Lax'), 'Cookie real HttpOnly/SameSite=Lax')
    assert.equal(await avaliar(aba, 'document.cookie.includes("JSESSIONID")'), false)
}

async function selecionar(aba) {
    await esperar(aba, '!!document.querySelector("#skills-options input")', 'catálogo HTTP')
    await avaliar(aba, `(() => { const input = document.querySelector('#skills-options input');
        input.value = 'admi'; input.dispatchEvent(new Event('input')); })()`)
    assert.deepEqual(await avaliar(aba, 'Array.from(document.querySelectorAll(".competencias-sugestoes button"), b => b.textContent)'), ['ADMINISTRAÇÃO'])
    await clicar(aba, '.competencias-sugestoes button')
    assert.equal(await avaliar(aba, 'document.querySelectorAll(".competencias-selecionadas button").length'), 1)
    await clicar(aba, '.competencias-sugestoes button')
    assert.equal(await avaliar(aba, 'document.querySelectorAll(".competencias-selecionadas button").length'), 2)
    assert.equal(await avaliar(aba, 'document.querySelectorAll(".competencias-sugestoes button").length'), 0)
    await clicar(aba, '.competencias-selecionadas button')
    await clicar(aba, '.competencias-sugestoes button')
    await avaliar(aba, `(() => { const input = document.querySelector('#skills-options input');
        input.value = 'Texto livre sem seleção'; input.dispatchEvent(new Event('input')); })()`)
    assert.equal(await avaliar(aba, 'document.querySelectorAll(".competencias-selecionadas button").length'), 2)
}

async function cadastrarEmpresa(aba, email, cnpj) {
    await abrir(aba, 'cadastro-empresa')
    await esperar(aba, '!document.getElementById("register-company").disabled', 'referências empresariais')
    await preencher(aba, { name: 'Empresa T18', email, password: senha, cnpj, country: 'BRASIL', state: 'SP', cep: '00000000', description: 'Descrição empresarial T18' })
    await clicar(aba, '#register-company')
    await esperar(aba, 'location.pathname.endsWith("/index.html")', 'confirmação de cadastro empresarial')
    assert.equal(await avaliar(aba, 'localStorage.getItem("sessao")'), null)
    await autenticar(aba, 'empresa', email)
}

async function cadastrarRecrutador(aba, nome, email, cpf, area) {
    await esperar(aba, '!document.getElementById("cadastrar-recrutador").disabled', 'áreas persistidas')
    await preencher(aba, { 'recruiter-name': nome, 'recruiter-email': email, 'recruiter-cpf': cpf,
        'recruiter-birthdate': '1990-01-01', 'recruiter-description': 'Perfil privado T18', 'recruiter-password': senha, 'recruiter-area': String(area) })
    await clicar(aba, '#cadastrar-recrutador')
    await esperar(aba, 'document.getElementById("recruiter-status").textContent.includes("com sucesso")', 'cadastro de recrutador confirmado')
}

async function criarVaga(aba, nome, perfil) {
    await esperar(aba, '!document.getElementById("criar-vaga").disabled', 'formulário de vaga')
    await preencher(aba, { 'job-title': nome, 'job-location': 'Local privado T18', 'job-description': 'Descrição ' + nome })
    await selecionar(aba)
    await clicar(aba, '#criar-vaga')
    await esperar(aba, 'document.getElementById("job-title").value === ""', 'publicação confirmada')
    if (perfil === 'empresa') await abrir(aba, 'empresa')
    await esperar(aba, `document.getElementById('job-list').textContent.includes(${JSON.stringify(nome)})`, 'vaga persistida na lista')
}

async function main() {
    await new Promise((resolve, reject) => { servidor.once('error', reject); servidor.listen(5500, '127.0.0.1', resolve) })
    navegador = spawn(process.argv[2], ['--headless=new', '--disable-gpu', '--no-first-run', '--no-default-browser-check',
        '--disable-background-networking', '--remote-debugging-address=127.0.0.1', '--remote-debugging-port=0',
        '--user-data-dir=' + artefatos, 'about:blank'], { windowsHide: true, stdio: 'ignore' })
    navegador.on('error', () => { console.error('Não foi possível iniciar o navegador instalado.'); process.exitCode = 1 })
    const portaArquivo = path.join(artefatos, 'DevToolsActivePort')
    for (let tentativa = 0; tentativa < 100 && !fs.existsSync(portaArquivo); tentativa++) await pausa(100)
    assert(fs.existsSync(portaArquivo), 'Chrome deve disponibilizar CDP local')
    const [porta, endpoint] = fs.readFileSync(portaArquivo, 'utf8').trim().split(/\r?\n/)
    socket = new WebSocket(`ws://127.0.0.1:${porta}${endpoint}`)
    await once(socket, 'open')
    socket.addEventListener('message', evento => {
        const mensagem = JSON.parse(evento.data)
        if (mensagem.id) {
            const chamada = pendentes.get(mensagem.id)
            if (!chamada) return
            clearTimeout(chamada.timer); pendentes.delete(mensagem.id)
            if (mensagem.error) chamada.reject(Error(mensagem.error.message))
            else chamada.resolve(mensagem.result)
        } else if (mensagem.method === 'Page.javascriptDialogOpening') {
            sessoes.get(mensagem.sessionId)?.alertas.push(mensagem.params.type)
            cdp('Page.handleJavaScriptDialog', { accept: true }, mensagem.sessionId).catch(() => {})
        } else if (mensagem.method === 'Runtime.exceptionThrown') {
            const aba = sessoes.get(mensagem.sessionId); if (aba) aba.erros++
        }
    })

    const empresa = await criarAba(), outraEmpresa = await criarAba(), candidato = await criarAba()
    const autor = await criarAba(), colega = await criarAba(), outroAutor = await criarAba()
    await cadastrarEmpresa(empresa, 'empresa@t18.invalid', 'cnpj-a-t18')
    await cadastrarEmpresa(outraEmpresa, 'outra@t18.invalid', 'cnpj-b-t18')
    await cadastrarRecrutador(empresa, 'Autor T18', 'autor@t18.invalid', 'cpf-compartilhado-t18', 1)
    await cadastrarRecrutador(empresa, 'Colega T18', 'colega@t18.invalid', 'cpf-colega-t18', 2)
    await cadastrarRecrutador(outraEmpresa, 'Outro Autor T18', 'outro-autor@t18.invalid', 'cpf-outro-t18', 1)
    console.log('CA-01/02: recrutadores cadastrados pelo formulário empresarial com áreas distintas.')

    await abrir(candidato, 'cadastro-candidato')
    await esperar(candidato, '!document.getElementById("register-candidate").disabled', 'referências de candidato')
    await preencher(candidato, { name: 'Pessoa T18', surname: 'Candidata', email: 'candidato@t18.invalid', password: senha,
        cpf: 'cpf-compartilhado-t18', 'birth-date': '1990-01-01', country: 'BRASIL', state: 'SP', cep: '00000000', description: 'Descrição candidata T18' })
    await selecionar(candidato)
    await clicar(candidato, '#register-candidate')
    await esperar(candidato, 'location.pathname.endsWith("/index.html")', 'cadastro candidato confirmado')
    assert.equal(await avaliar(candidato, 'localStorage.getItem("sessao")'), null)
    await autenticar(candidato, 'candidato', 'candidato@t18.invalid')
    await autenticar(autor, 'recrutador', 'autor@t18.invalid')
    await autenticar(colega, 'recrutador', 'colega@t18.invalid')
    await autenticar(outroAutor, 'recrutador', 'outro-autor@t18.invalid')
    const semSessao = await criarAba()
    await abrir(semSessao, 'index')
    for (const [perfil, email] of [['empresa', 'empresa@t18.invalid'], ['candidato', 'candidato@t18.invalid'], ['recrutador', 'autor@t18.invalid']]) {
        const resposta = await requisitar(semSessao, 'POST', '/login/' + perfil, { email, senha: 'incorreta-t18' })
        assert.equal(resposta.status, 401)
        assert.deepEqual(Object.keys(resposta.dados), ['erro'])
        assert(!JSON.stringify(resposta.dados).includes('incorreta-t18'))
    }
    assert.equal((await requisitar(semSessao, 'GET', '/vagas')).status, 401)
    assert.equal((await requisitar(semSessao, 'GET', '/matches/empresa/1')).status, 401)
    const cookieAnterior = (await cdp('Storage.getCookies', { browserContextId: autor.contexto })).cookies.find(c => c.name === 'JSESSIONID').value
    await autenticar(autor, 'recrutador', 'autor@t18.invalid')
    const cookieNovo = (await cdp('Storage.getCookies', { browserContextId: autor.contexto })).cookies.find(c => c.name === 'JSESSIONID').value
    assert(cookieAnterior !== cookieNovo, 'Novo login renova o cookie de sessão')
    console.log('CA-03/19/23/24/25: três perfis autenticados; CPF entre perfis; seletor real com catálogo RH/Administração.')

    await criarVaga(empresa, 'Direta T18', 'empresa')
    await criarVaga(autor, 'Autoral T18', 'recrutador')
    await criarVaga(colega, 'Colega T18', 'recrutador')
    await criarVaga(outroAutor, 'Outra empresa T18', 'recrutador')
    const vagasEmpresa = await consultar(empresa, '/vagas/empresa/1')
    assert.deepEqual(vagasEmpresa.map(v => v.idRecrutador), [null, 1, 2])
    assert.deepEqual((await consultar(autor, '/vagas/recrutador/1')).map(v => v.id), [2])
    assert.deepEqual((await consultar(colega, '/vagas/recrutador/2')).map(v => v.id), [3])
    assert.deepEqual((await consultar(outroAutor, '/vagas/recrutador/3')).map(v => v.id), [4])
    console.log('CA-05/06/25: vagas diretas/autorais nos três formulários e listas por titular.')

    await abrir(candidato, 'candidato')
    await esperar(candidato, 'document.querySelectorAll("#job-grid .btn-like").length === 4', 'descoberta anônima')
    const descoberta = await consultar(candidato, '/vagas')
    for (const vaga of descoberta) assert.deepEqual(Object.keys(vaga).sort(), anonimo)
    assert.equal(await avaliar(candidato, 'document.getElementById("job-grid").textContent.includes("Empresa T18")'), false)
    for (let numero = 0; numero < 4; numero++) {
        await clicar(candidato, `#job-grid .job-card:nth-child(${numero + 1}) .btn-like`)
        await esperar(candidato, `document.querySelectorAll('#liked-job-grid .job-card').length === ${numero + 1}`, 'curtida confirmada')
    }
    assert.deepEqual(await consultar(candidato, '/matches/candidato/1'), [])
    assert.equal((await requisitar(autor, 'PUT', '/recrutadores/1', {})).status, 405)
    assert.equal((await requisitar(autor, 'PUT', '/recrutadores/1/senha', {})).status, 405)
    assert.equal((await requisitar(candidato, 'POST', '/curtidas/vagas', { idVaga: 2 })).status, 204)
    const interessados = await consultar(empresa, '/curtidas/candidatos/vaga/2')
    assert.deepEqual(interessados, await consultar(autor, '/curtidas/candidatos/vaga/2'))
    assert.deepEqual(Object.keys(interessados[0]).sort(), anonimo)
    console.log('CA-07/08/12/15: quatro curtidas, repetição idempotente, zero match inicial e chaves anônimas exatas.')

    for (const [aba, rota] of [[colega, '/curtidas/candidatos/vaga/2'], [outroAutor, '/curtidas/candidatos/vaga/2'],
        [outraEmpresa, '/curtidas/candidatos/vaga/2'], [autor, '/vagas/recrutador/2'], [autor, '/matches/recrutador/2'],
        [candidato, '/empresas/1'], [autor, '/candidatos/1'], [candidato, '/matches/candidato/2']]) {
        assert.equal((await requisitar(aba, 'GET', rota)).status, 403, 'Escopo: ' + rota)
    }
    assert.equal((await requisitar(autor, 'POST', '/recrutadores/1/vagas', { nome: 'Adulterada', descricao: 'X', local: 'X', competencias: ['RH'], idEmpresa: 2 })).status, 400)
    assert.equal((await requisitar(outraEmpresa, 'POST', '/curtidas/candidatos', { idCandidato: 1, idVaga: 2 })).status, 403)
    assert.equal((await requisitar(empresa, 'POST', '/curtidas/candidatos', { idCandidato: 999, idVaga: 2 })).status, 409)
    assert.deepEqual(await consultar(candidato, '/matches/candidato/1'), [])

    await abrir(empresa, 'empresa')
    await esperar(empresa, 'document.querySelectorAll("#candidate-grid .candidate-card").length === 3', 'interessados empresariais')
    await clicar(empresa, '#candidate-grid .candidate-card:nth-child(2) .btn-like')
    await esperar(empresa, 'document.querySelectorAll("#liked-candidate-grid .candidate-card").length === 1', 'match empresarial')
    assert.equal((await consultar(candidato, '/matches/candidato/1')).length, 1)
    assert.deepEqual(await consultar(colega, '/matches/recrutador/2'), [])
    assert.deepEqual(Object.keys((await consultar(colega, '/curtidas/candidatos/vaga/3'))[0]).sort(), anonimo)
    await abrir(candidato, 'candidato')
    await esperar(candidato, 'document.getElementById("match-grid").textContent.includes("Autor T18")', 'autor identificado somente no match')
    assert.equal(await avaliar(candidato, 'document.getElementById("job-grid").textContent.includes("Autor T18")'), false)
    console.log('CA-09/10/11/13/17: retribuição empresarial só na vaga A; B e seu autor permanecem restritos.')

    await abrir(colega, 'recrutador')
    await esperar(colega, 'document.querySelectorAll("#candidate-grid .candidate-card").length === 1', 'interessado do colega')
    await clicar(colega, '#candidate-grid .btn-like')
    await esperar(colega, 'document.querySelectorAll("#liked-candidate-grid .candidate-card").length === 1', 'match pelo autor')
    await clicar(empresa, '#candidate-grid .candidate-card:nth-child(1) .btn-like')
    await esperar(empresa, 'document.querySelectorAll("#liked-candidate-grid .candidate-card").length === 3', 'matches totais da empresa')
    assert.equal((await requisitar(autor, 'POST', '/curtidas/candidatos', { idCandidato: 1, idVaga: 2 })).status, 204)
    const matches = await consultar(candidato, '/matches/candidato/1')
    assert.equal(matches.length, 3)
    assert.deepEqual(matches, await consultar(empresa, '/matches/empresa/1'))
    for (const match of matches) assert.deepEqual(Object.keys(match).sort(), camposMatch)
    const direto = matches.find(m => m.nome === 'Direta T18')
    assert.equal(direto.nomeRecrutador, null); assert.equal(direto.areaRecrutamento, null)
    assert.equal(matches.find(m => m.nome === 'Autoral T18').nomeRecrutador, 'Autor T18')
    assert.equal(matches.find(m => m.nome === 'Colega T18').areaRecrutamento, 'Administração')
    assert.equal((await consultar(autor, '/matches/recrutador/1')).length, 1)
    assert.equal((await consultar(colega, '/matches/recrutador/2')).length, 1)
    assert.deepEqual(await consultar(outroAutor, '/matches/recrutador/3'), [])
    for (const vaga of await consultar(candidato, '/vagas')) assert.deepEqual(Object.keys(vaga).sort(), anonimo)
    for (const vaga of await consultar(candidato, '/curtidas/vagas/candidato/1')) assert.deepEqual(Object.keys(vaga).sort(), anonimo)
    console.log('CA-09/12/14/15/16: três matches únicos, contrato completo, autor histórico e descoberta preservada.')

    for (const [aba, perfil, seletor, quantidade] of [
        [empresa, 'empresa', '#candidate-grid', 3],
        [autor, 'recrutador', '#candidate-grid', 1],
        [colega, 'recrutador', '#candidate-grid', 1],
        [candidato, 'candidato', '#job-grid', 4]
    ]) {
        await abrir(aba, perfil)
        await esperar(aba, `document.querySelectorAll('${seletor} .btn-like.curtido:disabled').length === ${quantidade}`, 'curtidas preservadas após recarga')
        const botoes = await avaliar(aba, `Array.from(document.querySelectorAll('${seletor} .btn-like'), b => ({
            icone: b.textContent, bloqueado: b.disabled, fundo: getComputedStyle(b).backgroundColor
        }))`)
        const vermelho = await avaliar(aba, `(() => { const e = document.createElement('span'); e.style.color = getComputedStyle(document.documentElement).getPropertyValue('--red'); document.body.appendChild(e); const c = getComputedStyle(e).color; e.remove(); return c })()`)
        for (const botao of botoes) {
            assert.equal(botao.icone, '♥')
            assert.equal(botao.bloqueado, true)
            assert.equal(botao.fundo, vermelho)
        }
    }
    console.log('T18 regressão: candidato, empresa e autores restauram cor vermelha, coração preenchido e bloqueio após recarga.')

    await abrir(candidato, 'candidato')
    await esperar(candidato, 'document.querySelectorAll("#match-grid .candidate-card").length === 3', 'matches do candidato')
    for (const [aba, seletor, esperados] of [[candidato, '#match-grid', matches], [empresa, '#liked-candidate-grid', matches],
        [colega, '#liked-candidate-grid', matches.filter(m => m.nome === 'Colega T18')]]) {
        const cards = await avaliar(aba, `Array.from(document.querySelectorAll('${seletor} .candidate-card'), card => card.textContent)`)
        assert.equal(cards.length, esperados.length)
        for (let numero = 0; numero < esperados.length; numero++) {
            for (const campo of camposMatch) {
                const valor = esperados[numero][campo]
                if (valor == null) continue
                const textos = Array.isArray(valor) ? valor.map(item => typeof item === 'string' ? item : item.nome) : [String(valor)]
                for (const texto of textos) assert(cards[numero].includes(texto), 'Campo permitido apresentado no match: ' + campo)
            }
        }
    }
    await avaliar(colega, 'localStorage.setItem("sessao", JSON.stringify({ id: 1, tipo: "recrutador" }))')
    await cdp('Page.navigate', { url: origem + '/html/recrutador.html' }, colega.sessao)
    await esperar(colega, 'location.pathname.endsWith("/index.html")', 'rejeição de ID local adulterado')
    assert.equal(await avaliar(colega, 'localStorage.getItem("sessao")'), null)
    for (const aba of sessoes.values()) assert.equal(aba.erros, 0, 'Sem exceções JavaScript não tratadas')
    console.log('CA-06/14/16/17: apresentação real dos matches e sessão adulterada rejeitada pelo backend.')
    console.log('ACEITE_NAVEGADOR_T18_OK')
}

main().catch(erro => { console.error(erro.message); process.exitCode = 1 }).finally(async () => {
    if (socket?.readyState === WebSocket.OPEN) {
        await cdp('Browser.close').catch(() => {})
        socket.close()
    }
    if (navegador && navegador.exitCode === null) {
        await Promise.race([once(navegador, 'exit'), pausa(3000)])
        if (navegador.exitCode === null) navegador.kill()
    }
    servidor.close()
    for (const chamada of pendentes.values()) clearTimeout(chamada.timer)
})
