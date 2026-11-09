import { MatchResponseDTO } from "../../api/dto/match/MatchResponseDTO"
import { MatchService } from "../../services/MatchService"
import { tratarErroSessao } from "../login/SessaoNavegacao"

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
    if (contador != null) contador.textContent = "Carregando matches..."

    try {
        const matches = await matchService.buscarPorCandidato(idCandidato)

        if (matches.length === 0) {
            const vazio = document.createElement("p")
            vazio.textContent = "Você ainda não tem matches."
            lista.appendChild(vazio)
        }

        for (const match of matches) {
            lista.appendChild(criarCardMatchCandidato(match))
        }

        if (contador != null) {
            contador.textContent = `${matches.length} ${matches.length === 1 ? "match" : "matches"}`
        }
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
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

    adicionarCampo(article, "ID do match", String(match.id))
    adicionarCampo(article, "CNPJ", match.cnpjEmpresa)
    adicionarCampo(article, "País da empresa", match.paisEmpresa)
    adicionarCampo(article, "Estado da empresa", match.estadoEmpresa)

    if (match.nomeRecrutador != null) {
        adicionarCampo(article, "Recrutador", match.nomeRecrutador)
    }
    if (match.areaRecrutamento != null) {
        adicionarCampo(article, "Área de recrutamento", match.areaRecrutamento)
    }
    article.appendChild(secaoVaga)

    const candidato = document.createElement("div")
    candidato.className = "candidate-section"
    const tituloCandidato = document.createElement("strong")
    tituloCandidato.textContent = "Candidato do match"
    candidato.appendChild(tituloCandidato)
    adicionarCampo(candidato, "Nome", `${match.nomeCandidato} ${match.sobrenomeCandidato}`)
    adicionarCampo(candidato, "E-mail", match.emailCandidato)
    adicionarCampo(candidato, "CPF", match.cpfCandidato)
    adicionarCampo(candidato, "Descrição", match.descricaoCandidato)
    adicionarCampo(candidato, "Idade", String(match.idadeCandidato))
    adicionarCampo(candidato, "País", match.paisCandidato)
    adicionarCampo(candidato, "Estado", match.estadoCandidato)
    adicionarCampo(candidato, "Competências", match.competenciasCandidato.map(competencia => competencia.nome).join(", "))
    article.appendChild(candidato)

    return article
}

function adicionarCampo(container: HTMLElement, rotulo: string, valor: string): void {
    const campo = document.createElement("p")
    campo.textContent = `${rotulo}: ${valor}`
    container.appendChild(campo)
}
