import { CurtidaCandidato } from "../models/CurtidaCandidato"
import { CurtidaCandidatoRepository } from "../repository/CurtidaCandidatoRepository"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { CandidatoRepository } from "../repository/CandidatoRepository"
import { EmpresaNaoEncontradaException } from "../exceptions/EmpresaNaoEncontradaException"
import { CandidatoNaoEncontradoException } from "../exceptions/CandidatoNaoEncontradoException"
import { CandidatoJaCurtidoException } from "../exceptions/CandidatoJaCurtidoException"

export class CurtidaCandidatoService {

    constructor(
        private curtidaCandidatoRepository: CurtidaCandidatoRepository,
        private empresaRepository: EmpresaRepository,
        private candidatoRepository: CandidatoRepository
    ) {}

    curtir(idEmpresa: number, idCandidato: number): void {
       const empresa = this.empresaRepository.buscarPorId(idEmpresa)

        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(idEmpresa)
        }

        const candidato = this.candidatoRepository.buscarPorId(idCandidato)

        if (candidato == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        const curtidaExistente =
            this.curtidaCandidatoRepository.buscar(
                idEmpresa,
                idCandidato
            )

        if (curtidaExistente != undefined) {
            throw new CandidatoJaCurtidoException(idCandidato)
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