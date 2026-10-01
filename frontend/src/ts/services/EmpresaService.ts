import { Empresa } from "../models/Empresa"
import { EmpresaRepository } from "../repository/EmpresaRepository"

export class EmpresaService {

    constructor(
        private empresaRepository: EmpresaRepository
    ) {}

    salvar(empresa: Empresa): void {
        this.empresaRepository.salvar(empresa)
    }

    buscarTodos(): Empresa[] {
        return this.empresaRepository.buscarTodos()
    }

    buscarPorId(id: number): Empresa | undefined {
        return this.empresaRepository.buscarPorId(id)
    }

    buscarPorEmail(email: string): Empresa | undefined {
        return this.empresaRepository.buscarPorEmail(email)
    }
}