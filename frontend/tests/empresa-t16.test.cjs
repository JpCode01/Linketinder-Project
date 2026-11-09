// Páginas, serviços e clientes reais; DOM/fetch controlados, sem navegador ou banco.
const { test, after } = require("node:test")
const assert = require("node:assert/strict")
const fs = require("node:fs")
const ts = require("typescript")
const originais = { fetch: global.fetch, document: global.document, window: global.window, localStorage: global.localStorage, alert: global.alert }
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
    value = ""; reportValidity() { return true }
    reset() { for (const id in elementos) if (id.startsWith("recruiter-")) elementos[id].value = "" }
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
    elementos = Object.fromEntries(["job-list", "candidate-grid", "candidate-count", "liked-candidate-grid", "liked-candidate-count", "recruiter-form", "recruiter-area", "cadastrar-recrutador", "recruiter-status", "recruiter-name", "recruiter-cpf", "recruiter-email", "recruiter-birthdate", "recruiter-description", "recruiter-password"].map(id => [id, new Elemento()]))
    armazenamento = new Map([["sessao", JSON.stringify({ id: 7, tipo: "empresa" })]])
    global.localStorage = { getItem: chave => armazenamento.get(chave) ?? null, removeItem: chave => armazenamento.delete(chave) }
    global.window = { location: { href: "" }, alert: () => {} }; global.alert = global.window.alert
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
const recrutadores = config.criarRecrutadorService()
const vagas = config.criarVagaService()
const matches = config.criarMatchService()
const { configurarCadastroRecrutadorEmpresa } = carregar("pages/empresa/EmpresaRecrutador")
const { exibirVagasEmpresa } = carregar("pages/empresa/EmpresaVaga")
const { exibirMatchesEmpresa } = carregar("pages/empresa/EmpresaMatch")
const { exibirCandidatosEmpresa } = carregar("pages/empresa/EmpresaCandidato")
const dados = {
    nome: "Autora", cpf: "cpf-teste", email: "autora@teste.com", dataNascimento: "1990-01-02",
    descricao: "Perfil profissional", senha: " senha preservada ", idAreaRecrutamento: 9
}
function preencher() {
    for (const [id, campo] of Object.entries({ name: "nome", cpf: "cpf", email: "email", birthdate: "dataNascimento", description: "descricao", password: "senha", area: "idAreaRecrutamento" }))
        elementos["recruiter-" + id].value = String(dados[campo])
}
const enviar = () => elementos["recruiter-form"].onsubmit({ preventDefault() {} })
const vaga = (id, autor) => ({ id, nome: `Vaga ${id}`, descricao: "Descrição", local: "Remoto", competencias: [{id: 9, nome: "RH"}], qtdeCandidatosQueCurtiram: 1,
    idRecrutador: autor ? 3 : null, nomeRecrutador: autor ? "Autora histórica" : null, areaRecrutamento: autor ? "Administração" : null })
const match = autor => ({ id: autor ? 102 : 101, nomeCandidato: "Pessoa", sobrenomeCandidato: "Candidata", emailCandidato: "pessoa@teste", cpfCandidato: "cpf-c", descricaoCandidato: "Perfil candidato", idadeCandidato: 36,
    estadoCandidato: "SP", paisCandidato: "Brasil candidato", competenciasCandidato: [{id: 9, nome: "RH candidato"}], nomeEmpresa: "Empresa própria", cnpjEmpresa: "cnpj-e", emailCorporativo: "empresa@teste", descricaoEmpresa: "Descrição empresarial", paisEmpresa: "Brasil empresa", estadoEmpresa: "RJ",
    nome: "Vaga identificada", descricao: "Descrição da vaga", competencias: ["Administração"], nomeRecrutador: autor ? "Autora histórica" : null, areaRecrutamento: autor ? "Administração" : null })

test("CA-01: áreas persistidas, empresa contextual, confirmação HTTP, senha exata e bloqueio de envio simultâneo", async () => {
    let confirmar
    preparar(c => c.metodo === "POST" ? new Promise(resolve => { confirmar = () => resolve(new Response(null, {status: 201})) }) : json([{id: 9, nome: "Administração"}]))
    await configurarCadastroRecrutadorEmpresa(7, recrutadores)
    assert.equal(elementos["recruiter-area"].children[1].value, "9")
    preencher()
    const pendente = enviar()
    await enviar()
    assert.equal(elementos["cadastrar-recrutador"].disabled, true)
    assert.equal(elementos["recruiter-password"].value, dados.senha)
    assert.ok(!texto(elementos["recruiter-status"]).includes("sucesso"))
    assert.deepEqual(chamadas.filter(c => c.metodo === "POST"), [{caminho: "/empresas/7/recrutadores", metodo: "POST", corpo: dados}])
    confirmar(); await pendente
    assert.equal(elementos["recruiter-password"].value, "")
    assert.equal(elementos["recruiter-status"].textContent, "Recrutador cadastrado com sucesso.")
    assert.ok(armazenamento.has("sessao"))
})

test("áreas vazias/erro bloqueiam cadastro; conflitos/validação/rede preservam dados e permitem tentar novamente", async () => {
    for (const resposta of [() => json([]), () => json({erro: "Falha"}, 500)]) {
        preparar(resposta)
        await configurarCadastroRecrutadorEmpresa(7, recrutadores)
        await enviar()
        assert.equal(elementos["cadastrar-recrutador"].disabled, true)
        assert.equal(chamadas.length, 1)
        assert.ok(elementos["recruiter-status"].textContent.length > 0)
    }
    for (const status of [400, 409, 500, 0]) {
        preparar(c => {
            if (c.metodo !== "POST") return json([{id: 9, nome: "RH"}])
            if (!status) throw new Error("Falha de rede")
            return json({erro: "Cadastro recusado"}, status)
        })
        await configurarCadastroRecrutadorEmpresa(7, recrutadores)
        preencher()
        await enviar(); await enviar()
        assert.equal(elementos["recruiter-name"].value, dados.nome)
        assert.equal(elementos["recruiter-area"].value, "9")
        assert.equal(elementos["cadastrar-recrutador"].disabled, false)
        assert.ok(!elementos["recruiter-status"].textContent.includes("sucesso"))
        assert.equal(chamadas.filter(c => c.metodo === "POST").length, 2)
    }
})

test("CA-06/18: vagas diretas e autorais preservadas no refresh, sem inferir atividade do autor", async () => {
    preparar(c => { assert.equal(c.caminho, "/vagas/empresa/7"); return json([vaga(1, false), vaga(2, true)]) })
    for (let i = 0; i < 2; i++) {
        await exibirVagasEmpresa(7, vagas)
        assert.equal(elementos["job-list"].children.length, 2)
        assert.ok(texto(elementos["job-list"].children[0]).includes("Criação direta pela empresa"))
        assert.ok(texto(elementos["job-list"].children[1]).includes("Autora histórica — Administração"))
    }
})

test("CA-13/14/18: matches completos somente pelo contrato empresarial, autor opcional e texto seguro", async () => {
    for (const autor of [false, true]) {
        const resultado = {...match(autor), descricao: "<img src=x onerror=alert(1)>"}
        preparar(c => { assert.equal(c.caminho, "/matches/empresa/7"); return json([resultado]) })
        await exibirMatchesEmpresa(7, matches)
        const conteudo = texto(elementos["liked-candidate-grid"])
        for (const [campo, valor] of Object.entries(resultado)) {
            if (valor == null) continue
            const valores = Array.isArray(valor) ? valor.map(v => typeof v === "string" ? v : v.nome) : [String(valor)]
            for (const esperado of valores) assert.ok(conteudo.includes(esperado), campo)
        }
        assert.equal(conteudo.includes("Recrutador autor:"), autor)
        assert.equal(conteudo.includes("null"), false)
    }
})

test("CA-07/09: interessados continuam anônimos nas duas origens; retribuição aguarda HTTP e atualiza matches", async () => {
    let confirmar, atualizacoes = 0
    preparar(c => {
        if (c.caminho === "/vagas/empresa/7") return json([vaga(1, false), vaga(2, true)])
        if (c.metodo === "POST") return new Promise(resolve => { confirmar = () => resolve(new Response(null, {status: 204})) })
        assert.match(c.caminho, /^\/curtidas\/candidatos\/vaga\/[12]$/)
        return json([{id: 42, descricao: "Perfil anônimo", competencias: [{id: 9, nome: "RH"}]}])
    })
    for (let refresh = 0; refresh < 2; refresh++) {
        await exibirCandidatosEmpresa(7, vagas, config.criarCurtidaVagaService(), config.criarCurtidaCandidatoService(), async () => { atualizacoes++ })
        assert.equal(elementos["candidate-grid"].children.length, 2)
        for (const [indice, card] of elementos["candidate-grid"].children.entries()) {
            assert.ok(texto(card).includes("Perfil anônimo"))
            assert.ok(!/cpf-c|pessoa@teste|Pessoa Candidata/.test(texto(card)))
            const botao = card.querySelector(".btn-like")
            botao.click(); botao.click()
            assert.equal(botao.textContent, "\u2661")
            confirmar(); await concluir()
            assert.equal(botao.textContent, "\u2665")
            assert.deepEqual(chamadas.filter(c => c.metodo === "POST").at(-1).corpo, {idCandidato: 42, idVaga: indice + 1})
        }
    }
    assert.equal(atualizacoes, 4)
})

test("gráfico conta candidato em duas vagas duas vezes, usa apenas interessados do escopo empresarial", async () => {
    preparar(c => {
        if (c.caminho === "/vagas/empresa/7") return json([vaga(1, false), vaga(2, true)])
        assert.match(c.caminho, /^\/curtidas\/candidatos\/vaga\/[12]$/)
        return json([{id: 42, descricao: "Anônimo", competencias: [{id: 9, nome: "RH"}, {id: 10, nome: "Administração"}]}])
    })
    assert.deepEqual([...await config.criarRelatorioCompetenciasService().contarCompetencias(7)], [["RH", 2], ["Administração", 2]])
    assert.equal(chamadas.length, 3)
})

test("formulário inválido não envia; 401/403 no POST descarta sessão empresarial", async () => {
    for (const status of [401, 403]) {
        preparar(c => c.metodo === "POST" ? json({erro: "Sessão rejeitada"}, status) : json([{id: 9, nome: "RH"}]))
        await configurarCadastroRecrutadorEmpresa(7, recrutadores)
        preencher()
        elementos["recruiter-form"].reportValidity = () => false
        await enviar()
        assert.equal(chamadas.length, 1)
        elementos["recruiter-form"].reportValidity = () => true
        await enviar()
        assert.equal(armazenamento.has("sessao"), false)
        assert.equal(window.location.href, "./index.html")
    }
})

test("atualizações simultâneas de matches substituem a lista sem duplicar cards", async () => {
    preparar(() => json([match(true)]))
    await Promise.all([exibirMatchesEmpresa(7, matches), exibirMatchesEmpresa(7, matches)])
    assert.equal(elementos["liked-candidate-grid"].children.length, 1)
})

test("falha de retribuição não confirma card ou atualiza match; permite tentar novamente", async () => {
    let atualizacoes = 0
    preparar(c => {
        if (c.caminho === "/vagas/empresa/7") return json([vaga(1, false)])
        if (c.metodo === "POST") return json({erro: "Falha"}, 500)
        return json([{id: 42, descricao: "Anônimo", competencias: []}])
    })
    await exibirCandidatosEmpresa(7, vagas, config.criarCurtidaVagaService(), config.criarCurtidaCandidatoService(), async () => { atualizacoes++ })
    const botao = elementos["candidate-grid"].children[0].querySelector(".btn-like")
    for (let tentativa = 0; tentativa < 2; tentativa++) {
        botao.click(); await concluir()
        assert.equal(botao.disabled, false)
        assert.equal(botao.textContent, "♡")
    }
    assert.equal(atualizacoes, 0)
    assert.equal(chamadas.filter(c => c.metodo === "POST").length, 2)
})

test("sessão rejeitada no cadastro ou matches volta ao login; vazio/falha de match não produz identificação", async () => {
    for (const status of [401, 403]) {
        preparar(() => json({erro: "Sessão rejeitada"}, status))
        await configurarCadastroRecrutadorEmpresa(7, recrutadores)
        assert.equal(window.location.href, "./index.html")
        assert.equal(armazenamento.has("sessao"), false)
        preparar(() => json({erro: "Sessão rejeitada"}, status))
        await exibirMatchesEmpresa(7, matches)
        assert.equal(window.location.href, "./index.html")
    }
    preparar(() => json([]))
    await exibirMatchesEmpresa(7, matches)
    assert.equal(elementos["liked-candidate-grid"].textContent, "Você ainda não tem matches.")
    preparar(() => json({erro: "Falha"}, 500))
    await exibirMatchesEmpresa(7, matches)
    assert.equal(elementos["liked-candidate-grid"].children.length, 0)
    assert.equal(elementos["liked-candidate-count"].textContent, "Erro ao carregar matches")
})
