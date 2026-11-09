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
const atores = ["Candidato", "Empresa", "Recrutador"]
function login(responder) {
    preparar(["email", "senha", ...atores.map(ator => "login-" + ator.toLowerCase()), "cadastrar-candidato", "cadastrar-empresa"], responder)
    elementos.email.value = "conta@example.invalid"
    elementos.senha.value = " senha com espaços "
    carregar("pages/LoginPage").exibirLogin()
}

test("entrada dos três perfis confirma sessão mínima, limpa senha e só navega para páginas existentes", async () => {
    for (const ator of atores) {
        const tipo = ator.toLowerCase()
        login(() => json({ id: 17, tipo, senha: "descartar", extra: "descartar" }))
        await elementos["login-" + tipo].click()
        assert.deepEqual(chamadas, [{ caminho: "/login/" + tipo, metodo: "POST", corpo: { email: "conta@example.invalid", senha: " senha com espaços " } }])
        assert.deepEqual([...armazenamento], [["sessao", JSON.stringify({ id: 17, tipo })]])
        assert.equal(elementos.senha.value, "")
        assert.equal(window.location.href, tipo === "recrutador" ? "" : "./" + tipo + ".html")
        if (tipo === "recrutador") assert.match(document.getElementById("login-status").textContent, /Login realizado/)
    }
    const html = fs.readFileSync(require("node:path").join(__dirname, "../src/html/index.html"), "utf8")
    assert.match(html, /id="login-recrutador"/)
    assert.ok(!html.includes('id="cadastrar-recrutador"'))
})

test("credenciais inválidas/inativas, falhas de rede/JSON e identidade divergente não conservam sessão antiga", async () => {
    for (const ator of atores) {
        for (const responder of [
            () => json({ erro: "Credenciais inválidas." }, 401),
            () => { throw new TypeError("Falha de rede") },
            () => new Response("{"),
            () => json({ id: 1, tipo: "perfil-inválido" })
        ]) {
            login(responder)
            armazenamento.set("sessao", JSON.stringify({ id: 99, tipo: "empresa" }))
            await elementos["login-" + ator.toLowerCase()].click()
            assert.equal(armazenamento.size, 0)
            assert.equal(window.location.href, "")
            assert.ok(document.getElementById("erro-login").textContent.length > 0)
            assert.ok(!document.getElementById("erro-login").textContent.includes("senha com espaços"))
            assert.equal(elementos.senha.value, "")
        }
    }
})

test("login valida formulário e bloqueia tentativas simultâneas entre perfis; Enter não submete credenciais", async () => {
    let resolver
    login(() => new Promise(resolve => { resolver = resolve }))
    elementos.form.reportValidity = () => false
    await elementos["login-candidato"].click()
    assert.equal(chamadas.length, 0)
    elementos.form.reportValidity = () => true
    const pendente = elementos["login-recrutador"].click()
    await elementos["login-empresa"].click()
    assert.equal(chamadas.length, 1)
    let prevenido = false
    elementos.form.onsubmit({ preventDefault: () => prevenido = true })
    assert.equal(prevenido, true)
    resolver(json({ id: 5, tipo: "recrutador" }))
    await pendente
    assert.equal(elementos["login-empresa"].disabled, false)
})

async function cadastro(ator, responder = () => new Response(null, { status: 201 })) {
    const candidato = ator === "Candidato"
    const form = candidato ? "candidate-registration-form" : "company-registration-form"
    const botao = candidato ? "register-candidate" : "register-company"
    preparar([form, botao, "skills-options", "registration-error", "name", "surname", "email", "password", "cpf", "cnpj", "birth-date", "country", "state", "cep", "description"], c => {
        if (c.caminho === "/competencias") return json(["RH", "Administração"])
        if (c.caminho === "/referencias/paises") return json([{ id: 42, nome: "Brasil" }])
        if (c.caminho === "/referencias/estados") return json([{ id: 51, sigla: "SP" }])
        return responder(c)
    })
    carregar(`pages/${ator.toLowerCase()}/Cadastro${ator}Page`)["cadastrar" + ator]()
    await concluir()
    Object.assign(elementos.name, { value: "Nome" })
    for (const id of ["surname", "cpf", "cnpj", "cep", "description"]) elementos[id].value = "valor"
    elementos.email.value = "conta@example.invalid"
    elementos.password.value = " senha com espaços "
    elementos["birth-date"].value = "2000-01-02"
    elementos.country.value = "Brasil"
    elementos.state.value = "SP"
    return { form: elementos[form], botao: elementos[botao] }
}

test("cadastros preservam contratos/referências e senha exata; sucesso vazio volta ao login sem autenticar", async () => {
    for (const ator of ["Candidato", "Empresa"]) {
        const { form, botao } = await cadastro(ator)
        assert.equal(elementos.country.children[0].value, "Brasil")
        assert.equal(elementos.state.children[0].value, "SP")
        let prevenido = false
        form.onsubmit({ preventDefault() { prevenido = true } })
        await concluir()
        assert.equal(prevenido, true)
        const posts = chamadas.filter(c => c.metodo === "POST")
        assert.equal(posts.length, 1)
        assert.equal(posts[0].caminho, ator === "Candidato" ? "/candidatos" : "/empresas")
        assert.equal(posts[0].corpo.senha, " senha com espaços ")
        assert.equal(posts[0].corpo.pais, "Brasil")
        assert.equal(posts[0].corpo.estado, "SP")
        assert.deepEqual(Object.keys(posts[0].corpo).sort(), (ator === "Candidato"
            ? ["nome", "sobrenome", "email", "senha", "cpf", "dataNascimento", "pais", "idade", "estado", "cep", "descricao", "competencias"]
            : ["nome", "email", "senha", "cnpj", "pais", "estado", "cep", "descricao"]).sort())
        assert.equal(window.location.href, "./index.html")
        assert.equal(armazenamento.size, 0)
        assert.equal(botao.disabled, false)
    }
})

test("cadastros mostram referência inválida, conflitos de identificador ativo e rede; permitem nova tentativa", async () => {
    for (const ator of ["Candidato", "Empresa"]) {
        for (const responder of [() => json({ erro: "Referência inválida." }, 400),
            () => json({ erro: "Identificador já utilizado por conta ativa." }, 409),
            () => { throw new TypeError("Falha de rede") }]) {
            const { botao } = await cadastro(ator, responder)
            await botao.click()
            assert.equal(window.location.href, "")
            assert.equal(armazenamento.size, 0)
            assert.equal(elementos["registration-error"].hidden, false)
            assert.equal(botao.disabled, false)
            assert.ok(!elementos["registration-error"].textContent.includes("senha com espaços"))
        }
    }
})

test("idade calculada não impõe limites 1–120 e permite competências vazias como o backend", async () => {
    for (const nascimento of ["1900-01-02", new Date().getFullYear() + "-01-01"]) {
        const { botao } = await cadastro("Candidato")
        elementos["birth-date"].value = nascimento
        await botao.click()
        const corpo = chamadas.at(-1).corpo
        assert.deepEqual(corpo.competencias, [])
        assert.equal(corpo.idade, new Date().getFullYear() - Number(nascimento.slice(0, 4)) -
            (new Date().getMonth() === 0 && new Date().getDate() < Number(nascimento.slice(8)) ? 1 : 0))
    }
    const { botao } = await cadastro("Candidato")
    for (const data of ["2000-02-30", "2999-01-01"]) {
        elementos["birth-date"].value = data
        await botao.click()
        assert.equal(chamadas.filter(c => c.metodo === "POST").length, 0)
    }
})

test("falha no carregamento de referências mantém cadastro bloqueado", async () => {
    preparar(["company-registration-form", "register-company", "registration-error"], () => json({ erro: "Falha." }, 500))
    carregar("pages/empresa/CadastroEmpresaPage").cadastrarEmpresa()
    await concluir()
    assert.equal(elementos["register-company"].disabled, true)
    assert.match(elementos["registration-error"].textContent, /carregar as opções/)
})

test("cadastro aguarda confirmação e evita submissão duplicada; competências selecionadas continuam por nome", async () => {
    let resolver
    const { botao } = await cadastro("Candidato", () => new Promise(resolve => { resolver = resolve }))
    const consultaOriginal = document.querySelectorAll
    document.querySelectorAll = seletor => seletor.includes(":checked")
        ? [{ value: "RH" }, { value: "Administração" }] : consultaOriginal(seletor)
    const pendente = botao.click()
    await botao.click()
    assert.equal(chamadas.filter(c => c.metodo === "POST").length, 1)
    assert.deepEqual(chamadas.at(-1).corpo.competencias, ["RH", "Administração"])
    assert.equal(window.location.href, "")
    assert.equal(armazenamento.size, 0)
    resolver(new Response(null, { status: 201 }))
    await pendente
    assert.equal(window.location.href, "./index.html")
})

test("sessão ausente/malformada ou perfil divergente volta ao login; refresh mantém somente id/tipo", () => {
    const { obterSessaoDoPerfil } = carregar("pages/login/SessaoNavegacao")
    for (const tipo of ["candidato", "empresa", "recrutador"]) {
        for (const valor of [null, "{", "null", "{}", JSON.stringify({ id: -1, tipo }), JSON.stringify({ id: 1, tipo: "outro" })]) {
            preparar()
            if (valor !== null) armazenamento.set("sessao", valor)
            assert.equal(obterSessaoDoPerfil(tipo), undefined)
            assert.equal(armazenamento.size, 0)
            assert.equal(window.location.href, "./index.html")
        }
        preparar()
        armazenamento.set("sessao", JSON.stringify({ id: 5, tipo }))
        assert.deepEqual(obterSessaoDoPerfil(tipo), { id: 5, tipo })
        assert.deepEqual(obterSessaoDoPerfil(tipo), { id: 5, tipo })
        assert.equal(chamadas.length, 0) // O estado local não autentica no servidor.
    }
})

test("páginas existentes revalidam perfil no refresh: 401/403 limpam navegação e impedem consultas seguintes", async () => {
    for (const ator of ["Candidato", "Empresa"]) {
        for (const status of [401, 403]) {
            preparar([], () => json({ erro: "Sessão ausente ou conta inativa." }, status))
            armazenamento.set("sessao", JSON.stringify({ id: 7, tipo: ator.toLowerCase() }))
            await carregar(`pages/${ator.toLowerCase()}/${ator}Page`)["exibirPage" + ator]()
            assert.deepEqual(chamadas.map(c => c.caminho), [ator === "Candidato" ? "/candidatos/7" : "/empresas/7"])
            assert.equal(armazenamento.size, 0)
            assert.equal(window.location.href, "./index.html")
        }
    }
    preparar()
    const { tratarErroSessao } = carregar("pages/login/SessaoNavegacao")
    const { ApiError } = carregar("api/clientes/ApiClient")
    assert.equal(tratarErroSessao(new ApiError(500, "Falha")), false)
    assert.equal(tratarErroSessao(new TypeError("Rede")), false)
    assert.equal(window.location.href, "")
})
