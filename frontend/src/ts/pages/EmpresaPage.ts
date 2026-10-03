import { Empresa } from "../models/Empresa"
import { Vaga } from "../models/Vaga"
import { Candidato } from "../models/Candidato"
import { RepositoryConfig } from "../config/RepositoryConfig"
import { ServiceConfig } from "../config/ServiceConfig"
import { Competencia } from "../models/Competencia"
import { VagaService } from "../services/VagaService"
import { CandidatoService } from "../services/CandidatoService"
import { CurtidaVagaService } from "../services/CurtidaVagaService"
import { EmpresaService } from "../services/EmpresaService"
import { CurtidaCandidatoService } from "../services/CurtidaCandidatoService"
import { CandidatoJaCurtidoException } from "../exceptions/candidato/CandidatoJaCurtidoException"

interface Sessao {
    id: number
    tipo: string
}

interface DadosVagaFormulario {
    nome: string
    descricao: string
    tipo: string
    localizacao: string
    competencias: Competencia[]
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const empresaService: EmpresaService =
    serviceConfig.criarEmpresaService()

const vagaService: VagaService = 
    serviceConfig.criarVagaService()

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

const curtidaVagaService: CurtidaVagaService =
    serviceConfig.criarCurtidaVagaService()

const curtidaCandidatoService: CurtidaCandidatoService =
    serviceConfig.criarCurtidaCandidatoService()

export function exibirPageEmpresa(): void {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "EMPRESA") {
        return
    }

    const empresa = empresaService.buscarPorId(sessao.id)

    exibirDadosEmpresa(empresa)
    carregarOpcoesCompetencias()
    configurarCriacaoVaga(empresa.id)
    exibirVagasEmpresa(empresa.id)
    exibirCandidatosEmpresa(empresa.id)
    exibirCandidatosCurtidos(empresa.id)
}

function obterSessao(): Sessao | undefined {
    const sessaoSalva = localStorage.getItem("sessao")

    if (sessaoSalva == null) {
        return undefined
    }

    return JSON.parse(sessaoSalva) as Sessao
}

function exibirDadosEmpresa(empresa: Empresa): void {
    const iniciais = obterIniciais(empresa.nome)

    document.getElementById("profile-avatar")!.textContent = iniciais
    document.getElementById("profile-name")!.textContent = empresa.nome
    document.getElementById("profile-description")!.textContent =
        empresa.descricao
}

function obterIniciais(nome: string): string {
    const partesNome = nome.trim().split(" ")

    if (partesNome.length === 1) {
        return partesNome[0].charAt(0).toUpperCase()
    }

    return (
        partesNome[0].charAt(0) +
        partesNome[partesNome.length - 1].charAt(0)
    ).toUpperCase()
}

function carregarOpcoesCompetencias(): void {
    const containerCompetencias =
        document.getElementById("skills-options")

    if (containerCompetencias == null) {
        return
    }

    for (const competencia of Object.values(Competencia)) {
        const opcaoCompetencia = document.createElement("div")
        opcaoCompetencia.classList.add("skill-option")

        const checkbox = document.createElement("input")
        checkbox.type = "checkbox"
        checkbox.name = "competencias"
        checkbox.value = competencia
        checkbox.id = `competencia-${competencia}`

        const label = document.createElement("label")
        label.htmlFor = checkbox.id
        label.textContent = competencia

        opcaoCompetencia.appendChild(checkbox)
        opcaoCompetencia.appendChild(label)

        containerCompetencias.appendChild(opcaoCompetencia)
    }
}

function capturarDadosVaga(): DadosVagaFormulario {
    const nome: string =
        (document.getElementById("job-title") as HTMLInputElement).value

    const tipo: string =
        (document.getElementById("job-type") as HTMLSelectElement).value

    const localizacao: string =
        (document.getElementById("job-location") as HTMLInputElement).value

    const descricao: string =
        (document.getElementById("job-description") as HTMLTextAreaElement).value

    const competencias: Competencia[] =
        capturarCompetenciasSelecionadas()

    return {
        nome,
        descricao,
        tipo,
        localizacao,
        competencias
    }
}

function capturarCompetenciasSelecionadas(): Competencia[] {
    const checkboxesSelecionados =
        document.querySelectorAll<HTMLInputElement>(
            '#skills-options input[type="checkbox"]:checked'
        )

    return Array.from(checkboxesSelecionados).map(
        checkbox => checkbox.value as Competencia
    )
}

function construirVaga(dados: DadosVagaFormulario, idEmpresa: number): Vaga {
    const vaga: Vaga = new Vaga(
        dados.nome,
        dados.descricao,
        dados.tipo,
        dados.localizacao,
        idEmpresa
    )

    vaga.competencias = dados.competencias

    return vaga
}

function configurarCriacaoVaga(idEmpresa: number): void {
    document.getElementById("criar-vaga")!.onclick = (): void => {
        const dados: DadosVagaFormulario = capturarDadosVaga()

        const vaga: Vaga = construirVaga(dados, idEmpresa)

        vagaService.salvar(vaga)
        limparFormularioVaga()
    }
}

function exibirVagasEmpresa(idEmpresa: number): void {
    const listaVagas = document.getElementById("job-list")

    if (listaVagas == null) {
        return
    }

    const vagas = vagaService.buscarPorEmpresa(idEmpresa)

    listaVagas.innerHTML = ""

    for (const vaga of vagas) {
        const card = criarCardVaga(vaga)
        listaVagas.appendChild(card)
    }
}

function criarCardVaga(vaga: Vaga): HTMLElement {
    const card = document.createElement("article")
    card.classList.add("company-job-card")

    const nome = document.createElement("h3")
    nome.textContent = vaga.nome

    const tipo = document.createElement("span")
    tipo.classList.add("job-type")
    tipo.textContent = vaga.tipo

    const descricao = document.createElement("p")
    descricao.classList.add("job-description")
    descricao.textContent = vaga.descricao

    const competencias = document.createElement("div")
    competencias.classList.add("job-tags")

    for (const competencia of vaga.competencias) {
        const competenciaElement = document.createElement("span")
        competenciaElement.textContent = competencia
        competencias.appendChild(competenciaElement)
    }

    const localizacao = document.createElement("span")
    localizacao.classList.add("job-location")
    localizacao.textContent = vaga.localizacao

    card.appendChild(nome)
    card.appendChild(tipo)
    card.appendChild(descricao)
    card.appendChild(competencias)
    card.appendChild(localizacao)

    return card
}

function exibirCandidatosEmpresa(idEmpresa: number): void {
    const listaCandidatos = document.getElementById("candidate-grid")

    if (listaCandidatos == null) {
        return
    }

    const vagas = vagaService.buscarPorEmpresa(idEmpresa)
    const curtidasCandidatos =
        curtidaCandidatoService.buscarPorEmpresa(idEmpresa)

    listaCandidatos.innerHTML = ""

    let quantidadeCandidatos: number = 0

    for (const vaga of vagas) {
        const curtidas = curtidaVagaService.buscarPorVaga(vaga.id)
        const contadorCandidatos = document.getElementById("candidate-count")

        for (const curtida of curtidas) {
            const candidato = candidatoService.buscarPorId(curtida.idCandidato)

            const candidatoJaCurtido = curtidasCandidatos.some(
                curtida => curtida.idCandidato === candidato.id
            )

            const card = criarCardCandidato(
                candidato,
                vaga,
                candidatoJaCurtido,
                () => curtirCandidato(idEmpresa, candidato.id, card)
            )

            listaCandidatos.appendChild(card)
            quantidadeCandidatos++
        }

        if (contadorCandidatos != null) {
        contadorCandidatos.textContent =
            quantidadeCandidatos + " candidatos"
        }
    }
}

function criarCardCandidato(
    candidato: Candidato,
    vaga: Vaga,
    candidatoJaCurtido: boolean,
    aoCurtir: () => void
): HTMLElement {
    const card = document.createElement("article")
    card.classList.add("candidate-card")

    card.appendChild(criarTopoCandidato())
    card.appendChild(criarSecaoFormacao(candidato))
    card.appendChild(criarSecaoCompetencias(candidato))
    card.appendChild(criarSecaoVaga(vaga))
    card.appendChild(
        criarRodapeCandidato(candidatoJaCurtido, aoCurtir)
    )

    return card
}

function criarTopoCandidato(): HTMLElement {
    const topo = document.createElement("div")
    topo.classList.add("candidate-top")

    const avatar = document.createElement("div")
    avatar.classList.add("anonymous-avatar")
    avatar.textContent = "?"

    const nome = document.createElement("h3")
    nome.textContent = "Candidato"

    topo.appendChild(avatar)
    topo.appendChild(nome)

    return topo
}

function criarSecaoFormacao(candidato: Candidato): HTMLElement {
    const secao = document.createElement("div")
    secao.classList.add("candidate-section")

    const titulo = document.createElement("strong")
    titulo.textContent = "Formação"

    const formacao = document.createElement("p")
    formacao.textContent = candidato.formacao

    secao.appendChild(titulo)
    secao.appendChild(formacao)

    return secao
}

function criarSecaoCompetencias(candidato: Candidato): HTMLElement {
    const secao = document.createElement("div")
    secao.classList.add("candidate-section")

    const titulo = document.createElement("strong")
    titulo.textContent = "Competências"

    const lista = document.createElement("div")
    lista.classList.add("job-tags")

    for (const competencia of candidato.competencias) {
        const competenciaElement = document.createElement("span")
        competenciaElement.textContent = competencia
        lista.appendChild(competenciaElement)
    }

    secao.appendChild(titulo)
    secao.appendChild(lista)

    return secao
}

function criarSecaoVaga(vaga: Vaga): HTMLElement {
    const secao = document.createElement("div")
    secao.classList.add("candidate-section")

    const titulo = document.createElement("strong")
    titulo.textContent = "Vaga de interesse"

    const nomeVaga = document.createElement("p")
    nomeVaga.textContent = vaga.nome

    secao.appendChild(titulo)
    secao.appendChild(nomeVaga)

    return secao
}

function criarRodapeCandidato(candidatoJaCurtido: boolean, aoCurtir: () => void): HTMLElement {
        const rodape = document.createElement("div")
    rodape.classList.add("candidate-footer")

    const botao = document.createElement("button")
    botao.classList.add("btn-like")

    if (candidatoJaCurtido) {
        botao.textContent = "♥"
        botao.title = "Candidato curtido"
        botao.disabled = true
    } else {
        botao.textContent = "♡"
        botao.title = "Curtir candidato"
        botao.addEventListener("click", aoCurtir)
    }

    rodape.appendChild(botao)

    return rodape
}

function curtirCandidato(
    idEmpresa: number,
    idCandidato: number,
    card: HTMLElement
): void {
    try {
        curtidaCandidatoService.curtir(idEmpresa, idCandidato)
        atualizarCardCandidatoCurtido(card)
    } catch (erro) {
        if (erro instanceof CandidatoJaCurtidoException) {
            atualizarCardCandidatoCurtido(card)
            return
        }

        throw erro
    }
}

function atualizarCardCandidatoCurtido(card: HTMLElement): void {
    const botao = card.querySelector<HTMLButtonElement>(".btn-like")

    if (botao == null) {
        return
    }

    botao.textContent = "♥"
    botao.title = "Candidato curtido"
    botao.disabled = true
}

function limparFormularioVaga(): void {
    const nome = document.getElementById("job-title") as HTMLInputElement
    const tipo = document.getElementById("job-type") as HTMLSelectElement
    const localizacao =
        document.getElementById("job-location") as HTMLInputElement
    const descricao =
        document.getElementById("job-description") as HTMLTextAreaElement

    nome.value = ""
    tipo.value = "CLT"
    localizacao.value = ""
    descricao.value = ""

    const checkboxes =
        document.querySelectorAll<HTMLInputElement>(
            '#skills-options input[type="checkbox"]'
        )

    for (const checkbox of checkboxes) {
        checkbox.checked = false
    }
}

function exibirCandidatosCurtidos(idEmpresa: number): void {
    const listaCandidatos =
        document.getElementById("liked-candidate-grid")

    const contadorCandidatos =
        document.getElementById("liked-candidate-count")

    if (listaCandidatos == null) {
        return
    }

    const curtidasCandidatos =
        curtidaCandidatoService.buscarPorEmpresa(idEmpresa)

    const vagas = vagaService.buscarPorEmpresa(idEmpresa)

    listaCandidatos.innerHTML = ""

    let quantidadeCandidatos: number = 0

    for (const curtidaCandidato of curtidasCandidatos) {
        const candidato =
            candidatoService.buscarPorId(curtidaCandidato.idCandidato)

        for (const vaga of vagas) {
            const curtidas =
                curtidaVagaService.buscarPorVaga(vaga.id)

            const candidatoCurtiuVaga = curtidas.some(
                curtida => curtida.idCandidato === candidato.id
            )

            if (candidatoCurtiuVaga) {
                const card = criarCardCandidatoCurtido(
                    candidato,
                    vaga
                )

                listaCandidatos.appendChild(card)
                quantidadeCandidatos++
            }
        }
    }
    
    if (contadorCandidatos != null) {
        contadorCandidatos.textContent = quantidadeCandidatos + " candidatos"
    }
}

function criarCardCandidatoCurtido(
    candidato: Candidato,
    vaga: Vaga
): HTMLElement {
    const card = document.createElement("article")
    card.classList.add("candidate-card")

    card.appendChild(criarTopoCandidatoCurtido(candidato))
    card.appendChild(criarSecaoFormacao(candidato))
    card.appendChild(criarSecaoCompetencias(candidato))
    card.appendChild(criarSecaoVaga(vaga))

    return card
}

function criarTopoCandidatoCurtido(candidato: Candidato): HTMLElement {
    const topo = document.createElement("div")
    topo.classList.add("candidate-top")

    const avatar = document.createElement("div")
    avatar.classList.add("anonymous-avatar")
    avatar.textContent = obterIniciais(candidato.nome)

    const nome = document.createElement("h3")
    nome.textContent = candidato.nome

    topo.appendChild(avatar)
    topo.appendChild(nome)

    return topo
}