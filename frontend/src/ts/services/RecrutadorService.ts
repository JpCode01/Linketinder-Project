import { RecrutadorApiClient } from "../api/clientes/RecrutadorApiClient"
import { CadastrarRecrutadorRequestDTO } from "../api/dto/recrutador/CadastrarRecrutadorRequestDTO"
import { RecrutadorViewDTO } from "../api/dto/recrutador/RecrutadorViewDTO"
import { AreaRecrutamentoResponseDTO } from "../api/dto/recrutador/AreaRecrutamentoResponseDTO"

export class RecrutadorService {
    constructor(private recrutadorApiClient: RecrutadorApiClient) {}

    async cadastrarPelaEmpresa(idEmpresa: number, dados: CadastrarRecrutadorRequestDTO): Promise<void> {
        await this.recrutadorApiClient.cadastrarPelaEmpresa(idEmpresa, dados)
    }

    async buscarPerfilProprio(idRecrutador: number): Promise<RecrutadorViewDTO> {
        return await this.recrutadorApiClient.buscarPerfilProprio(idRecrutador)
    }

    async buscarAreas(): Promise<AreaRecrutamentoResponseDTO[]> {
        return await this.recrutadorApiClient.buscarAreas()
    }
}
