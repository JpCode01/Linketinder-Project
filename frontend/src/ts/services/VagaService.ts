import { Candidato } from "../models/Candidato";
import { Vaga } from "../models/Vaga"
import { Competencia } from "../models/Competencia"

export class VagaService {
    cadastrar(vaga: Vaga): void {
        const vagasSalvas = localStorage.getItem("vagas")
        

        // Pega as vagas do LocalStorage e cadastra novamente

        const vagas: Vaga[] =
            vagasSalvas ? JSON.parse(vagasSalvas)
            : []

        vagas.push(vaga)

        localStorage.setItem(
            "vagas",
            JSON.stringify(vagas)
        )

    }

    adicionarCandidato(vaga: Vaga, candidato: Candidato) {
        if (vaga != null && candidato != null) {
            vaga.addCandidatoQueCurtiu(candidato)
        } else {
            throw "Candidato ou Vaga não podem ser nulos"
        }
    }

        adicionarCompetencia(vaga: Vaga, competencia: Competencia): void {
         if (vaga != null && competencia != null) {
            vaga.addCompetencia(competencia)
        } else {
            throw "Vaga ou competência não podem ser nulos"
        }
    }
}