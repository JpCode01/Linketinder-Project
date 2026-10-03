import { Candidato } from "../models/Candidato"
import { Vaga } from "../models/Vaga"
import { CurtidaVaga } from "../models/CurtidaVaga"
import { CandidatoService } from "../services/CandidatoService"
import { VagaService } from "../services/VagaService"
import { CurtidaVagaService } from "../services/CurtidaVagaService"
import { VagaJaCurtidaException } from "../exceptions/vaga/VagaJaCurtidaException"
import { RepositoryConfig } from "../config/RepositoryConfig"
import { ServiceConfig } from "../config/ServiceConfig";

interface Sessao {
    id: number
    tipo: "CANDIDATO" | "EMPRESA"
}

interface DadosContador {
    contador: HTMLElement | null
    quantidade: number
    singular: string
    plural: string
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const candidatoService: CandidatoService = serviceConfig.criarCandidatoService()
const vagaService: VagaService = serviceConfig.criarVagaService()
const curtidaVagaService: CurtidaVagaService = serviceConfig.criarCurtidaVagaService()

export function exibirPageCandidato(): void {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "CANDIDATO") {
        return
    }

    const candidato = candidatoService.buscarPorId(sessao.id)

    exibirDadosCandidato(candidato)
    exibirVagas(candidato)
    exibirVagasCurtidas(candidato)
}

function exibirVagasCurtidas(candidato: Candidato): void {
    const listaVagas = document.getElementById("liked-job-grid")
    const contador = document.getElementById("liked-result-count")

    if (listaVagas == null) {
        return
    }

    const vagasCurtidas = buscarVagasCurtidas(candidato)

    listaVagas.innerHTML = ""

    atualizarContador({
        contador: contador,
        quantidade: vagasCurtidas.length,
        singular: "vaga curtida",
        plural: "vagas curtidas"
    })

    for (const vaga of vagasCurtidas) {
        const card = criarCardVaga(vaga, true)

        listaVagas.appendChild(card)
    }
}


function buscarVagasCurtidas(candidato: Candidato): Vaga[] {
    const curtidas =
        curtidaVagaService.buscarPorCandidato(candidato.id)

    const vagasCurtidas: Vaga[] = []

    for (const curtida of curtidas) {
        const vaga = vagaService.buscarPorId(curtida.idVaga)

        if (vaga != undefined) {
            vagasCurtidas.push(vaga)
        }
    }

    return vagasCurtidas
}

function obterSessao(): Sessao | undefined {
    const sessaoSalva = localStorage.getItem("sessao")

    if (sessaoSalva == null) {
        return undefined
    }

    return JSON.parse(sessaoSalva) as Sessao
}

function exibirDadosCandidato(candidato: Candidato): void {
    const iniciais = obterIniciais(candidato.nome) 
     
    document.getElementById("profile-avatar")!.textContent = iniciais
    document.getElementById("user-avatar")!.textContent = iniciais
    document.getElementById("profile-name")!.textContent = candidato.nome
    document.getElementById("user-name")!.textContent = candidato.nome
    document.getElementById("profile-description")!.textContent = candidato.descricao
   
    exibirCompetencias(candidato)
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

function exibirCompetencias(candidato: Candidato): void {
    const listaCompetencias = document.getElementById("profile-skills") 
    
    if (listaCompetencias == null) {
         return
    } 
    
    listaCompetencias.innerHTML = "" 
    
    for (const competencia of candidato.competencias) {
        const competenciaElement = document.createElement("span")

        competenciaElement.classList.add("skill")
        competenciaElement.textContent = competencia
        
        listaCompetencias.appendChild(competenciaElement)
    }
}

function exibirVagas(candidato: Candidato): void {
    const listaVagas = document.getElementById("job-grid")
    const contador = document.getElementById("result-count")

    if (listaVagas == null) {
        return
    }

    const vagas = vagaService.buscarTodos()
    const curtidas = curtidaVagaService.buscarPorCandidato(candidato.id)

    listaVagas.innerHTML = ""

    atualizarContador({
        contador: contador,
        quantidade: vagas.length,
        singular: "vaga",
        plural: "vagas"
    })

    for (const vaga of vagas) {
        const vagaJaCurtida = vagaFoiCurtida(vaga, curtidas)
        
        const card = criarCardVaga(
            vaga, 
            vagaJaCurtida, 
            () => curtirVaga(candidato.id, vaga, card)
        ) 
        
        listaVagas.appendChild(card) 
    }
}

function criarCardVaga(vaga: Vaga, vagaJaCurtida: boolean, aoCurtir?: () => void): HTMLElement {
    const card = document.createElement("article")
    card.classList.add("job-card")

    card.appendChild(criarTopoVaga(vaga))

    const nome = document.createElement("h3")
    nome.textContent = vaga.nome

    const descricao = document.createElement("p")
    descricao.classList.add("job-description")
    descricao.textContent = vaga.descricao

    card.appendChild(nome)
    card.appendChild(descricao)
    card.appendChild(criarCompetenciasVaga(vaga))
    card.appendChild(
        criarRodapeVaga(vaga, vagaJaCurtida, aoCurtir)
    )

    return card
}

function criarRodapeVaga(vaga: Vaga, vagaJaCurtida: boolean, aoCurtir?: () => void): HTMLElement {
    const rodape = document.createElement("div")
    rodape.classList.add("job-footer")

    const localizacao = document.createElement("span")
    localizacao.classList.add("job-location")
    localizacao.textContent = vaga.localizacao

    rodape.appendChild(localizacao)

    if (aoCurtir != undefined) {
        const botao = criarBotaoCurtir(
            vagaJaCurtida,
            aoCurtir
        )

        rodape.appendChild(botao)
    }

    return rodape
}

function criarCompetenciasVaga(vaga: Vaga): HTMLElement {
    const competencias = document.createElement("div")
    competencias.classList.add("job-tags")

    for (const competencia of vaga.competencias) {
        const competenciaElement = document.createElement("span")
        competenciaElement.textContent = competencia

        competencias.appendChild(competenciaElement)
    }

    return competencias
}

function criarTopoVaga(vaga: Vaga): HTMLElement {
    const topo = document.createElement("div")
    topo.classList.add("job-top")

    const tipo = document.createElement("span")
    tipo.classList.add("job-type")
    tipo.textContent = vaga.tipo

    const empresa = document.createElement("span")
    empresa.classList.add("anonymous")
    empresa.textContent = "Empresa"

    topo.appendChild(tipo)
    topo.appendChild(empresa)

    return topo
}



function curtirVaga(idCandidato: number, vaga: Vaga, card: HTMLElement ): void {
    try { 
        curtidaVagaService.curtir(idCandidato, vaga.id)
        atualizarCardCurtido(card) 
    } catch (erro) { 
        if (erro instanceof VagaJaCurtidaException) { 
            atualizarCardCurtido(card) 
            return 
        } 
        
        throw erro 
    } 
}

function atualizarCardCurtido(card: HTMLElement): void {
    const botao = card.querySelector<HTMLButtonElement>(".btn-like") 
    if (botao == null) { 
        return 
    } 
    
    botao.textContent = "♥" 
    botao.title = "Vaga curtida" 
    botao.disabled = true 
}

function criarBotaoCurtir(vagaJaCurtida: boolean, aoCurtir: () => void ): HTMLButtonElement { 
    const botao = document.createElement("button") 
    botao.classList.add("btn-like") 

    botao.textContent = vagaJaCurtida ? "♥" : "♡" 
    botao.title = vagaJaCurtida 
    ? "Vaga curtida" : "Curtir vaga" 
    botao.disabled = vagaJaCurtida 
    
    if (!vagaJaCurtida) {
        botao.addEventListener("click", aoCurtir) } 
        return botao 
    }

function atualizarContador(dadosContador : DadosContador ): void {

    if (dadosContador.contador == null) {
        return
    }
    
    dadosContador.contador.textContent = dadosContador.quantidade + " " + (
        dadosContador.quantidade === 1 
        ? dadosContador.singular : dadosContador.plural) 
}

function vagaFoiCurtida(vaga: Vaga, curtidas: CurtidaVaga[]): boolean {
    return curtidas.some(
        curtida => curtida.idVaga === vaga.id
    )
}