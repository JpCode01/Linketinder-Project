import { Empresa } from "../models/Empresa"
import { CadastroRepository } from "../repository/CadastroRepository"
import { EmpresaConsultaRepository } from "../repository/empresa/contrato/EmpresaConsultaRepository"
import { CandidatoConsultaRepository } from "../repository/candidato/contrato/CandidatoConsultaRepository"
import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CnpjJaCadastradoException } from "../exceptions/CnpjJaCadastradoException"
import { EmpresaNaoEncontradaException } from "../exceptions/empresa/EmpresaNaoEncontradaException"

export class EmpresaService {

    constructor(
        private empresaCadastroRepository: CadastroRepository<Empresa>,
        private empresaConsultaRepository: EmpresaConsultaRepository,
        private candidatoConsultaRepository: CandidatoConsultaRepository
    ) {}

    salvar(empresa: Empresa): void {
        const empresaPorEmail =
            this.empresaConsultaRepository.buscarPorEmail(empresa.email)

        const candidatoPorEmail =
            this.candidatoConsultaRepository.buscarPorEmail(empresa.email)

        const empresaPorCnpj =
            this.empresaConsultaRepository.buscarPorCnpj(empresa.cnpj)

        if ((empresaPorEmail != undefined) || (candidatoPorEmail != undefined)) {
            throw new EmailJaCadastradoException()
        }

        if (empresaPorCnpj != undefined) {
            throw new CnpjJaCadastradoException()
        }

        this.empresaCadastroRepository.salvar(empresa)
    }

    buscarTodos(): Empresa[] {
        return this.empresaConsultaRepository.buscarTodos()
    }

    buscarPorId(id: number): Empresa {
        const empresa = this.empresaConsultaRepository.buscarPorId(id)

        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(id)
        }

        return empresa
    }

    buscarPorEmail(email: string): Empresa {
        const empresa = this.empresaConsultaRepository.buscarPorEmail(email)
    
        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(email)
        }

        return empresa
    }
}