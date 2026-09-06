import { Candidato } from "../models/Candidato"
import { Competencia } from "../models/Competencia";
import { Vaga } from "../models/Vaga"
import { ICandidatoJSON } from "../models/Candidato"

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

    buscarCandidato(nomeProcurado: string): Candidato | undefined {


        const candidatosSalvos = localStorage.getItem("candidatos")

        if (candidatosSalvos == null) {
            return undefined
        }

        const candidatosJson: ICandidatoJSON[] = JSON.parse(candidatosSalvos)

        const candidatoEncontrado = candidatosJson.find(
        candidato => candidato._nome === nomeProcurado
        )

        if (candidatoEncontrado == null) {
            return undefined
        }

        return new Candidato(
            candidatoEncontrado._cpf,
            candidatoEncontrado._idade,
            candidatoEncontrado._formacao,
            candidatoEncontrado._nome,
            candidatoEncontrado._email,
            candidatoEncontrado._estado,
            candidatoEncontrado._cep,
            candidatoEncontrado._descricao
        )
    }

    buscarCandidatoLogado(): Candidato | undefined {
        const candidatoSalvo = localStorage.getItem("candidatoLogado")

        if (candidatoSalvo == null) {
            return undefined
        }

        const candidatoJson: ICandidatoJSON =
            JSON.parse(candidatoSalvo)

        return new Candidato(
            candidatoJson._cpf,
            candidatoJson._idade,
            candidatoJson._formacao,
            candidatoJson._nome,
            candidatoJson._email,
            candidatoJson._estado,
            candidatoJson._cep,
            candidatoJson._descricao
        )
    }
}