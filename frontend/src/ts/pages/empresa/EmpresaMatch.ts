import { MatchResponseDTO } from "../../api/dto/match/MatchResponseDTO"
import { MatchService } from "../../services/MatchService"
import { obterIniciais } from "./EmpresaPerfil"

export async function exibirMatchesEmpresa(
    idEmpresa: number,
    matchService: MatchService
): Promise<void> {
    const lista = document.getElementById("liked-candidate-grid")
    const contador = document.getElementById("liked-candidate-count")

    if (lista == null) {
        return
    }

    lista.innerHTML = ""

    try {
        const matches = await matchService.buscarPorEmpresa(idEmpresa)

        for (const match of matches) {
            lista.appendChild(criarCardMatchEmpresa(match))
        }

        if (contador != null) {
            contador.textContent = `${matches.length} matches`
        }
    } catch (erro: unknown) {
        console.error("Erro ao carregar matches da empresa:", erro)

        if (contador != null) {
            contador.textContent = "Erro ao carregar matches"
        }
    }
}

function criarCardMatchEmpresa(
    match: MatchResponseDTO
): HTMLElement {
    const article = document.createElement("article")
    article.className = "candidate-card"

    const topo = document.createElement("div")
    topo.className = "candidate-top"

    const avatar = document.createElement("div")
    avatar.className = "candidate-avatar"
    avatar.textContent = obterIniciais(match.nomeCandidato)

    const nome = document.createElement("h3")
    nome.textContent = `${match.nomeCandidato} ${match.sobrenomeCandidato}`

    topo.appendChild(avatar)
    topo.appendChild(nome)

    const descricao = document.createElement("p")
    descricao.textContent = match.descricaoCandidato || "Sem descrição cadastrada"

    const email = document.createElement("p")
    email.textContent = `E-mail: ${match.emailCandidato}`

    const secaoCompetencias = document.createElement("div")
    secaoCompetencias.className = "candidate-section"

    const tituloCompetencias = document.createElement("strong")
    tituloCompetencias.textContent = "Competências do candidato"

    const tags = document.createElement("div")
    tags.className = "job-tags"

    for (const competencia of match.competenciasCandidato) {
        const tag = document.createElement("span")
        tag.textContent = competencia.nome
        tags.appendChild(tag)
    }

    secaoCompetencias.appendChild(tituloCompetencias)
    secaoCompetencias.appendChild(tags)

    const secaoVaga = document.createElement("div")
    secaoVaga.className = "candidate-section"

    const tituloVaga = document.createElement("strong")
    tituloVaga.textContent = "Vaga do match"

    const nomeVaga = document.createElement("p")
    nomeVaga.textContent = match.nome

    const descricaoVaga = document.createElement("p")
    descricaoVaga.textContent = match.descricao

    const tituloCompetenciasVaga = document.createElement("strong")
    tituloCompetenciasVaga.textContent = "Competências da vaga"

    const tagsVaga = document.createElement("div")
    tagsVaga.className = "job-tags"

    for (const competencia of match.competencias) {
        const tag = document.createElement("span")
        tag.textContent = competencia
        tagsVaga.appendChild(tag)
    }

    secaoVaga.appendChild(tituloVaga)
    secaoVaga.appendChild(nomeVaga)
    secaoVaga.appendChild(descricaoVaga)
    secaoVaga.appendChild(tituloCompetenciasVaga)
    secaoVaga.appendChild(tagsVaga)

    article.appendChild(topo)
    article.appendChild(descricao)
    article.appendChild(email)
    article.appendChild(secaoCompetencias)
    article.appendChild(secaoVaga)

    return article
}
