import { Candidato } from "../../models/Candidato"
import { Vaga } from "../../models/Vaga"
import { VagaService } from "../../services/VagaService"
import { CandidatoService } from "../../services/CandidatoService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService"
import { CandidatoJaCurtidoException } from "../../exceptions/candidato/CandidatoJaCurtidoException"

export function exibirCandidatosEmpresa(
    idEmpresa: number,
    vagaService: VagaService,
    candidatoService: CandidatoService,
    curtidaVagaService: CurtidaVagaService,
    curtidaCandidatoService: CurtidaCandidatoService
): void {
    const grid = document.getElementById("candidate-grid")

    if (grid == null) {
        return
    }

    const vagas = vagaService.buscarPorEmpresa(idEmpresa)

    const curtidasCandidatos =
        curtidaCandidatoService.buscarPorEmpresa(idEmpresa)

    grid.innerHTML = ""

    let quantidadeCandidatos = 0

    vagas.forEach((vaga) => {
        const curtidas =
            curtidaVagaService.buscarPorVaga(vaga.id)

        const contador =
            document.getElementById("candidate-count")

        curtidas.forEach((curtida) => {
            const candidato =
                candidatoService.buscarPorId(curtida.idCandidato)

            const candidatoJaCurtido =
                curtidasCandidatos.some(
                    (curtidaCandidato) =>
                        curtidaCandidato.idCandidato === candidato.id &&
                        curtidaCandidato.idVaga === vaga.id
                )

            const card = criarCardCandidato(
                candidato,
                vaga,
                candidatoJaCurtido,
                () =>
                    curtirCandidato(
                        idEmpresa,
                        candidato.id,
                        vaga.id,
                        card,
                        curtidaCandidatoService
                    )
            )

            grid.appendChild(card)

            quantidadeCandidatos++
        })

        if (contador != null) {
            contador.textContent =
                quantidadeCandidatos.toString()
        }
    })
}

function criarCardCandidato(
    candidato: Candidato,
    vaga: Vaga,
    candidatoJaCurtido: boolean,
    aoCurtir: () => void
): HTMLElement {
    const article = document.createElement("article")
    article.className = "candidate-card"

    article.appendChild(
        criarTopoCandidato()
    )

    article.appendChild(
        criarSecaoFormacao(candidato)
    )

    article.appendChild(
        criarSecaoCompetencias(candidato)
    )

    article.appendChild(
        criarSecaoVaga(vaga)
    )

    article.appendChild(
        criarRodapeCandidato(
            candidatoJaCurtido,
            aoCurtir
        )
    )

    return article
}

function criarTopoCandidato(): HTMLElement {
    const topo = document.createElement("div")
    topo.className = "candidate-top"

    const avatar = document.createElement("div")
    avatar.className = "candidate-avatar"
    avatar.textContent = "?"

    const nome = document.createElement("h3")
    nome.textContent = "Candidato"

    topo.appendChild(avatar)
    topo.appendChild(nome)

    return topo
}

function criarSecaoFormacao(
    candidato: Candidato
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Formação"

    const formacao = document.createElement("p")
    formacao.textContent = candidato.formacao

    secao.appendChild(titulo)
    secao.appendChild(formacao)

    return secao
}

function criarSecaoCompetencias(
    candidato: Candidato
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Competências"

    const tags = document.createElement("div")
    tags.className = "job-tags"

    candidato.competencias.forEach((competencia) => {
        const tag = document.createElement("span")
        tag.textContent = competencia

        tags.appendChild(tag)
    })

    secao.appendChild(titulo)
    secao.appendChild(tags)

    return secao
}

function criarSecaoVaga(
    vaga: Vaga
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Vaga de interesse"

    const nomeVaga = document.createElement("p")
    nomeVaga.textContent = vaga.nome

    secao.appendChild(titulo)
    secao.appendChild(nomeVaga)

    return secao
}

function criarRodapeCandidato(
    candidatoJaCurtido: boolean,
    aoCurtir: () => void
): HTMLElement {
    const rodape = document.createElement("div")
    rodape.className = "candidate-footer"

    const botao = document.createElement("button")
    botao.className = "btn-like"

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
    idVaga: number,
    card: HTMLElement,
    curtidaCandidatoService: CurtidaCandidatoService
): void {
    try {
        curtidaCandidatoService.curtir(
            idEmpresa,
            idCandidato,
            idVaga
        )

        atualizarCardCandidatoCurtido(card)
    } catch (erro) {
        if (erro instanceof CandidatoJaCurtidoException) {
            atualizarCardCandidatoCurtido(card)
            return
        }

        throw erro
    }
}

function atualizarCardCandidatoCurtido(
    card: HTMLElement
): void {
    const botao =
        card.querySelector(".btn-like") as HTMLButtonElement | null

    if (botao == null) {
        return
    }

    botao.textContent = "♥"
    botao.title = "Candidato curtido"
    botao.disabled = true
}