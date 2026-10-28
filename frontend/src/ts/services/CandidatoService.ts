import { Candidato } from "../models/Candidato"
import { CadastroRepository } from "../repository/CadastroRepository"
import { CandidatoConsultaRepository } from "../repository/candidato/contrato/CandidatoConsultaRepository"
import { EmpresaConsultaRepository } from "../repository/empresa/contrato/EmpresaConsultaRepository"
import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CpfJaCadastradoException } from "../exceptions/CpfJaCadastradoException"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"
import { CadastrarCandidatoRequestDTO } from "../api/dto/candidato/CadastrarCandidatoRequestDTO";
import { CandidatoApiClient } from "../api/clientes/CandidatoApiClient";

export class CandidatoService {

    constructor(
        private candidatoApiClient: CandidatoApiClient,
        private candidatoConsultaRepository: CandidatoConsultaRepository
    ) {}

    async cadastrar(dados: CadastrarCandidatoRequestDTO): Promise<void> {
        await this.candidatoApiClient.cadastrar(dados)
    }

    buscarTodos(): Candidato[] {
        return this.candidatoConsultaRepository.buscarTodos()
    }

    buscarPorId(id: number): Candidato {
        const candidatoPorId =
            this.candidatoConsultaRepository.buscarPorId(id)

        if (candidatoPorId == undefined) {
            throw new CandidatoNaoEncontradoException(id)
        }

        return candidatoPorId
    }

    buscarPorEmail(email: string): Candidato {
        const candidatoPorEmail =
            this.candidatoConsultaRepository.buscarPorEmail(email)

        if (candidatoPorEmail == undefined) {
            throw new CandidatoNaoEncontradoException(email)
        }

        return candidatoPorEmail
    }
}