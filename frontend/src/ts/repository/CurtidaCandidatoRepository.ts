import { CurtidaCandidato } from "../models/CurtidaCandidato"

export class CurtidaCandidatoRepository {

    salvar(curtida: CurtidaCandidato): void {
        const curtidasSalvas = localStorage.getItem("curtidasCandidatos")

        const curtidas: CurtidaCandidato[] =
            curtidasSalvas
                ? JSON.parse(curtidasSalvas)
                : []

        curtidas.push(curtida)

        localStorage.setItem(
            "curtidasCandidatos",
            JSON.stringify(curtidas)
        )
    }

    buscarTodos(): CurtidaCandidato[] {
        const curtidasSalvas = localStorage.getItem("curtidasCandidatos")

        if (curtidasSalvas == null) {
            return []
        }

        return JSON.parse(curtidasSalvas)
    }

    buscarPorEmpresa(idEmpresa: number): CurtidaCandidato[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idEmpresa === idEmpresa
        )
    }

    buscarPorCandidato(idCandidato: number): CurtidaCandidato[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idCandidato === idCandidato
        )
    }

    buscar(idEmpresa: number, idCandidato: number, idVaga: number): CurtidaCandidato | undefined {
        const curtidas = this.buscarTodos()

        return curtidas.find(
            curtida =>
                curtida.idEmpresa === idEmpresa &&
                curtida.idCandidato === idCandidato &&
                curtida.idVaga === idVaga
        )
    }
}