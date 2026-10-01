import { CurtidaVaga } from "../models/CurtidaVaga"
import { CurtidaVagaRepository } from "../repository/CurtidaVagaRepository"

export class CurtidaVagaService {

    constructor(
        private curtidaVagaRepository: CurtidaVagaRepository
    ) {}

    curtir(idCandidato: number, idVaga: number): void {
        const curtidaExistente =
            this.curtidaVagaRepository.buscar(
                idCandidato,
                idVaga
            )

        if (curtidaExistente !== undefined) {
            return
        }

        const curtida = new CurtidaVaga(
            idCandidato,
            idVaga
        )

        this.curtidaVagaRepository.salvar(curtida)
    }

    buscarPorCandidato(idCandidato: number): CurtidaVaga[] {
        return this.curtidaVagaRepository.buscarPorCandidato(
            idCandidato
        )
    }

    buscarPorVaga(idVaga: number): CurtidaVaga[] {
        return this.curtidaVagaRepository.buscarPorVaga(
            idVaga
        )
    }
}