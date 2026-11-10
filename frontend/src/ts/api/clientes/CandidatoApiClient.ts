import { ApiClient } from "./ApiClient"
import { CadastrarCandidatoRequestDTO } from "../dto/candidato/CadastrarCandidatoRequestDTO"
import { CandidatoViewDTO } from "../dto/candidato/CandidatoViewDTO"

export class CandidatoApiClient {
    constructor(private api = new ApiClient()) {}

    cadastrar(dados: CadastrarCandidatoRequestDTO): Promise<void> {
        return this.api.postar("/candidatos", dados)
    }

    buscarPorId(id: number): Promise<CandidatoViewDTO> {
        return this.api.obter(`/candidatos/${id}`)
    }
}
