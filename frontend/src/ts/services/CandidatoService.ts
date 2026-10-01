import { Candidato } from "../models/Candidato"
import { CandidatoRepository } from "../repository/CandidatoRepository"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CpfJaCadastradoException } from "../exceptions/CpfJaCadastradoException"

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

    buscarPorId(id: number): Candidato | undefined {
        return this.candidatoRepository.buscarPorId(id)
    }

    buscarPorEmail(email: string): Candidato | undefined {
        return this.candidatoRepository.buscarPorEmail(email)
    }
}