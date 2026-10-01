import { Vaga } from "../models/Vaga"
import { VagaRepository } from "../repository/VagaRepository"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { EmpresaNaoEncontradaException } from "../exceptions/EmpresaNaoEncontradaException"

export class VagaService {

    constructor(
        private vagaRepository: VagaRepository,
        private empresaRepository: EmpresaRepository
    ) {}

    salvar(vaga: Vaga): void {
        const empresa = this.empresaRepository.buscarPorId(vaga.idEmpresa)

        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(vaga.idEmpresa)
        }

        this.vagaRepository.salvar(vaga)
    }

    buscarTodos(): Vaga[] {
        return this.vagaRepository.buscarTodos()
    }

    buscarPorId(id: number): Vaga | undefined {
        return this.vagaRepository.buscarPorId(id)
    }

    buscarPorEmpresa(idEmpresa: number): Vaga[] {
        return this.vagaRepository.buscarPorEmpresa(idEmpresa)
    }
}