import { CurtidaVaga } from "../models/CurtidaVaga"

export class CurtidaVagaRepository {

    salvar(curtida: CurtidaVaga): void {
        const curtidasSalvas = localStorage.getItem("curtidasVagas")

        const curtidas: CurtidaVaga[] =
            curtidasSalvas
                ? JSON.parse(curtidasSalvas)
                : []

        curtidas.push(curtida)

        localStorage.setItem(
            "curtidasVagas",
            JSON.stringify(curtidas)
        )
    }

    buscarTodos(): CurtidaVaga[] {
        const curtidasSalvas = localStorage.getItem("curtidasVagas")

        if (curtidasSalvas == null) {
            return []
        }

        return JSON.parse(curtidasSalvas)
    }

    buscarPorCandidato(idCandidato: number): CurtidaVaga[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idCandidato === idCandidato
        )
    }

    buscarPorVaga(idVaga: number): CurtidaVaga[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idVaga === idVaga
        )
    }

    buscar(idCandidato: number, idVaga: number): CurtidaVaga | undefined {
        const curtidas = this.buscarTodos()

        return curtidas.find(
            curtida =>
                curtida.idCandidato === idCandidato &&
                curtida.idVaga === idVaga
        )
    }
}