import { CurtidaVagaApiClient } from "../api/clientes/CurtidaVagaApiClient";
import { CandidatoAnonimoResponseDTO } from "../api/dto/candidato/CandidatoAnonimoResponseDTO";
import { VagaAnonimaResponseDTO } from "../api/dto/vaga/VagaAnonimaResponseDTO";

export class CurtidaVagaService {

   constructor(
    private curtidaVagaApiClient: CurtidaVagaApiClient
    ) {}

    async buscarCandidatosQueCurtiram(
        idVaga: number
    ): Promise<CandidatoAnonimoResponseDTO[]> {
        return await this.curtidaVagaApiClient.buscarCandidatosQueCurtiram(idVaga)
    }

    async buscarVagasCurtidas(
        idCandidato: number
    ): Promise<VagaAnonimaResponseDTO[]> {
        return await this.curtidaVagaApiClient.buscarVagasCurtidas(
            idCandidato
        )
    }

    async curtirViaApi(
        idCandidato: number,
        idVaga: number
    ): Promise<void> {
        await this.curtidaVagaApiClient.curtirVaga(
            idCandidato,
            idVaga
        )
    }
    
    async jaCurtiuVaga(
        idCandidato: number,
        idVaga: number
    ): Promise<boolean> {
        return await this.curtidaVagaApiClient.jaCurtiuVaga(
            idCandidato,
            idVaga
        )
    }
}