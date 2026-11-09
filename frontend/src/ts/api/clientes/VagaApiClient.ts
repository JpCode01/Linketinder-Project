import { ApiClient } from "./ApiClient"
import { CadastrarVagaRequestDTO } from "../dto/vaga/CadastrarVagaRequestDTO"
import { VagaAnonimaResponseDTO } from "../dto/vaga/VagaAnonimaResponseDTO"
import { VagaEmpresaResponseDTO } from "../dto/vaga/VagaEmpresaResponseDTO"

export class VagaApiClient {
    constructor(private api = new ApiClient()) {}

    cadastrarEmpresa(dados: CadastrarVagaRequestDTO): Promise<void> {
        return this.api.postar("/vagas", dados)
    }

    cadastrarRecrutador(idRecrutador: number, dados: CadastrarVagaRequestDTO): Promise<void> {
        return this.api.postar(`/recrutadores/${idRecrutador}/vagas`, dados)
    }

    buscarPorEmpresa(idEmpresa: number): Promise<VagaEmpresaResponseDTO[]> {
        return this.api.obter(`/vagas/empresa/${idEmpresa}`)
    }

    buscarPorRecrutador(idRecrutador: number): Promise<VagaEmpresaResponseDTO[]> {
        return this.api.obter(`/vagas/recrutador/${idRecrutador}`)
    }

    buscarTodasAsVagas(): Promise<VagaAnonimaResponseDTO[]> {
        return this.api.obter("/vagas")
    }
}
