import { MatchApiClient } from "../api/clientes/MatchApiClient"
import { MatchResponseDTO } from "../api/dto/match/MatchResponseDTO"

export class MatchService {

    constructor(
        private matchApiClient: MatchApiClient
    ) {}

    async buscarPorEmpresa(
        idEmpresa: number
    ): Promise<MatchResponseDTO[]> {
        return await this.matchApiClient.buscarPorEmpresa(idEmpresa)
    }

    async buscarPorCandidato(
        idCandidato: number
    ): Promise<MatchResponseDTO[]> {
        return await this.matchApiClient.buscarPorCandidato(idCandidato)
    }
}