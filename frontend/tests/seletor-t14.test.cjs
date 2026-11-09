// Executa páginas/estratégias reais com DOM e fetch controlados; sem navegador ou banco.
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
const chamadas = []
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status })
const concluir = () => new Promise(resolve => setImmediate(resolve))
let elementos, armazenamento
class Elemento {
    children = []; value = ""; textContent = ""; disabled = false; hidden = true
    focus() {}
    classList = { add() {} }
    reportValidity() { return true }
    setAttribute() {}
    appendChild(filho) { filho.parent = this; this.children.push(filho); return filho }
    remove() { this.parent.children = this.parent.children.filter(filho => filho !== this) }
    set innerHTML(valor) { this.children = [] }
    get childElementCount() { return this.children.length }
    click() { if (!this.disabled) return this.onclick?.() }
}
function preparar(ids = [], responder = () => json([])) {
    chamadas.length = 0
    elementos = Object.fromEntries(["form", ...ids].map(id => [id, new Elemento()]))
    armazenamento = new Map()
    global.localStorage = {
        getItem: chave => armazenamento.get(chave) ?? null,
        setItem: (chave, valor) => armazenamento.set(chave, valor),
        removeItem: chave => armazenamento.delete(chave)
    }
    global.window = { location: { href: "", pathname: "/html/index.html" } }
    global.document = {
        getElementById: id => elementos[id] ?? elementos.form.children.find(filho => filho.id === id) ?? null,
        createElement: () => new Elemento(),
        querySelector: seletor => seletor === "form" ? elementos.form : null,
        querySelectorAll: seletor => seletor === '[id^="login-"]'
            ? Object.entries(elementos).filter(([id]) => id.startsWith("login-")).map(([, item]) => item)
            : []
    }
    global.fetch = async (url, opcoes) => {
        assert.ok(url.startsWith("http://127.0.0.1:8080/"))
        assert.equal(opcoes.credentials, "include")
        const chamada = { caminho: new URL(url).pathname, metodo: opcoes.method, corpo: opcoes.body && JSON.parse(opcoes.body) }
        chamadas.push(chamada)
        return responder(chamada)
    }
}

const { SeletorCompetencias } = carregar("components/SeletorCompetencias")
function seletor(nomes = ["Recursos Humanos", "Administração", "Java"]) {
    preparar(["skills-options"])
    const componente = new SeletorCompetencias(elementos["skills-options"], nomes)
    const [label, status, sugestoes, selecionadas] = elementos["skills-options"].children
    const pesquisa = label.children[0]
    return { componente, pesquisa, status, sugestoes, selecionadas }
}
function pesquisar(campo, termo) { campo.value = termo; campo.oninput() }

test("CA-23/24: pesquisa parcial sem distinguir maiúsculas, seleção múltipla por nomes e remoção", () => {
    const { componente, pesquisa, sugestoes, selecionadas } = seletor()
    pesquisar(pesquisa, "HUMAN")
    assert.deepEqual(sugestoes.children.map(b => b.textContent), ["Recursos Humanos"])
    sugestoes.children[0].click()
    pesquisar(pesquisa, "minis")
    sugestoes.children[0].click()
    assert.deepEqual(componente.obterSelecionadas(), ["Recursos Humanos", "Administração"])
    assert.equal(pesquisa.value, "")
    selecionadas.children[0].click()
    assert.deepEqual(componente.obterSelecionadas(), ["Administração"])
    pesquisar(pesquisa, "human")
    sugestoes.children[0].click()
    assert.deepEqual(componente.obterSelecionadas(), ["Administração", "Recursos Humanos"])
})

test("CA-23: catálogo duplicado e repetição de clique não duplicam seleção; texto livre e Enter não cadastram", () => {
    const { componente, pesquisa, sugestoes } = seletor(["RH", "RH", "Administração"])
    assert.equal(sugestoes.children.length, 2)
    const rh = sugestoes.children[0]
    rh.click(); rh.click()
    assert.deepEqual(componente.obterSelecionadas(), ["RH"])
    pesquisar(pesquisa, "competência inexistente")
    assert.equal(sugestoes.children.length, 0)
    let prevenido = false
    pesquisa.onkeydown({ key: "Enter", preventDefault() { prevenido = true } })
    assert.equal(prevenido, true)
    assert.deepEqual(componente.obterSelecionadas(), ["RH"])
    const copia = componente.obterSelecionadas()
    copia.push("livre")
    assert.deepEqual(componente.obterSelecionadas(), ["RH"])
})

test("lista vazia, filtro sem resultados, limpeza e instâncias independentes", () => {
    const vazio = seletor([])
    assert.deepEqual(vazio.componente.obterSelecionadas(), [])
    assert.match(vazio.status.textContent, /Nenhuma competência disponível/)
    const primeiro = seletor()
    primeiro.sugestoes.children[0].click()
    const segundo = seletor()
    assert.deepEqual(segundo.componente.obterSelecionadas(), [])
    pesquisar(primeiro.pesquisa, "zzzz")
    assert.match(primeiro.status.textContent, /Nenhuma competência encontrada/)
    primeiro.componente.limpar()
    assert.deepEqual(primeiro.componente.obterSelecionadas(), [])
    assert.equal(primeiro.pesquisa.value, "")
    assert.equal(primeiro.selecionadas.children.length, 0)
    assert.equal(primeiro.sugestoes.children.length, 3)
})

test("nomes do catálogo são texto; botões de seleção/remoção não submetem formulários", () => {
    const nome = '<img src=x onerror="alert(1)">'
    const { sugestoes, selecionadas, componente } = seletor([nome])
    assert.equal(sugestoes.children[0].textContent, nome)
    assert.equal(sugestoes.children[0].type, "button")
    sugestoes.children[0].click()
    assert.equal(selecionadas.children[0].type, "button")
    assert.deepEqual(componente.obterSelecionadas(), [nome])
})

async function formularioEmpresa(responder) {
    preparar(["skills-options", "criar-vaga", "job-title", "job-type", "job-location", "job-description"], responder)
    window.alert = () => {}
    const pagina = carregar("pages/empresa/EmpresaVaga")
    await pagina.carregarOpcoesCompetencias()
    pagina.configurarCriacaoVaga(8, new (carregar("config/ServiceConfig").ServiceConfig)().criarVagaService())
    return pagina
}

test("formulário empresarial envia apenas seleção; falha conserva seleção e sucesso limpa inclusive pesquisa", async () => {
    let status = 400
    await formularioEmpresa(c => c.caminho === "/competencias" ? json(["RH", "Administração"]) : json({ erro: "Falha" }, status))
    const [label, , sugestoes, selecionadas] = elementos["skills-options"].children
    const pesquisa = label.children[0]
    sugestoes.children[1].click()
    pesquisar(pesquisa, "texto livre")
    await elementos["criar-vaga"].click()
    assert.deepEqual(chamadas.at(-1).corpo.competencias, ["Administração"])
    assert.equal(selecionadas.children.length, 1)
    assert.equal(pesquisa.value, "texto livre")
    status = 201
    await elementos["criar-vaga"].click()
    assert.deepEqual(chamadas.at(-1).corpo.competencias, ["Administração"])
    assert.equal(selecionadas.children.length, 0)
    assert.equal(pesquisa.value, "")
    await elementos["criar-vaga"].click()
    assert.deepEqual(chamadas.at(-1).corpo.competencias, [])
})

test("falha HTTP/rede no catálogo bloqueia vaga e mostra erro; catálogo vazio permite lista vazia", async () => {
    for (const responder of [() => json({ erro: "Falha" }, 500), () => { throw new TypeError("Rede") }]) {
        await formularioEmpresa(responder)
        assert.equal(elementos["criar-vaga"].disabled, true)
        assert.match(elementos["skills-options"].textContent, /Não foi possível carregar/)
        await elementos["criar-vaga"].click()
        assert.equal(chamadas.filter(c => c.metodo === "POST").length, 0)
    }
    await formularioEmpresa(c => c.caminho === "/competencias" ? json([]) : new Response(null, { status: 201 }))
    assert.equal(elementos["criar-vaga"].disabled, false)
    await elementos["criar-vaga"].click()
    assert.deepEqual(chamadas.at(-1).corpo.competencias, [])
})

async function formularioCandidato(responder) {
    preparar(["skills-options", "candidate-registration-form", "register-candidate", "registration-error",
        "name", "surname", "email", "password", "cpf", "birth-date", "country", "state", "cep", "description"], responder)
    carregar("pages/candidato/CadastroCandidatoPage").cadastrarCandidato()
    await concluir()
    elementos["birth-date"].value = "2000-01-02"
}

test("cadastro candidato envia seleção após remoção e ignora pesquisa livre inclusive após falha HTTP", async () => {
    await formularioCandidato(c => c.caminho === "/competencias" ? json(["RH", "Administração"])
        : c.metodo === "POST" ? json({ erro: "Conflito" }, 409) : json([]))
    const [label, , sugestoes, selecionadas] = elementos["skills-options"].children
    sugestoes.children[0].click()
    sugestoes.children[0].click()
    selecionadas.children[0].click()
    pesquisar(label.children[0], "Java não selecionado")
    await elementos["register-candidate"].click()
    assert.deepEqual(chamadas.at(-1).corpo.competencias, ["Administração"])
    assert.equal(elementos["registration-error"].hidden, false)
    assert.equal(selecionadas.children.length, 1)
    selecionadas.children[0].click()
    await elementos["register-candidate"].click()
    assert.deepEqual(chamadas.at(-1).corpo.competencias, [])
})

test("falha HTTP/rede no catálogo do candidato bloqueia cadastro sem POST", async () => {
    for (const responder of [() => json({ erro: "Falha" }, 500), () => { throw new TypeError("Rede") }]) {
        await formularioCandidato(responder)
        assert.equal(elementos["register-candidate"].disabled, true)
        assert.match(elementos["registration-error"].textContent, /carregar as opções/)
        await elementos["register-candidate"].click()
        assert.equal(chamadas.filter(c => c.metodo === "POST").length, 0)
    }
})
