import { Vaga } from "../models/Vaga"
import { VagaRepository } from "../repository/VagaRepository"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { EmpresaNaoEncontradaException } from "../exceptions/empresa/EmpresaNaoEncontradaException"
import { VagaNaoEncontradaException } from "../exceptions/vaga/VagaNaoEncontradaException";

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

    buscarPorId(id: number): Vaga {
        const procurarVaga = this.vagaRepository.buscarPorId(id)

        if (procurarVaga == undefined) {
            throw new VagaNaoEncontradaException(id)
        }

        return procurarVaga
    }

    buscarPorEmpresa(idEmpresa: number): Vaga[] {
        const empresaProcurada = this.empresaRepository.buscarPorId(idEmpresa)

        if (empresaProcurada == undefined) {
            throw new EmpresaNaoEncontradaException(idEmpresa)
        }
        
        return this.vagaRepository.buscarPorEmpresa(idEmpresa)
    }
}