import { Empresa } from "../models/Empresa"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { CandidatoRepository } from "../repository/CandidatoRepository"
import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CnpjJaCadastradoException } from "../exceptions/CnpjJaCadastradoException"
import { EmpresaNaoEncontradaException } from "../exceptions/empresa/EmpresaNaoEncontradaException";

export class EmpresaService {

    constructor(
        private empresaRepository: EmpresaRepository,
        private candidatoRepository: CandidatoRepository
    ) {}

    salvar(empresa: Empresa): void {
        const empresaPorEmail =
            this.empresaRepository.buscarPorEmail(empresa.email)

        const candidatoPorEmail =
            this.candidatoRepository.buscarPorEmail(empresa.email)

        const empresaPorCnpj =
            this.empresaRepository.buscarPorCnpj(empresa.cnpj)

        if ((empresaPorEmail != undefined) || (candidatoPorEmail != undefined)) {
            throw new EmailJaCadastradoException()
        }

        if (empresaPorCnpj != undefined) {
            throw new CnpjJaCadastradoException()
        }

        this.empresaRepository.salvar(empresa)
    }

    buscarTodos(): Empresa[] {
        return this.empresaRepository.buscarTodos()
    }

    buscarPorId(id: number): Empresa {
        const empresa = this.empresaRepository.buscarPorId(id)

        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(id)
        }

        return empresa
    }

    buscarPorEmail(email: string): Empresa {
        const empresa = this.empresaRepository.buscarPorEmail(email)
    
        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(email)
        }

        return empresa
    }
}