import { VagaApiClient } from "../api/clientes/VagaApiClient";
import { VagaAnonimaResponseDTO } from "../api/dto/vaga/VagaAnonimaResponseDTO";
import { VagaEmpresaResponseDTO } from "../api/dto/vaga/VagaEmpresaResponseDTO";
import { Vaga } from "../models/Vaga";

export class VagaService {

    constructor(
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

    async buscarPorEmpresa(
            idEmpresa: number
    ): Promise<VagaEmpresaResponseDTO[]> {
        return await this.vagaApiClient.buscarPorEmpresa(idEmpresa)
    }

    async buscarTodasAsVagasAnonimas(): Promise<VagaAnonimaResponseDTO[]> {
        return await this.vagaApiClient.buscarTodasAsVagas()
    }
}