import { VagaApiClient } from "../api/clientes/VagaApiClient";
import { VagaEmpresaResponseDTO } from "../api/dto/vaga/VagaEmpresaResponseDTO";
import { VagaNaoEncontradaException } from "../exceptions/vaga/VagaNaoEncontradaException";
import { Vaga } from "../models/Vaga";
import { VagaConsultaRepository } from "../repository/vaga/contrato/VagaConsultaRepository";

export class VagaService {

    constructor(
        private vagaConsultaRepository: VagaConsultaRepository,
        private vagaApiClient: VagaApiClient
    ) {}

    async salvar(vaga: Vaga): Promise<void> {
        await this.vagaApiClient.cadastrar({
            nome: vaga.nome,
            descricao: vaga.descricao,
            local: vaga.localizacao,
            idEmpresa: vaga.idEmpresa,
            competencias: vaga.competencias.map(
                (competencia) => competencia.nome
            )
        })
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

    async buscarPorEmpresa(
        idEmpresa: number
    ): Promise<VagaEmpresaResponseDTO[]> {
        return await this.vagaApiClient.buscarPorEmpresa(idEmpresa)
    }
}