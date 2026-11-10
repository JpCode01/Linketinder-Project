import { VagaApiClient } from "../api/clientes/VagaApiClient"
import { CadastrarVagaRequestDTO } from "../api/dto/vaga/CadastrarVagaRequestDTO"
import { VagaAnonimaResponseDTO } from "../api/dto/vaga/VagaAnonimaResponseDTO"
import { VagaEmpresaResponseDTO } from "../api/dto/vaga/VagaEmpresaResponseDTO"

export class VagaService {
    constructor(private vagaApiClient: VagaApiClient) {}

    async criarVagaEmpresa(dados: { nome: string; descricao: string; localizacao: string; competencias: string[] }): Promise<void> {
        await this.vagaApiClient.cadastrarEmpresa({
            nome: dados.nome,
            descricao: dados.descricao,
            local: dados.localizacao,
            competencias: dados.competencias
        })
    }

    async criarVagaRecrutador(idRecrutador: number, dados: CadastrarVagaRequestDTO): Promise<void> {
        await this.vagaApiClient.cadastrarRecrutador(idRecrutador, dados)
    }

    async buscarPorEmpresa(idEmpresa: number): Promise<VagaEmpresaResponseDTO[]> {
        return await this.vagaApiClient.buscarPorEmpresa(idEmpresa)
    }

    async buscarPorRecrutador(idRecrutador: number): Promise<VagaEmpresaResponseDTO[]> {
        return await this.vagaApiClient.buscarPorRecrutador(idRecrutador)
    }

    async buscarTodasAsVagasAnonimas(): Promise<VagaAnonimaResponseDTO[]> {
        return await this.vagaApiClient.buscarTodasAsVagas()
    }
}
