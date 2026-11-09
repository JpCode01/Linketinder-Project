import { ApiClient } from "./ApiClient"
import { CandidatoAnonimoResponseDTO } from "../dto/candidato/CandidatoAnonimoResponseDTO"
import { VagaAnonimaResponseDTO } from "../dto/vaga/VagaAnonimaResponseDTO"

export class CurtidaVagaApiClient {
    constructor(private api = new ApiClient()) {}

    buscarCandidatosQueCurtiram(idVaga: number): Promise<CandidatoAnonimoResponseDTO[]> {
        return this.api.obter(`/curtidas/candidatos/vaga/${idVaga}`)
    }

    buscarVagasCurtidas(idCandidato: number): Promise<VagaAnonimaResponseDTO[]> {
        return this.api.obter(`/curtidas/vagas/candidato/${idCandidato}`)
    }

    curtirVaga(idVaga: number): Promise<void> {
        return this.api.postar("/curtidas/vagas", { idVaga })
    }
}
