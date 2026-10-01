import { Vaga } from "../models/Vaga"

export class VagaRepository {

    salvar(vaga: Vaga): void {
        const vagasSalvas = localStorage.getItem("vagas")

        const vagas: Vaga[] =
            vagasSalvas
                ? JSON.parse(vagasSalvas)
                : []

        vagas.push(vaga)

        localStorage.setItem(
            "vagas",
            JSON.stringify(vagas)
        )
    }

    buscarTodos(): Vaga[] {
        const vagasSalvas = localStorage.getItem("vagas")

        if (vagasSalvas == null) {
            return []
        }

        return JSON.parse(vagasSalvas)
    }

    buscarPorId(id: number): Vaga | undefined {
        const vagas = this.buscarTodos()

        return vagas.find(
            vaga => vaga.id === id
        )
    }

    buscarPorEmpresa(idEmpresa: number): Vaga[] {
        const vagas = this.buscarTodos()

        return vagas.filter(
            vaga => vaga.idEmpresa === idEmpresa
        )
    }
}   