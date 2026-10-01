import { CurtidaCandidato } from "../models/CurtidaCandidato"
import { CurtidaCandidatoRepository } from "../repository/CurtidaCandidatoRepository"

export class CurtidaCandidatoService {

    constructor(
        private curtidaCandidatoRepository: CurtidaCandidatoRepository
    ) {}

    curtir(idEmpresa: number, idCandidato: number): void {
        const curtidaExistente =
            this.curtidaCandidatoRepository.buscar(
                idEmpresa,
                idCandidato
            )

        if (curtidaExistente !== undefined) {
            return
        }

        const curtida = new CurtidaCandidato(
            idEmpresa,
            idCandidato
        )

        this.curtidaCandidatoRepository.salvar(curtida)
    }

    buscarPorEmpresa(idEmpresa: number): CurtidaCandidato[] {
        return this.curtidaCandidatoRepository.buscarPorEmpresa(
            idEmpresa
        )
    }

    buscarPorCandidato(idCandidato: number): CurtidaCandidato[] {
        return this.curtidaCandidatoRepository.buscarPorCandidato(
            idCandidato
        )
    }
}