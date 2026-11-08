import { MatchResponseDTO } from "../../api/dto/match/MatchResponseDTO"
import { MatchService } from "../../services/MatchService"

export async function exibirMatchesCandidato(
    idCandidato: number,
    matchService: MatchService
): Promise<void> {
    const lista = document.getElementById("match-grid")
    const contador = document.getElementById("match-count")

    if (lista == null) {
        return
    }

    lista.innerHTML = ""

    try {
        const matches = await matchService.buscarPorCandidato(idCandidato)

        for (const match of matches) {
            lista.appendChild(criarCardMatchCandidato(match))
        }

        if (contador != null) {
            contador.textContent = `${matches.length} matches`
        }
    } catch (erro: unknown) {
        console.error("Erro ao carregar matches do candidato:", erro)

        if (contador != null) {
            contador.textContent = "Erro ao carregar matches"
        }
    }
}

function criarCardMatchCandidato(
    match: MatchResponseDTO
): HTMLElement {
    const article = document.createElement("article")
    article.className = "candidate-card"

    const topo = document.createElement("div")
    topo.className = "candidate-top"

    const nomeEmpresa = document.createElement("h3")
    nomeEmpresa.textContent = match.nomeEmpresa
    topo.appendChild(nomeEmpresa)

    const descricaoEmpresa = document.createElement("p")
    descricaoEmpresa.textContent = match.descricaoEmpresa || "Sem descrição cadastrada"

    const emailEmpresa = document.createElement("p")
    emailEmpresa.textContent = `E-mail corporativo: ${match.emailCorporativo}`

    const secaoVaga = document.createElement("div")
    secaoVaga.className = "candidate-section"

    const tituloVaga = document.createElement("strong")
    tituloVaga.textContent = "Vaga do match"

    const nomeVaga = document.createElement("p")
    nomeVaga.textContent = match.nome

    const descricaoVaga = document.createElement("p")
    descricaoVaga.textContent = match.descricao

    const tituloCompetencias = document.createElement("strong")
    tituloCompetencias.textContent = "Competências da vaga"

    const tags = document.createElement("div")
    tags.className = "job-tags"

    for (const competencia of match.competencias) {
        const tag = document.createElement("span")
        tag.textContent = competencia
        tags.appendChild(tag)
    }

    secaoVaga.appendChild(tituloVaga)
    secaoVaga.appendChild(nomeVaga)
    secaoVaga.appendChild(descricaoVaga)
    secaoVaga.appendChild(tituloCompetencias)
    secaoVaga.appendChild(tags)

    article.appendChild(topo)
    article.appendChild(descricaoEmpresa)
    article.appendChild(emailEmpresa)
    article.appendChild(secaoVaga)

    return article
}