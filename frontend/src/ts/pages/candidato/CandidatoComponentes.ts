import { Vaga } from "../../models/Vaga"

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
    vaga: Vaga,
    vagaJaCurtida: boolean,
    aoCurtir?: () => void
): HTMLElement {
    const card = document.createElement("article")
    card.classList.add("job-card")

    card.appendChild(
        criarTopoVaga(vaga)
    )

    const nome = document.createElement("h3")
    nome.textContent = vaga.nome

    const descricao = document.createElement("p")
    descricao.classList.add("job-description")
    descricao.textContent = vaga.descricao

    card.appendChild(nome)
    card.appendChild(descricao)

    card.appendChild(
        criarCompetenciasVaga(vaga)
    )

    card.appendChild(
        criarRodapeVaga(
            vaga,
            vagaJaCurtida,
            aoCurtir
        )
    )

    return card
}

function criarRodapeVaga(
    vaga: Vaga,
    vagaJaCurtida: boolean,
    aoCurtir?: () => void
): HTMLElement {
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

function criarCompetenciasVaga(
    vaga: Vaga
): HTMLElement {
    const competencias = document.createElement("div")
    competencias.classList.add("job-tags")

    for (const competencia of vaga.competencias) {
        const competenciaElement =
            document.createElement("span")

        competenciaElement.textContent = competencia

        competencias.appendChild(
            competenciaElement
        )
    }

    return competencias
}

function criarTopoVaga(
    vaga: Vaga
): HTMLElement {
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

function criarBotaoCurtir(
    vagaJaCurtida: boolean,
    aoCurtir: () => void
): HTMLButtonElement {
    const botao = document.createElement("button")
    botao.classList.add("btn-like")

    botao.textContent =
        vagaJaCurtida ? "♥" : "♡"

    botao.title =
        vagaJaCurtida
            ? "Vaga curtida"
            : "Curtir vaga"

    botao.disabled = vagaJaCurtida

    if (!vagaJaCurtida) {
        botao.addEventListener(
            "click",
            aoCurtir
        )
    }

    return botao
}

export function atualizarCardCurtido(
    card: HTMLElement
): void {
    const botao =
        card.querySelector<HTMLButtonElement>(
            ".btn-like"
        )

    if (botao == null) {
        return
    }

    botao.textContent = "♥"
    botao.title = "Vaga curtida"
    botao.disabled = true
}