import { Candidato } from "../models/Candidato";

export class CandidatoRepository {

    salvar(candidato: Candidato): void {
        const candidatosSalvos = localStorage.getItem("candidatos")

        const candidatos: Candidato[] =
            candidatosSalvos
                ? JSON.parse(candidatosSalvos)
                : []

        candidatos.push(candidato)

        localStorage.setItem(
            "candidatos",
            JSON.stringify(candidatos)
        )
    }

    buscarTodos(): Candidato[] {
        const candidatosSalvos = localStorage.getItem("candidatos")

        if (candidatosSalvos == null) {
            return []
        }

        return JSON.parse(candidatosSalvos)
    }

    buscarPorId(id: number): Candidato | undefined {
        const candidatos = this.buscarTodos()

        return candidatos.find(
            candidato => candidato.id === id
        )
    }

    buscarPorEmail(email: string): Candidato | undefined {
        const candidatos = this.buscarTodos()

        return candidatos.find(
            candidato => candidato.email === email
        )
    }

    buscarPorCpf(cpf: string): Candidato | undefined {
        const candidatos = this.buscarTodos()

        return candidatos.find(
            candidato => candidato.cpf === cpf
        )
    }
}