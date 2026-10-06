import { Vaga } from "../models/Vaga"
import { CadastroRepository } from "../repository/CadastroRepository"
import { VagaConsultaRepository } from "../repository/vaga/contrato/VagaConsultaRepository"
import { EmpresaConsultaRepository } from "../repository/empresa/contrato/EmpresaConsultaRepository"
import { EmpresaNaoEncontradaException } from "../exceptions/empresa/EmpresaNaoEncontradaException"
import { VagaNaoEncontradaException } from "../exceptions/vaga/VagaNaoEncontradaException"

export class VagaService {

    constructor(
        private vagaCadastroRepository: CadastroRepository<Vaga>,
        private vagaConsultaRepository: VagaConsultaRepository,
        private empresaConsultaRepository: EmpresaConsultaRepository
    ) {}

    salvar(vaga: Vaga): void {
        const empresa = this.empresaConsultaRepository.buscarPorId(vaga.idEmpresa)

        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(vaga.idEmpresa)
        }

        this.vagaCadastroRepository.salvar(vaga)
    }

    buscarTodos(): Vaga[] {
        return this.vagaConsultaRepository.buscarTodos()
    }

    buscarPorId(id: number): Vaga {
        const procurarVaga = this.vagaConsultaRepository.buscarPorId(id)

        if (procurarVaga == undefined) {
            throw new VagaNaoEncontradaException(id)
        }

        return procurarVaga
    }

    buscarPorEmpresa(idEmpresa: number): Vaga[] {
        const empresaProcurada = this.empresaConsultaRepository.buscarPorId(idEmpresa)

        if (empresaProcurada == undefined) {
            throw new EmpresaNaoEncontradaException(idEmpresa)
        }
        
        return this.vagaConsultaRepository.buscarPorEmpresa(idEmpresa)
    }
}