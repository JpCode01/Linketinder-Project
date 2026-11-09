import { ApiClient } from "./ApiClient"
import { CadastrarRecrutadorRequestDTO } from "../dto/recrutador/CadastrarRecrutadorRequestDTO"
import { RecrutadorViewDTO } from "../dto/recrutador/RecrutadorViewDTO"
import { AreaRecrutamentoResponseDTO } from "../dto/recrutador/AreaRecrutamentoResponseDTO"

export class RecrutadorApiClient {
    constructor(private api = new ApiClient()) {}

    cadastrarPelaEmpresa(idEmpresa: number, dados: CadastrarRecrutadorRequestDTO): Promise<void> {
        return this.api.postar(`/empresas/${idEmpresa}/recrutadores`, dados)
    }

    buscarPerfilProprio(idRecrutador: number): Promise<RecrutadorViewDTO> {
        return this.api.obter(`/recrutadores/${idRecrutador}`)
    }

    buscarAreas(): Promise<AreaRecrutamentoResponseDTO[]> {
        return this.api.obter("/areas-recrutamento")
    }
}
