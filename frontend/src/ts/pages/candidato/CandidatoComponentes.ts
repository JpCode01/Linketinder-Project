import { VagaAnonimaResponseDTO } from "../../api/dto/vaga/VagaAnonimaResponseDTO"

export interface DadosContador {
    contador: HTMLElement | null
    quantidade: number
    singular: string
    plural: string
}

export function atualizarContador(
    dadosContador: DadosContador
): void {
    if (dadosContador.contador == null) {
        return
    }

    dadosContador.contador.textContent =
        dadosContador.quantidade +
        " " +
        (
            dadosContador.quantidade === 1
                ? dadosContador.singular
                : dadosContador.plural
        )
}

export function criarCardVaga(
    vaga: VagaAnonimaResponseDTO,
    vagaJaCurtida: boolean,
    aoCurtir?: () => void
): HTMLElement {
    const card = document.createElement("article")
    card.classList.add("job-card")

    const descricao = document.createElement("p")
    descricao.classList.add("job-description")
    descricao.textContent = vaga.descricao

    card.appendChild(descricao)
    card.appendChild(criarCompetenciasVaga(vaga))

    if (aoCurtir != undefined) {
        const rodape = document.createElement("div")
        rodape.classList.add("job-footer")

        rodape.appendChild(
            criarBotaoCurtir(vagaJaCurtida, aoCurtir)
        )

        card.appendChild(rodape)
    }

    return card
}

function criarCompetenciasVaga(
    vaga: VagaAnonimaResponseDTO
): HTMLElement {
    const competencias = document.createElement("div")
    competencias.classList.add("job-tags")

    for (const competencia of vaga.competencias) {
        const competenciaElement = document.createElement("span")
        competenciaElement.classList.add("job-tag")
        competenciaElement.textContent = competencia.nome

        competencias.appendChild(competenciaElement)
    }

    return competencias
}

function criarBotaoCurtir(
    vagaJaCurtida: boolean,
    aoCurtir: () => void
): HTMLButtonElement {
    const botao = document.createElement("button")
    botao.classList.add("btn-like")

    botao.textContent = vagaJaCurtida ? "♥" : "♡"
    botao.title = vagaJaCurtida ? "Vaga curtida" : "Curtir vaga"
    botao.disabled = vagaJaCurtida

    if (!vagaJaCurtida) {
        botao.addEventListener("click", aoCurtir)
    }

    return botao
}

export function atualizarCardCurtido(
    card: HTMLElement
): void {
    const botao =
        card.querySelector<HTMLButtonElement>(".btn-like")

    if (botao == null) {
        return
    }

    botao.textContent = "♥"
    botao.title = "Vaga curtida"
    botao.disabled = true
}