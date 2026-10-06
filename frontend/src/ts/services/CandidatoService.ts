import { Candidato } from "../models/Candidato"
import { CadastroRepository } from "../repository/CadastroRepository"
import { CandidatoConsultaRepository } from "../repository/candidato/contrato/CandidatoConsultaRepository"
import { EmpresaConsultaRepository } from "../repository/empresa/contrato/EmpresaConsultaRepository"
import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CpfJaCadastradoException } from "../exceptions/CpfJaCadastradoException"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"

export class CandidatoService {


    constructor(
        private candidatoCadastroRepository: CadastroRepository<Candidato>,
        private candidatoConsultaRepository: CandidatoConsultaRepository,
        private empresaConsultaRepository: EmpresaConsultaRepository
    ) {}

    salvar(candidato: Candidato): void {

        const candidatoPorEmail =
            this.candidatoConsultaRepository.buscarPorEmail(candidato.email)

        const empresaPorEmail =
            this.empresaConsultaRepository.buscarPorEmail(candidato.email)

        const candidatoPorCpf =
            this.candidatoConsultaRepository.buscarPorCpf(candidato.cpf)

        if ((candidatoPorEmail != undefined) || (empresaPorEmail != undefined)) {
            throw new EmailJaCadastradoException()
        }

        if (candidatoPorCpf != undefined) {
            throw new CpfJaCadastradoException()
        }

        this.candidatoCadastroRepository.salvar(candidato)
    }

    buscarTodos(): Candidato[] {
        return this.candidatoConsultaRepository.buscarTodos()
    }

    buscarPorId(id: number): Candidato {
        const candidatoPorId = this.candidatoConsultaRepository.buscarPorId(id)
        if (candidatoPorId == undefined) {
            throw new CandidatoNaoEncontradoException(id)
        }
        return candidatoPorId
    }

    buscarPorEmail(email: string): Candidato {
        const candidatoPorEmail = this.candidatoConsultaRepository.buscarPorEmail(email)
        if (candidatoPorEmail == undefined) {
            throw new CandidatoNaoEncontradoException(email)
        }
        return candidatoPorEmail
    }
}