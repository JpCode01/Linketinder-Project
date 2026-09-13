import { Candidato } from "../models/Candidato";
import { Vaga } from "../models/Vaga";
import { CandidatoService } from "../services/CandidatoService"
import { VagaService } from "../services/VagaService"

const candidatoService: CandidatoService = new CandidatoService
const vagaService: VagaService = new VagaService

export function exibirPageCandidato(): void {
    const candidato = candidatoService.buscarCandidatoLogado()
    if (candidato != null) {
        exibirDadosCandidato(candidato)
        exibirVagas(candidato)
        exibirVagasCurtidas(candidato)
    }
}

function exibirVagasCurtidas(candidato: Candidato): void {
    const vagasCurtidas = candidato.getVagasCurtidas

    const contador = document.getElementById("liked-result-count")
    const listaVagas = document.getElementById("liked-job-grid")

    if (listaVagas == null) {
        return
    }

    listaVagas.innerHTML =  ""

    if (contador != null) {
        contador.innerHTML = 
        vagasCurtidas.length.toString() + " vagas curtidas"
    }

    for (const vaga of vagasCurtidas) {

        const card = document.createElement("article")
        card.classList.add("job-card")

        const tipo = document.createElement("span")
        tipo.classList.add("job-type")
        tipo.innerHTML = vaga.tipo

        const nome = document.createElement("h3")
        nome.innerHTML = vaga.nome

        const descricao = document.createElement("p")
        descricao.innerHTML = vaga.descricao

        const localizacao = document.createElement("span")
        localizacao.innerHTML = vaga.localização

        const botao = document.createElement("button")
        botao.classList.add("btn", "btn-success")
        botao.textContent = "Curtido"

        card.appendChild(tipo)
        card.appendChild(nome)
        card.appendChild(descricao)
        card.appendChild(localizacao)
        card.appendChild(botao)

        listaVagas.appendChild(card)
        }
}

function exibirDadosCandidato(candidato: Candidato): void {

    const apelidoAvatar = candidato.nome.split(" ")

    document.getElementById("profile-avatar")!.innerHTML =
        apelidoAvatar[0].charAt(0) + apelidoAvatar[1].charAt(0)   
    document.getElementById("user-avatar")!.innerHTML =
        apelidoAvatar[0].charAt(0) + apelidoAvatar[1].charAt(0)   
    document.getElementById("profile-name")!.innerHTML =
    candidato.nome
    document.getElementById("user-name")!.innerHTML =
    candidato.nome
    document.getElementById("profile-description")!.innerHTML =
    candidato.descricao
}

function exibirVagas(candidato: Candidato): void {

    const vagas = vagaService.exibirVagasEConverter()

    if (vagas == null) {
        return
    }

    document.getElementById("result-count")!.innerHTML = (vagas.length).toString()

    const listaVagas = document.getElementById("job-grid")

    for (const vaga of vagas) {
        const vagaJaCurtida = candidato.getVagasCurtidas.some(
            vagaCurtida => vagaCurtida.nome === vaga.nome
        )

        const card = document.createElement("article")
        card.classList.add("job-card")

        const tipo = document.createElement("span")
        tipo.classList.add("job-type")
        tipo.innerHTML = vaga.tipo

        const nome = document.createElement("h3")
        nome.innerHTML = vaga.nome

        const descricao = document.createElement("p")
        descricao.innerHTML = vaga.descricao

        const localizacao = document.createElement("span")
        localizacao.innerHTML = vaga.localização

        const botao = document.createElement("button")

        botao.classList.add("btn")

        if (vagaJaCurtida) {
            botao.textContent = "Curtido"
            botao.classList.add("btn-success")
        } else {
            botao.textContent = "Curtir"
            botao.classList.add("btn-primary")
        }

        botao.addEventListener("click", () => {
            curtirVaga(vaga, botao, candidato)
        })

        card.appendChild(tipo)
        card.appendChild(nome)
        card.appendChild(descricao)
        card.appendChild(localizacao)
        if (listaVagas != null) {
            listaVagas.appendChild(card)
        }
        card.appendChild(botao)



    }
}

function curtirVaga(vaga: Vaga, botao: HTMLButtonElement, candidato: Candidato): void {
    if (candidato == null || vaga == null) {
        return
    }

    const curtido = candidatoService.curtirVaga(candidato, vaga)

    if (curtido) {
        candidato.addVaga(vaga)

        botao.textContent = "Curtido"
        botao.classList.remove("btn-primary")
        botao.classList.add("btn-success")
    }
} 