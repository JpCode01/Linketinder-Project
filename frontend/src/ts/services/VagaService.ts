import { Vaga } from "../models/Vaga"
import { VagaRepository } from "../repository/VagaRepository"

export class VagaService {

    constructor(
        private vagaRepository: VagaRepository
    ) {}

    salvar(vaga: Vaga): void {
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