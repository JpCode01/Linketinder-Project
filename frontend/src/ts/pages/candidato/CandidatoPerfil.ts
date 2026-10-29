import { CandidatoViewDTO } from "../../api/dto/candidato/CandidatoViewDTO"

export function exibirDadosCandidato(
    candidato: CandidatoViewDTO
): void {
    const iniciais = obterIniciais(candidato.nome)

    const nomeCompleto = 
    `${candidato.nome} ${candidato.sobrenome}`

    document.getElementById("profile-avatar")!.textContent =
        iniciais

    document.getElementById("user-avatar")!.textContent =
        iniciais

    document.getElementById("profile-name")!.textContent =
        nomeCompleto

    document.getElementById("user-name")!.textContent =
        nomeCompleto

    document.getElementById("profile-description")!.textContent =
        candidato.descricao

    exibirCompetencias(candidato)
}

export function obterIniciais(nome: string): string {
    const partesNome = nome.trim().split(" ")

    if (partesNome.length === 1) {
        return partesNome[0].charAt(0).toUpperCase()
    }

    return (
        partesNome[0].charAt(0) +
        partesNome[partesNome.length - 1].charAt(0)
    ).toUpperCase()
}

function exibirCompetencias(
    candidato: CandidatoViewDTO
): void {
    const listaCompetencias =
        document.getElementById("profile-skills")

    if (listaCompetencias == null) {
        return
    }

    listaCompetencias.innerHTML = ""

    for (const competencia of candidato.competencias) {
        const competenciaElement =
            document.createElement("span")

        competenciaElement.classList.add("skill")
        competenciaElement.textContent = competencia.nome

        listaCompetencias.appendChild(
            competenciaElement
        )
    }
}