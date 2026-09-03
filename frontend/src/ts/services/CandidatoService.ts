import { Candidato } from "../models/Candidato"
import { Competencia } from "../models/Competencia";
import { Vaga } from "../models/Vaga"

export class CandidatoService {
    cadastrar(candidato: Candidato): void {
        const candidatosSalvos = localStorage.getItem("candidatos")
        

        // Pega os candidatos do LocalStorage e cadastra novamente

        const candidatos: Candidato[] =
            candidatosSalvos ? JSON.parse(candidatosSalvos)
            : []

        candidatos.push(candidato)

        localStorage.setItem(
            "candidatos",
            JSON.stringify(candidatos)
        )

    }

    curtirVaga(candidato: Candidato, vaga: Vaga): void {
        if (vaga != null && candidato != null) {
            candidato.addVaga(vaga)
        } else {
            throw "Candidato ou Vaga não podem ser nulos"
        }
        
    } 

    adicionarCompetencia(candidato: Candidato, competencia: Competencia): void {
         if (competencia != null && candidato != null) {
            candidato.addCompetencia(competencia)
        } else {
            throw "Candidato ou competência não podem ser nulos"
        }
    }
}