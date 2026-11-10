import { ApiClient } from "./ApiClient"
import { MatchResponseDTO } from "../dto/match/MatchResponseDTO"

export class MatchApiClient {
    constructor(private api = new ApiClient()) {}

    buscarPorEmpresa(idEmpresa: number): Promise<MatchResponseDTO[]> {
        return this.api.obter(`/matches/empresa/${idEmpresa}`)
    }

    buscarPorCandidato(idCandidato: number): Promise<MatchResponseDTO[]> {
        return this.api.obter(`/matches/candidato/${idCandidato}`)
    }

    buscarPorRecrutador(idRecrutador: number): Promise<MatchResponseDTO[]> {
        return this.api.obter(`/matches/recrutador/${idRecrutador}`)
    }
}
