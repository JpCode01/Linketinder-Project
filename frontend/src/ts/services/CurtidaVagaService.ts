import { CurtidaVaga } from "../models/CurtidaVaga"
import { CurtidaVagaRepository } from "../repository/CurtidaVagaRepository"
import { CandidatoRepository } from "../repository/CandidatoRepository"
import { VagaRepository } from "../repository/VagaRepository"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"
import { VagaNaoEncontradaException } from "../exceptions/vaga/VagaNaoEncontradaException"
import { VagaJaCurtidaException } from "../exceptions/vaga/VagaJaCurtidaException"

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
        const candidatoProcurado = this.candidatoRepository.buscarPorId(idCandidato)

        if (candidatoProcurado == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        return this.curtidaVagaRepository.buscarPorCandidato(idCandidato)
    }

    buscarPorVaga(idVaga: number): CurtidaVaga[] {
        const vagaPrcurada = this.vagaRepository.buscarPorId(idVaga)

        if (vagaPrcurada == undefined) {
            throw new VagaNaoEncontradaException(idVaga)
        }

        return this.curtidaVagaRepository.buscarPorVaga(idVaga)
    }
}