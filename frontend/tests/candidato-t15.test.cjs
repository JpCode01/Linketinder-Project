// Páginas, serviços e clientes reais; DOM/fetch controlados, sem navegador ou banco.
const { test, after } = require("node:test")
const assert = require("node:assert/strict")
const fs = require("node:fs")
const ts = require("typescript")
const originais = { fetch: global.fetch, document: global.document, window: global.window, localStorage: global.localStorage }
const extensaoOriginal = require.extensions[".ts"]
require.extensions[".ts"] = (modulo, arquivo) => modulo._compile(ts.transpileModule(fs.readFileSync(arquivo, "utf8"), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 }
}).outputText, arquivo)
after(() => {
    Object.assign(global, originais)
    if (extensaoOriginal) require.extensions[".ts"] = extensaoOriginal
    else delete require.extensions[".ts"]
})
const carregar = arquivo => require("../src/ts/" + arquivo + ".ts")
class Elemento {
    children = []; textContent = ""; disabled = false; className = ""
    classList = { add: nome => { this.className += " " + nome } }
    setAttribute() {}
    appendChild(filho) { filho.parent = this; this.children.push(filho); return filho }
    remove() { this.parent.children = this.parent.children.filter(filho => filho !== this) }
    set innerHTML(valor) { this.children = [] }
    addEventListener(evento, callback) { this["on" + evento] = callback }
    click() { if (!this.disabled) this.onclick?.() }
    querySelector(seletor) {
        for (const filho of this.children) {
            if (filho.className.split(" ").includes(seletor.slice(1))) return filho
            const encontrado = filho.querySelector(seletor)
            if (encontrado) return encontrado
        }
        return null
    }
}
const texto = elemento => [elemento.textContent, ...elemento.children.map(texto)].join(" ")
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status })
const concluir = () => new Promise(resolve => setImmediate(resolve))
const chamadas = []
let elementos, armazenamento
function preparar(responder = () => json([])) {
    chamadas.length = 0
    elementos = Object.fromEntries(["job-grid", "result-count", "liked-job-grid", "liked-result-count", "match-grid", "match-count"].map(id => [id, new Elemento()]))
    armazenamento = new Map([["sessao", JSON.stringify({ id: 7, tipo: "candidato" })]])
    global.localStorage = { getItem: chave => armazenamento.get(chave) ?? null, removeItem: chave => armazenamento.delete(chave) }
    global.window = { location: { href: "" } }
    global.document = { getElementById: id => elementos[id] ?? null, createElement: () => new Elemento() }
    global.fetch = async (url, opcoes) => {
        assert.equal(opcoes.credentials, "include")
        assert.equal(new URL(url).origin, "http://127.0.0.1:8080")
        const chamada = { caminho: new URL(url).pathname, metodo: opcoes.method, corpo: opcoes.body && JSON.parse(opcoes.body) }
        chamadas.push(chamada)
        return responder(chamada)
    }
}
const { ServiceConfig } = carregar("config/ServiceConfig")
const config = new ServiceConfig()
const vagas = config.criarVagaService()
const curtidas = config.criarCurtidaVagaService()
const matches = config.criarMatchService()
const { exibirVagas } = carregar("pages/candidato/CandidatoVaga")
const { exibirVagasCurtidas } = carregar("pages/candidato/CandidatoVagaCurtida")
const { exibirMatchesCandidato } = carregar("pages/candidato/CandidatoMatch")
const anonima = id => ({ id, descricao: `Descrição anônima ${id}`, competencias: [{ id: 9, nome: "RH" }] })
const match = (autor = true) => ({
    id: 101, nomeCandidato: "Pessoa", sobrenomeCandidato: "Candidata", emailCandidato: "pessoa@teste",
    cpfCandidato: "cpf-c", descricaoCandidato: "Perfil candidato", idadeCandidato: 36,
    estadoCandidato: "SP", paisCandidato: "Brasil candidato", competenciasCandidato: [{ id: 9, nome: "RH candidato" }],
    nomeEmpresa: "Empresa privada", cnpjEmpresa: "cnpj-e", emailCorporativo: "empresa@teste",
    descricaoEmpresa: "Descrição empresarial", paisEmpresa: "Brasil empresa", estadoEmpresa: "RJ",
    nome: "Vaga A identificada", descricao: "Descrição da vaga A", competencias: ["Administração"],
    nomeRecrutador: autor ? "Autor histórico" : null, areaRecrutamento: autor ? "Recursos Humanos" : null
})

test("CA-16: contrato completo apresentado por match, com autor opcional e texto seguro", async () => {
    for (const autor of [true, false]) {
        const dados = { ...match(autor), descricaoEmpresa: "<img src=x onerror=alert(1)>" }
        preparar(() => json([dados]))
        await exibirMatchesCandidato(7, matches)
        const card = elementos["match-grid"].children[0]
        const conteudo = texto(card)
        for (const [campo, valor] of Object.entries(dados)) {
            if (valor == null) continue
            const valores = Array.isArray(valor) ? valor.map(v => typeof v === "string" ? v : v.nome) : [String(valor)]
            for (const esperado of valores) assert.ok(conteudo.includes(esperado), campo)
        }
        assert.equal(conteudo.includes("Recrutador:"), autor)
        assert.equal(conteudo.includes("Área de recrutamento:"), autor)
        assert.equal(conteudo.includes("null"), false)
        assert.equal(elementos["match-count"].textContent, "1 match")
        assert.deepEqual(chamadas.map(c => c.caminho), ["/matches/candidato/7"])
    }
})

test("CA-08/10/15/17: A e B continuam anônimas após match somente de A; refresh usa listas HTTP", async () => {
    preparar(c => {
        if (c.caminho === "/vagas" || c.caminho === "/curtidas/vagas/candidato/7") return json([anonima(1), anonima(2)])
        if (c.caminho === "/matches/candidato/7") return json([match()])
        assert.fail("Chamada identificável alternativa: " + c.caminho)
    })
    for (let refresh = 0; refresh < 2; refresh++) {
        await exibirVagas(7, vagas, curtidas)
        await exibirVagasCurtidas(7, curtidas)
        await exibirMatchesCandidato(7, matches)
        for (const id of ["job-grid", "liked-job-grid"]) {
            assert.equal(elementos[id].children.length, 2)
            const conteudo = texto(elementos[id])
            assert.ok(conteudo.includes("Descrição anônima 1"))
            assert.ok(conteudo.includes("Descrição anônima 2"))
            assert.ok(!/Empresa privada|Autor histórico|Vaga A identificada|cnpj-e|empresa@teste/.test(conteudo))
        }
        assert.equal(elementos["job-grid"].children[0].querySelector(".btn-like").disabled, true)
        assert.equal(elementos["match-grid"].children.length, 1)
    }
})

test("curtida aguarda confirmação, impede repetição e atualiza lista anônima sem consultar match", async () => {
    let confirmar, confirmada = false
    preparar(c => {
        if (c.metodo === "POST") return new Promise(resolve => { confirmar = () => { confirmada = true; resolve(new Response(null, { status: 204 })) } })
        if (c.caminho === "/vagas") return json([anonima(1)])
        if (c.caminho === "/curtidas/vagas/candidato/7") return json(confirmada ? [anonima(1)] : [])
        assert.fail(c.caminho)
    })
    await exibirVagas(7, vagas, curtidas)
    await exibirVagasCurtidas(7, curtidas)
    const botao = elementos["job-grid"].children[0].querySelector(".btn-like")
    botao.click(); botao.click()
    assert.equal(botao.disabled, true)
    assert.equal(botao.textContent, "♡")
    assert.equal(elementos["liked-result-count"].textContent, "0 vagas curtidas")
    assert.deepEqual(chamadas.filter(c => c.metodo === "POST"), [{ caminho: "/curtidas/vagas", metodo: "POST", corpo: { idVaga: 1 } }])
    confirmar(); await concluir()
    assert.equal(botao.textContent, "♥")
    assert.equal(elementos["liked-result-count"].textContent, "1 vaga curtida")
    assert.ok(texto(elementos["liked-job-grid"]).includes("Descrição anônima 1"))
})

test("falha de curtida permite tentar novamente sem confirmação local ou duplicar mensagens", async () => {
    preparar(c => c.metodo === "POST" ? json({ erro: "Indisponível" }, 500) : json(c.caminho === "/vagas" ? [anonima(1)] : []))
    await exibirVagas(7, vagas, curtidas)
    const card = elementos["job-grid"].children[0], botao = card.querySelector(".btn-like")
    for (let tentativa = 0; tentativa < 2; tentativa++) {
        botao.click(); await concluir()
        assert.equal(botao.disabled, false)
        assert.equal(botao.textContent, "♡")
        assert.equal(card.children.filter(c => c.className === "like-error").length, 1)
    }
    assert.equal(chamadas.filter(c => c.caminho === "/curtidas/vagas/candidato/7").length, 1)
})

test("vazio, carregamento, erro e recuperação de cada lista não preservam cards antigos", async () => {
    const listas = [
        [() => exibirVagas(7, vagas, curtidas), "job-grid", "result-count", "Nenhuma vaga disponível."],
        [() => exibirVagasCurtidas(7, curtidas), "liked-job-grid", "liked-result-count", "Você ainda não curtiu vagas."],
        [() => exibirMatchesCandidato(7, matches), "match-grid", "match-count", "Você ainda não tem matches."]
    ]
    for (const [exibir, grid, contador, vazio] of listas) {
        let responder
        preparar(() => new Promise(resolve => { responder = resolve }))
        elementos[grid].appendChild(new Elemento())
        const pendente = exibir()
        assert.match(elementos[contador].textContent, /Carregando/)
        assert.equal(elementos[grid].children.length, 0)
        responder(json([], 200)); await concluir()
        // Descoberta consulta também as curtidas depois da listagem.
        if (grid === "job-grid") responder(json([]))
        await pendente
        assert.ok(texto(elementos[grid]).includes(vazio))
        global.fetch = async () => { throw new TypeError("offline") }
        await exibir()
        assert.match(elementos[contador].textContent, /Erro ao carregar/)
        assert.equal(elementos[grid].children.length, 0)
        global.fetch = async () => json([])
        await exibir()
        assert.ok(texto(elementos[grid]).includes(vazio))
    }
})

test("401/403 nas consultas e na curtida descartam navegação sem fallback de ator", async () => {
    for (const status of [401, 403]) {
        for (const exibir of [() => exibirVagas(7, vagas, curtidas), () => exibirVagasCurtidas(7, curtidas), () => exibirMatchesCandidato(7, matches)]) {
            preparar(() => json({ erro: "Acesso negado" }, status))
            await exibir()
            assert.equal(armazenamento.has("sessao"), false)
            assert.equal(window.location.href, "./index.html")
            assert.equal(chamadas.length, 1)
        }
        preparar(c => c.metodo === "POST" ? json({ erro: "Acesso negado" }, status) : json(c.caminho === "/vagas" ? [anonima(1)] : []))
        await exibirVagas(7, vagas, curtidas)
        elementos["job-grid"].children[0].querySelector(".btn-like").click()
        await concluir()
        assert.equal(armazenamento.has("sessao"), false)
        assert.equal(window.location.href, "./index.html")
    }
})
