import { CurtidaVaga } from "../models/CurtidaVaga"
import { CadastroRepository } from "../repository/CadastroRepository"
import { CurtidaVagaConsultaRepository } from "../repository/candidato/contrato/CurtidaVagaConsultaRepository"
import { CandidatoConsultaRepository } from "../repository/candidato/contrato/CandidatoConsultaRepository"
import { VagaConsultaRepository } from "../repository/vaga/contrato/VagaConsultaRepository"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"
import { VagaNaoEncontradaException } from "../exceptions/vaga/VagaNaoEncontradaException"
import { VagaJaCurtidaException } from "../exceptions/vaga/VagaJaCurtidaException"

export class CurtidaVagaService {

    constructor(
        private curtidaVagaCadastroRepository: CadastroRepository<CurtidaVaga>,
        private curtidaVagaConsultaRepository: CurtidaVagaConsultaRepository,
        private candidatoConsultaRepository: CandidatoConsultaRepository,
        private vagaConsultaRepository: VagaConsultaRepository
    ) {}

    curtir(idCandidato: number, idVaga: number): void {
        const candidato = this.candidatoConsultaRepository.buscarPorId(idCandidato)

        if (candidato == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        const vaga = this.vagaConsultaRepository.buscarPorId(idVaga)

        if (vaga == undefined) {
            throw new VagaNaoEncontradaException(idVaga)
        }

        const curtidaExistente =
            this.curtidaVagaConsultaRepository.buscar(idCandidato, idVaga)

        if (curtidaExistente != undefined) {
            throw new VagaJaCurtidaException(idVaga)
        }

        const curtida = new CurtidaVaga(idCandidato, idVaga)

        this.curtidaVagaCadastroRepository.salvar(curtida)
    }

    buscarPorCandidato(idCandidato: number): CurtidaVaga[] {
        const candidatoProcurado = this.candidatoConsultaRepository.buscarPorId(idCandidato)

        if (candidatoProcurado == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        return this.curtidaVagaConsultaRepository.buscarPorCandidato(idCandidato)
    }

    buscarPorVaga(idVaga: number): CurtidaVaga[] {
        const vagaPrcurada = this.vagaConsultaRepository.buscarPorId(idVaga)

        if (vagaPrcurada == undefined) {
            throw new VagaNaoEncontradaException(idVaga)
        }

        return this.curtidaVagaConsultaRepository.buscarPorVaga(idVaga)
    }
}