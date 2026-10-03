import { Candidato } from "../models/Candidato"
import { CandidatoRepository } from "../repository/CandidatoRepository"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CpfJaCadastradoException } from "../exceptions/CpfJaCadastradoException"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"

export class CandidatoService {

    constructor(
        private candidatoRepository: CandidatoRepository,
        private empresaRepository: EmpresaRepository
    ) {}

    salvar(candidato: Candidato): void {

        const candidatoPorEmail =
            this.candidatoRepository.buscarPorEmail(candidato.email)

        const empresaPorEmail =
            this.empresaRepository.buscarPorEmail(candidato.email)

        const candidatoPorCpf =
            this.candidatoRepository.buscarPorCpf(candidato.cpf)

        if ((candidatoPorEmail != undefined) || (empresaPorEmail != undefined)) {
            throw new EmailJaCadastradoException()
        }

        if (candidatoPorCpf != undefined) {
            throw new CpfJaCadastradoException()
        }

        this.candidatoRepository.salvar(candidato)
    }

    buscarTodos(): Candidato[] {
        return this.candidatoRepository.buscarTodos()
    }

    buscarPorId(id: number): Candidato {
        const candidatoPorId = this.candidatoRepository.buscarPorId(id)
        if (candidatoPorId == undefined) {
            throw new CandidatoNaoEncontradoException(id)
        }
        return candidatoPorId
    }

    buscarPorEmail(email: string): Candidato {
        const candidatoPorEmail = this.candidatoRepository.buscarPorEmail(email)
        if (candidatoPorEmail == undefined) {
            throw new CandidatoNaoEncontradoException(email)
        }
        return candidatoPorEmail
    }
}