import { CandidatoAnonimoResponseDTO } from "../dto/candidato/CandidatoAnonimoResponseDTO"

export class CurtidaVagaApiClient {

    private readonly baseUrl: string = "http://localhost:8080/curtidas"

    async buscarCandidatosQueCurtiram(
        idVaga: number
    ): Promise<CandidatoAnonimoResponseDTO[]> {
        const resposta = await fetch(
            `${this.baseUrl}/candidatos/vaga/${idVaga}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar candidatos da vaga ${idVaga}: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as CandidatoAnonimoResponseDTO[]
    }
}