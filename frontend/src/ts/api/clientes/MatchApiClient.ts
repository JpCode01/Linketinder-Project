import { MatchResponseDTO } from "../dto/match/MatchResponseDTO"

export class MatchApiClient {

    private readonly baseUrl: string = "http://localhost:8080/matches"

    async buscarPorEmpresa(
        idEmpresa: number
    ): Promise<MatchResponseDTO[]> {
        const resposta = await fetch(
            `${this.baseUrl}/empresa/${idEmpresa}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar matches da empresa: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as MatchResponseDTO[]
    }

    async buscarPorCandidato(
        idCandidato: number
    ): Promise<MatchResponseDTO[]> {
        const resposta = await fetch(
            `${this.baseUrl}/candidato/${idCandidato}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar matches do candidato: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as MatchResponseDTO[]
    }
}