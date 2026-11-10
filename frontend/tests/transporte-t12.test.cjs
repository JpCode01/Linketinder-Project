const { test, after } = require("node:test")
const assert = require("node:assert/strict")
const fs = require("node:fs")
const path = require("node:path")
const ts = require("typescript")

const originais = { fetch: global.fetch, document: global.document, window: global.window, localStorage: global.localStorage }
const extensaoOriginal = require.extensions[".ts"]
require.extensions[".ts"] = (modulo, arquivo) => {
    const resultado = ts.transpileModule(fs.readFileSync(arquivo, "utf8"), {
        compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
    })
    modulo._compile(resultado.outputText, arquivo)
}
after(() => {
    Object.assign(global, originais)
    if (extensaoOriginal) require.extensions[".ts"] = extensaoOriginal
    else delete require.extensions[".ts"]
})
const carregar = arquivo => require("../src/ts/" + arquivo + ".ts")
const { ApiClient, ApiError } = carregar("api/clientes/ApiClient")
const { ServiceConfig } = carregar("config/ServiceConfig")
const config = new ServiceConfig()
const chamadas = []
function respostaJson(dados, status = 200) {
    return new Response(JSON.stringify(dados), { status, headers: { "Content-Type": "application/json" } })
}
function simular(responder = () => respostaJson([])) {
    chamadas.length = 0
    global.fetch = async (url, opcoes) => {
        assert.ok(url.startsWith("http://127.0.0.1:8080/"))
        assert.equal(opcoes.credentials, "include")
        const chamada = { caminho: new URL(url).pathname, metodo: opcoes.method, corpo: opcoes.body && JSON.parse(opcoes.body) }
        if (opcoes.body) assert.equal(opcoes.headers["Content-Type"], "application/json")
        chamadas.push(chamada)
        return responder(chamada)
    }
}

test("contratos de consulta: referências, perfis próprios, vagas, interessados e matches por ator", async () => {
    simular()
    const consultas = [
        [() => config.criarCandidatoService().buscarPorId(7), "/candidatos/7"],
        [() => new (carregar("api/clientes/EmpresaApiClient").EmpresaApiClient)().buscarPorId(8), "/empresas/8"],
        [() => config.criarRecrutadorService().buscarPerfilProprio(9), "/recrutadores/9"],
        [() => config.criarRecrutadorService().buscarAreas(), "/areas-recrutamento"],
        [() => config.criarReferenciaService().buscarPaises(), "/referencias/paises"],
        [() => config.criarReferenciaService().buscarEstados(), "/referencias/estados"],
        [() => config.criarCompetenciaService().buscarTodas(), "/competencias"],
        [() => config.criarVagaService().buscarTodasAsVagasAnonimas(), "/vagas"],
        [() => config.criarVagaService().buscarPorEmpresa(8), "/vagas/empresa/8"],
        [() => config.criarVagaService().buscarPorRecrutador(9), "/vagas/recrutador/9"],
        [() => config.criarCurtidaVagaService().buscarVagasCurtidas(7), "/curtidas/vagas/candidato/7"],
        [() => config.criarCurtidaVagaService().buscarCandidatosQueCurtiram(10), "/curtidas/candidatos/vaga/10"],
        [() => config.criarMatchService().buscarPorCandidato(7), "/matches/candidato/7"],
        [() => config.criarMatchService().buscarPorEmpresa(8), "/matches/empresa/8"],
        [() => config.criarMatchService().buscarPorRecrutador(9), "/matches/recrutador/9"]
    ]
    for (const [consultar, caminho] of consultas) {
        assert.deepEqual(await consultar(), [])
        assert.deepEqual(chamadas.at(-1), { caminho, metodo: "GET", corpo: undefined })
    }
})

test("cadastros e curtidas aceitam 201/204 vazios; corpos excluem identidade do ator e campos locais", async () => {
    simular(c => new Response(null, { status: c.caminho.startsWith("/curtidas") ? 204 : 201 }))
    const candidato = { nome: "C", sobrenome: "S", email: "c@example.invalid", senha: "teste", cpf: "cpf", dataNascimento: "2000-01-02", pais: "Brasil", idade: 26, estado: "SP", cep: "cep", descricao: "d", competencias: ["RH"] }
    const empresa = { nome: "E", email: "e@example.invalid", senha: "teste", cnpj: "cnpj", pais: "Brasil", estado: "SP", cep: "cep", descricao: "d" }
    const recrutador = { nome: "R", cpf: "cpf", email: "r@example.invalid", dataNascimento: "2000-01-02", descricao: "d", senha: "teste", idAreaRecrutamento: 1 }
    await config.criarCandidatoService().cadastrar(candidato)
    await config.criarEmpresaService().cadastrar(empresa)
    await config.criarRecrutadorService().cadastrarPelaEmpresa(8, recrutador)
    const vaga = { nome: "Analista", descricao: "d", local: "Remoto", competencias: ["RH", "Administração"] }
    await config.criarVagaService().criarVagaEmpresa({ ...vaga, localizacao: "Remoto", tipo: "CLT", formacao: "ignorada" })
    await config.criarVagaService().criarVagaRecrutador(9, vaga)
    await config.criarCurtidaVagaService().curtirVaga(10)
    await config.criarCurtidaCandidatoService().retribuirInteresseEmpresa(7, 10)
    await config.criarCurtidaCandidatoService().retribuirInteresseRecrutador(7, 10)
    assert.deepEqual(chamadas.map(c => [c.caminho, c.corpo]), [
        ["/candidatos", candidato], ["/empresas", empresa], ["/empresas/8/recrutadores", recrutador],
        ["/vagas", vaga], ["/recrutadores/9/vagas", vaga], ["/curtidas/vagas", { idVaga: 10 }],
        ["/curtidas/candidatos", { idCandidato: 7, idVaga: 10 }],
        ["/curtidas/candidatos", { idCandidato: 7, idVaga: 10 }]
    ])
})

test("401/403/409 preservam status e mensagem; erros não JSON não mascaram status", async () => {
    const api = new ApiClient()
    for (const status of [400, 401, 403, 404, 409, 500]) {
        simular(() => respostaJson({ erro: "Operação rejeitada." }, status))
        await assert.rejects(api.obter("/vagas"), e => e instanceof ApiError && e.status === status && e.message === "Operação rejeitada.")
        for (const corpo of ["<html>erro</html>", "{", "", "null", '{"erro":42}']) {
            simular(() => new Response(corpo, { status }))
            await assert.rejects(api.postar("/curtidas/vagas", { idVaga: 1 }), e => e instanceof ApiError && e.status === status && e.message.includes("HTTP " + status))
        }
    }
})

test("falha de rede e JSON de sucesso inválido rejeitam a operação", async () => {
    simular(() => { throw new TypeError("Falha de rede") })
    await assert.rejects(config.criarCurtidaVagaService().curtirVaga(1), TypeError)
    simular(() => new Response("{"))
    await assert.rejects(config.criarVagaService().buscarTodasAsVagasAnonimas(), /JSON inválida/)
    await assert.rejects(config.criarLoginApiClient().loginCandidato("c", "s"), /JSON inválida/)
})

test("login dos três perfis usa resposta minúscula e rejeita identidade inválida", async () => {
    for (const ator of ["Candidato", "Empresa", "Recrutador"]) {
        simular(() => respostaJson({ id: 7, tipo: ator.toLowerCase(), extra: "descartar" }))
        assert.deepEqual(await config.criarLoginApiClient()["login" + ator]("email", "senha"), { id: 7, tipo: ator.toLowerCase() })
        assert.deepEqual(chamadas[0], { caminho: "/login/" + ator.toLowerCase(), metodo: "POST", corpo: { email: "email", senha: "senha" } })
    }
    for (const dados of [null, {}, { id: -1, tipo: "candidato" }, { id: 7, tipo: "CANDIDATO" }, { id: 7, tipo: "empresa" }]) {
        simular(() => respostaJson(dados))
        await assert.rejects(config.criarLoginApiClient().loginCandidato("c", "s"), /login inválida/)
    }
})

test("estratégias persistem somente sessão confirmada e falhas não simulam sucesso", async () => {
    for (const ator of ["Candidato", "Empresa"]) {
        const gravacoes = []
        global.localStorage = { setItem: (...args) => gravacoes.push(args) }
        global.window = { location: { href: "" } }
        const Strategy = carregar("strategy/login/Login" + ator + "Strategy")["Login" + ator + "Strategy"]
        const strategy = new Strategy()
        simular(() => respostaJson({ erro: "Credenciais inválidas." }, 401))
        await assert.rejects(strategy.login("e", "s"), carregar("exceptions/LoginException").LoginException)
        assert.equal(gravacoes.length, 0)
        assert.equal(window.location.href, "")
        simular(() => respostaJson({ id: 7, tipo: ator.toLowerCase() }))
        await strategy.login("e", "s")
        assert.deepEqual(gravacoes, [["sessao", JSON.stringify({ id: 7, tipo: ator.toLowerCase() })]])
        assert.equal(window.location.href, "./" + ator.toLowerCase() + ".html")
    }
})

class Elemento {
    children = []; textContent = ""; value = ""; disabled = false; dataset = {}; eventos = {}
    focus() {}
    setAttribute() {}
    className = ""; classes = new Set()
    classList = { add: nome => this.classes.add(nome) }
    appendChild(filho) { this.children.push(filho); return filho }
    set innerHTML(valor) { this.children = [] }
    get childElementCount() { return this.children.length }
    addEventListener(evento, acao) { this.eventos[evento] = acao }
    querySelector(seletor) {
        return this.children.find(c => c.className === seletor.slice(1) || c.classes.has(seletor.slice(1))) ??
            this.children.map(c => c.querySelector(seletor)).find(Boolean) ?? null
    }
}
function dom(ids) {
    const elementos = Object.fromEntries(ids.map(id => [id, new Elemento()]))
    global.document = {
        getElementById: id => elementos[id] ?? null,
        createElement: () => new Elemento(),
        createTextNode: texto => Object.assign(new Elemento(), { textContent: texto }),
        querySelectorAll: () => []
    }
    return elementos
}
const concluir = () => new Promise(resolve => setImmediate(resolve))

test("descoberta lê lista anônima de curtidas uma vez e atualiza o card somente após sucesso", async () => {
    const elementos = dom(["job-grid", "result-count"])
    const vagas = [1, 2].map(id => ({ id, descricao: "Descrição", competencias: [{ id: 4, nome: "RH" }] }))
    let confirmar
    simular(c => {
        if (c.caminho === "/vagas") return respostaJson(vagas)
        if (c.caminho === "/curtidas/vagas/candidato/7") return respostaJson([vagas[0]])
        if (c.caminho === "/curtidas/vagas") return new Promise(resolve => { confirmar = () => resolve(new Response(null, { status: 204 })) })
        assert.fail("Consulta inesperada: " + c.caminho)
    })
    await carregar("pages/candidato/CandidatoVaga").exibirVagas(7, config.criarVagaService(), config.criarCurtidaVagaService())
    const [curtido, novo] = elementos["job-grid"].children.map(c => c.querySelector(".btn-like"))
    assert.equal(curtido.disabled, true)
    assert.equal(novo.disabled, false)
    novo.eventos.click()
    await concluir()
    assert.equal(novo.textContent, "♡")
    confirmar()
    await concluir()
    assert.equal(novo.textContent, "♥")
    assert.deepEqual(chamadas.map(c => c.caminho), ["/vagas", "/curtidas/vagas/candidato/7", "/curtidas/vagas"])
})

test("interessados continuam anônimos e retribuição não se propaga entre vagas", async () => {
    const elementos = dom(["candidate-grid", "candidate-count"])
    simular(c => {
        if (c.caminho === "/vagas/empresa/8") return respostaJson([10, 11].map(id => ({ id, nome: "Analista" })))
        if (c.caminho.startsWith("/curtidas/candidatos/vaga/")) return respostaJson([{ id: 7, descricao: "Descrição anônima", competencias: [] }])
        if (c.caminho === "/curtidas/candidatos") return new Response(null, { status: 204 })
        assert.fail("Consulta inesperada: " + c.caminho)
    })
    await carregar("pages/empresa/EmpresaCandidato").exibirCandidatosEmpresa(8, config.criarVagaService(), config.criarCurtidaVagaService(), config.criarCurtidaCandidatoService())
    const botoes = elementos["candidate-grid"].children.map(c => c.querySelector(".btn-like"))
    await botoes[0].eventos.click()
    assert.equal(botoes[0].disabled, true)
    assert.equal(botoes[1].disabled, false)
    await botoes[1].eventos.click()
    assert.deepEqual(chamadas.filter(c => c.metodo === "POST").map(c => c.corpo), [{ idCandidato: 7, idVaga: 10 }, { idCandidato: 7, idVaga: 11 }])
})

test("catálogo RH/administração preenche ambos os formulários sem IDs fabricados e envia nomes", async () => {
    const nomes = ["RH", "Administração"]
    const elementos = dom(["skills-options", "criar-vaga", "job-title", "job-type", "job-location", "job-description"])
    simular(c => c.caminho === "/competencias" ? respostaJson(nomes) : new Response(null, { status: 201 }))
    const pagina = carregar("pages/empresa/EmpresaVaga")
    await pagina.carregarOpcoesCompetencias()
    const sugestoes = elementos["skills-options"].children[2]
    assert.deepEqual(sugestoes.children.map(botao => botao.textContent), nomes)
    for (const nome of nomes) sugestoes.children.find(botao => botao.textContent === nome).onclick()
    elementos["job-title"].value = "Analista"
    elementos["job-location"].value = "Remoto"
    elementos["job-description"].value = "Descrição"
    global.window = { alert: () => {} }
    pagina.configurarCriacaoVaga(config.criarVagaService())
    await elementos["criar-vaga"].onclick()
    assert.deepEqual(chamadas.at(-1).corpo, { nome: "Analista", descricao: "Descrição", local: "Remoto", competencias: nomes })

    const candidato = dom(["skills-options", "country", "state", "candidate-registration-form", "register-candidate", "birth-date"])
    simular(c => respostaJson(c.caminho === "/competencias" ? nomes : []))
    carregar("pages/candidato/CadastroCandidatoPage").cadastrarCandidato()
    await concluir()
    const opcoes = candidato["skills-options"].children[2].children
    assert.deepEqual(opcoes.map(botao => botao.textContent), nomes)
    assert.ok(opcoes.every(botao => botao.type === "button"))
})

test("gráfico preserva contagem por interesse; nenhum fetch ativo contorna transporte", async () => {
    simular(c => {
        if (c.caminho === "/vagas/empresa/8") return respostaJson([{ id: 10 }, { id: 11 }])
        if (c.caminho.startsWith("/curtidas/candidatos/vaga/")) return respostaJson([{ id: 7, descricao: "d", competencias: [{ id: 1, nome: "RH" }] }])
        assert.fail("Consulta inesperada")
    })
    assert.deepEqual(await config.criarRelatorioCompetenciasService().contarCompetencias(8), new Map([["RH", 2]]))
    const raiz = path.join(__dirname, "../src/ts")
    for (const arquivo of fs.readdirSync(raiz, { recursive: true }).filter(p => p.endsWith(".ts"))) {
        const codigo = fs.readFileSync(path.join(raiz, arquivo), "utf8")
        assert.ok(!codigo.includes("/verificar"), arquivo)
        if (path.basename(arquivo) !== "ApiClient.ts") {
            assert.ok(!/\bfetch\s*\(/.test(codigo), arquivo)
        }
    }
})
