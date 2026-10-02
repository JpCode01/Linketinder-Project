import { CurtidaVaga } from "../models/CurtidaVaga"
import { CurtidaVagaRepository } from "../repository/CurtidaVagaRepository"
import { CandidatoRepository } from "../repository/CandidatoRepository"
import { VagaRepository } from "../repository/VagaRepository"
import { CandidatoNaoEncontradoException } from "../exceptions/CandidatoNaoEncontradoException"
import { VagaNaoEncontradaException } from "../exceptions/VagaNaoEncontradaException"
import { VagaJaCurtidaException } from "../exceptions/VagaJaCurtidaException"

export class CurtidaVagaService {

    constructor(
        private curtidaVagaRepository: CurtidaVagaRepository,
        private candidatoRepository: CandidatoRepository,
        private vagaRepository: VagaRepository
    ) {}

    curtir(idCandidato: number, idVaga: number): void {
        const candidato = this.candidatoRepository.buscarPorId(idCandidato)

        if (candidato == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        const vaga = this.vagaRepository.buscarPorId(idVaga)

        if (vaga == undefined) {
            throw new VagaNaoEncontradaException(idVaga)
        }

        const curtidaExistente =
            this.curtidaVagaRepository.buscar(idCandidato, idVaga)

        if (curtidaExistente != undefined) {
            throw new VagaJaCurtidaException(idVaga)
        }

        const curtida = new CurtidaVaga(idCandidato, idVaga)

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