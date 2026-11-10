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
class Elemento {
    children = []; value = ""; disabled = false; className = ""; conteudo = ""
    classList = { add: nome => { this.className += " " + nome } }
    set textContent(valor) { this.children = []; this.conteudo = valor }
    get textContent() { return this.conteudo }
    set innerHTML(valor) { this.children = []; this.conteudo = "" }
    setAttribute() {}
    focus() {}
    reportValidity() { return true }
    reset() { for (const id of ["job-title", "job-location", "job-description"]) elementos[id].value = "" }
    appendChild(filho) { this.children.push(filho); return filho }
    addEventListener(evento, callback) { this["on" + evento] = callback }
    click() { if (!this.disabled) return this.onclick?.() }
    querySelector(seletor) {
        for (const filho of this.children) {
            if (filho.className.split(" ").includes(seletor.slice(1))) return filho
            const encontrado = filho.querySelector(seletor)
            if (encontrado) return encontrado
        }
        return null
    }
}
const carregar = arquivo => require("../src/ts/" + arquivo + ".ts")
const { exibirPageRecrutador } = carregar("pages/recrutador/RecrutadorPage")
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status })
const chamadas = []
const texto = elemento => [elemento.textContent, ...elemento.children.map(texto)].join(" ")
let elementos, armazenamento, avisos
const vaga = id => ({ id, nome: `Vaga ${id}`, descricao: "Descrição interna", local: "Remoto", competencias: [{ id: 9, nome: "RH" }],
    qtdeCandidatosQueCurtiram: 1, idRecrutador: 7, nomeRecrutador: "Autora", areaRecrutamento: "Administração" })
const match = { id: 1, nomeCandidato: "Pessoa", sobrenomeCandidato: "Candidata", emailCandidato: "pessoa@teste", cpfCandidato: "cpf-c", descricaoCandidato: "Perfil identificado", idadeCandidato: 30,
    paisCandidato: "Brasil", estadoCandidato: "SP", competenciasCandidato: [{ id: 9, nome: "RH" }], nomeEmpresa: "Empresa própria", cnpjEmpresa: "cnpj-e", emailCorporativo: "empresa@teste", descricaoEmpresa: "Descrição empresarial", paisEmpresa: "Brasil", estadoEmpresa: "RJ",
    nome: "Vaga do match", descricao: "Descrição da vaga", competencias: ["Administração"], nomeRecrutador: "Autora", areaRecrutamento: "RH" }
function preparar(responder, id = 7) {
    chamadas.length = 0; avisos = []
    elementos = Object.fromEntries(["profile-name", "profile-description", "profile-area", "profile-email", "page-status", "job-form", "job-status", "criar-vaga", "skills-options", "job-title", "job-location", "job-description", "job-list", "candidate-grid", "candidate-count", "liked-candidate-grid", "liked-candidate-count"].map(id => [id, new Elemento()]))
    elementos["criar-vaga"].disabled = true
    armazenamento = new Map([["sessao", JSON.stringify({ id, tipo: "recrutador" })]])
    global.localStorage = { getItem: chave => armazenamento.get(chave) ?? null, removeItem: chave => armazenamento.delete(chave) }
    global.window = { location: { href: "", pathname: "/html/recrutador.html" }, alert: mensagem => avisos.push(mensagem) }
    global.document = { getElementById: id => elementos[id] ?? null, createElement: () => new Elemento() }
    global.fetch = async (url, opcoes) => {
        assert.equal(new URL(url).origin, "http://127.0.0.1:8080")
        assert.equal(opcoes.credentials, "include")
        const chamada = { caminho: new URL(url).pathname, metodo: opcoes.method, corpo: opcoes.body && JSON.parse(opcoes.body) }
        chamadas.push(chamada)
        const resposta = responder?.(chamada)
        if (resposta !== undefined) return resposta
        if (chamada.caminho === `/recrutadores/${id}`) return json({ id, nome: "Autora <script>", descricao: "Perfil próprio", email: "autora@teste", idEmpresa: id === 9 ? 34 : 33, areaRecrutamento: { id: 9, nome: "RH" } })
        if (chamada.caminho === "/competencias") return json(["RH", "Administração"])
        if (chamada.caminho === `/vagas/recrutador/${id}`) return json([10, 11].map(numero => ({ ...vaga(numero), idRecrutador: id })))
        if (chamada.caminho.endsWith("/retribuidos")) return json([])
        if (chamada.caminho.startsWith("/curtidas/candidatos/vaga/")) return json([{ id: 3, descricao: "Perfil anônimo", competencias: [{ id: 9, nome: "RH" }] }])
        if (chamada.caminho === `/matches/recrutador/${id}`) return json([])
        assert.fail("Rota fora da jornada: " + chamada.caminho)
    }
}
const enviar = () => elementos["job-form"].onsubmit({ preventDefault() {} })
function preencher() {
    elementos["job-title"].value = "Analista"
    elementos["job-description"].value = "Descrição"
    elementos["job-location"].value = "Remoto"
    elementos["skills-options"].children[2].children.find(botao => botao.textContent === "RH").click()
    elementos["skills-options"].children[0].children[0].value = "Texto não selecionado"
}

test("perfil próprio, consultas por autor e interessados anônimos em dois contextos de vaga", async () => {
    for (const id of [7, 8, 9]) {
        preparar(undefined, id)
        await exibirPageRecrutador()
        assert.equal(elementos["profile-name"].textContent, "Autora <script>")
        assert.equal(elementos["profile-area"].textContent, "RH")
        assert.equal(elementos["profile-email"].textContent, "autora@teste")
        assert.deepEqual(chamadas.map(c => c.caminho), [`/recrutadores/${id}`, "/competencias", `/vagas/recrutador/${id}`, `/vagas/recrutador/${id}`, "/curtidas/candidatos/vaga/10", "/curtidas/candidatos/vaga/10/retribuidos", "/curtidas/candidatos/vaga/11", "/curtidas/candidatos/vaga/11/retribuidos", `/matches/recrutador/${id}`])
        assert.equal(elementos["job-list"].children.length, 2)
        assert.equal(elementos["candidate-grid"].children.length, 2)
        assert.match(texto(elementos["candidate-grid"]), /Perfil anônimo.*RH.*Vaga 10/)
        assert.ok(!texto(elementos["candidate-grid"]).includes("pessoa@teste"))
        assert.equal(elementos["liked-candidate-grid"].textContent, "Você ainda não tem matches.")
    }
})

test("publicação com catálogo: payload sem empresa/autor, confirmação HTTP, bloqueio simultâneo e recarga", async () => {
    let confirmar
    preparar(c => c.metodo === "POST" ? new Promise(resolve => { confirmar = () => resolve(new Response(null, { status: 201 })) }) : undefined)
    await exibirPageRecrutador(); preencher()
    const pendente = enviar(); await enviar()
    assert.equal(elementos["criar-vaga"].disabled, true)
    assert.equal(elementos["job-title"].value, "Analista")
    assert.equal(elementos["job-status"].textContent, "Publicando vaga...")
    assert.deepEqual(chamadas.filter(c => c.metodo === "POST"), [{ caminho: "/recrutadores/7/vagas", metodo: "POST", corpo: { nome: "Analista", descricao: "Descrição", local: "Remoto", competencias: ["RH"] } }])
    confirmar(); await pendente
    assert.equal(elementos["job-title"].value, "")
    assert.equal(elementos["skills-options"].children[3].children.length, 0)
    assert.equal(elementos["job-status"].textContent, "Vaga cadastrada com sucesso.")
    assert.equal(elementos["job-list"].children.length, 2)
    assert.equal(chamadas.filter(c => c.caminho === "/vagas/recrutador/7").length, 4)
})

test("falhas de criação preservam dados/seleção e permitem nova tentativa; refresh não simula criação", async () => {
    for (const resposta of [() => json({ erro: "Entrada inválida." }, 400), () => json({ erro: "Falha." }, 500), () => { throw new TypeError("Rede") }]) {
        preparar(c => c.metodo === "POST" ? resposta() : undefined)
        await exibirPageRecrutador(); preencher(); await enviar()
        assert.equal(elementos["job-title"].value, "Analista")
        assert.equal(elementos["skills-options"].children[3].children.length, 1)
        assert.equal(elementos["criar-vaga"].disabled, false)
        assert.ok(!elementos["job-status"].textContent.includes("sucesso"))
        assert.equal(chamadas.filter(c => c.caminho === "/vagas/recrutador/7").length, 2)
    }
})

test("catálogo pendente ou indisponível bloqueia publicação sem impedir consulta das próprias vagas", async () => {
    let confirmar
    preparar(c => c.caminho === "/competencias" ? new Promise(resolve => { confirmar = resolve }) : undefined)
    const pendente = exibirPageRecrutador()
    await new Promise(resolve => setImmediate(resolve))
    assert.equal(elementos["criar-vaga"].disabled, true)
    confirmar(json({ erro: "Falha." }, 500)); await pendente
    assert.equal(elementos["criar-vaga"].disabled, true)
    assert.match(elementos["skills-options"].textContent, /Não foi possível/)
    assert.equal(elementos["job-list"].children.length, 2)
    await enviar()
    assert.equal(chamadas.filter(c => c.metodo === "POST").length, 0)
})

test("retribuição aguarda 204, preserva contexto candidato/vaga e atualiza identificação somente por matches", async () => {
    let confirmar, retribuido = false
    preparar(c => {
        if (c.metodo === "POST") return new Promise(resolve => { confirmar = () => { retribuido = true; resolve(new Response(null, { status: 204 })) } })
        if (c.caminho === "/matches/recrutador/7") return json(retribuido ? [match] : [])
    })
    await exibirPageRecrutador()
    const [primeiro, segundo] = elementos["candidate-grid"].children.map(card => card.querySelector(".btn-like"))
    const pendente = primeiro.click(); await primeiro.click()
    assert.equal(primeiro.textContent, "♡")
    assert.equal(segundo.disabled, false)
    assert.equal(elementos["liked-candidate-grid"].children.length, 0)
    confirmar(); await pendente
    assert.equal(primeiro.textContent, "♥")
    assert.deepEqual(chamadas.filter(c => c.metodo === "POST").map(c => c.corpo), [{ idCandidato: 3, idVaga: 10 }])
    const exibido = texto(elementos["liked-candidate-grid"])
    for (const campo of ["Pessoa Candidata", "pessoa@teste", "cpf-c", "30", "SP", "RJ", "RH", "Empresa própria", "cnpj-e", "empresa@teste", "Descrição empresarial", "Vaga do match", "Descrição da vaga", "Administração", "Autora"])
        assert.ok(exibido.includes(campo), campo)
    assert.ok(!texto(elementos["candidate-grid"]).includes("pessoa@teste"))
    await exibirPageRecrutador()
    const repeticao = elementos["candidate-grid"].children[0].querySelector(".btn-like").click()
    confirmar(); await repeticao
    assert.equal(elementos["liked-candidate-grid"].children.length, 1)
})

test("recarga restaura retribuição por vaga, inclusive realizada pela empresa, sem segundo POST", async () => {
    preparar(c => c.caminho === "/curtidas/candidatos/vaga/10/retribuidos" ? json([3]) : undefined)
    for (let recarga = 0; recarga < 2; recarga++) {
        await exibirPageRecrutador()
        const [curtido, disponivel] = elementos["candidate-grid"].children.map(card => card.querySelector(".btn-like"))
        assert.equal(curtido.disabled, true)
        assert.equal(curtido.textContent, "♥")
        assert.ok(curtido.className.includes("curtido"))
        assert.equal(disponivel.disabled, false)
        assert.equal(disponivel.textContent, "♡")
        await curtido.click()
    }
    assert.equal(chamadas.filter(c => c.metodo === "POST").length, 0)
})

test("falha na consulta de retribuição impede renderizar botões disponíveis por suposição", async () => {
    preparar(c => c.caminho.endsWith("/retribuidos") ? json({erro: "Indisponível"}, 500) : undefined)
    await exibirPageRecrutador()
    assert.equal(elementos["candidate-grid"].children.length, 0)
    assert.match(elementos["page-status"].textContent, /Não foi possível/)
    assert.equal(chamadas.filter(c => c.metodo === "POST").length, 0)
})

test("falha na retribuição não confirma card nem atualiza matches; permite tentar de novo", async () => {
    preparar(c => c.metodo === "POST" ? json({ erro: "Falha ao retribuir." }, 500) : undefined)
    await exibirPageRecrutador()
    const botao = elementos["candidate-grid"].children[0].querySelector(".btn-like")
    await botao.click()
    assert.equal(botao.disabled, false)
    assert.equal(botao.textContent, "♡")
    assert.equal(chamadas.filter(c => c.caminho === "/matches/recrutador/7").length, 1)
    assert.equal(avisos.length, 1)
})

test("401/403 por sessão inativa ou ID adulterado interrompem fluxo e voltam ao login sem fallback", async () => {
    for (const status of [401, 403]) {
        for (const rota of ["/recrutadores/7", "/vagas/recrutador/7", "/curtidas/candidatos/vaga/10", "/matches/recrutador/7"]) {
            preparar(c => c.caminho === rota ? json({ erro: "Acesso rejeitado." }, status) : undefined)
            await exibirPageRecrutador()
            assert.equal(chamadas.at(-1).caminho, rota)
            assert.equal(armazenamento.size, 0)
            assert.equal(window.location.href, "./index.html")
        }
        for (const acao of ["publicar", "retribuir"]) {
            preparar(c => c.metodo === "POST" ? json({ erro: "Conta inativa." }, status) : undefined)
            await exibirPageRecrutador()
            if (acao === "publicar") { preencher(); await enviar() }
            else await elementos["candidate-grid"].children[0].querySelector(".btn-like").click()
            assert.equal(armazenamento.size, 0)
            assert.equal(window.location.href, "./index.html")
            assert.equal(chamadas.at(-1).metodo, "POST")
        }
    }
})

test("sessão local ausente/perfil divergente não consulta dados; falha no perfil bloqueia operações", async () => {
    for (const sessao of [null, JSON.stringify({ id: 7, tipo: "empresa" }), "{"]) {
        preparar()
        if (sessao === null) armazenamento.clear()
        else armazenamento.set("sessao", sessao)
        await exibirPageRecrutador()
        assert.equal(chamadas.length, 0)
        assert.equal(window.location.href, "./index.html")
    }
    preparar(() => json({ erro: "Falha." }, 500))
    await exibirPageRecrutador()
    assert.equal(chamadas.length, 1)
    assert.equal(elementos["criar-vaga"].disabled, true)
    assert.match(elementos["page-status"].textContent, /Não foi possível/)
})

test("vagas/interessados vazios e falhas de consulta apresentam estado tratável", async () => {
    preparar(c => c.caminho.startsWith("/vagas/") ? json([]) : undefined)
    await exibirPageRecrutador()
    assert.equal(elementos["job-list"].textContent, "Você ainda não tem vagas.")
    assert.equal(elementos["candidate-grid"].textContent, "Nenhum interessado nas suas vagas.")
    for (const rota of ["/vagas/recrutador/7", "/curtidas/candidatos/vaga/10", "/matches/recrutador/7"]) {
        preparar(c => c.caminho === rota ? json({ erro: "Falha." }, 500) : undefined)
        await exibirPageRecrutador()
        assert.match(elementos["page-status"].textContent, /Não foi possível/)
        assert.equal(window.location.href, "")
    }
})

test("App seleciona a página publicada e HTML oferece somente operações do escopo aprovado", async () => {
    preparar()
    new (carregar("app/App").App)().start()
    await new Promise(resolve => setImmediate(resolve))
    assert.equal(chamadas[0].caminho, "/recrutadores/7")
    assert.equal(elementos["job-list"].children.length, 2)
    const html = fs.readFileSync(require("node:path").join(__dirname, "../src/html/recrutador.html"), "utf8")
    for (const id of Object.keys(elementos)) assert.ok(html.includes(`id="${id}"`), id)
    assert.match(html, /src="\.\.\/js\/bundle.js"/)
    assert.ok(!html.includes("cadastrar-recrutador"))
    assert.ok(!html.includes("type=\"password\""))
})

test("sucesso de publicação/retribuição permanece confirmado se a recarga posterior falhar", async () => {
    let confirmou = false
    preparar(c => {
        if (c.metodo === "POST") { confirmou = true; return new Response(null, { status: 201 }) }
        if (confirmou && c.caminho === "/vagas/recrutador/7") return json({ erro: "Falha." }, 500)
    })
    await exibirPageRecrutador(); preencher(); await enviar()
    assert.equal(elementos["job-status"].textContent, "Vaga cadastrada com sucesso.")
    assert.match(elementos["page-status"].textContent, /Não foi possível/)
    preparar(c => {
        if (c.metodo === "POST") return new Response(null, { status: 204 })
        if (c.caminho === "/matches/recrutador/7") return json({ erro: "Falha." }, 500)
    })
    await exibirPageRecrutador()
    const botao = elementos["candidate-grid"].children[0].querySelector(".btn-like")
    await botao.click()
    assert.equal(botao.textContent, "♥")
    assert.equal(botao.disabled, true)
    assert.match(elementos["page-status"].textContent, /Não foi possível/)
})
