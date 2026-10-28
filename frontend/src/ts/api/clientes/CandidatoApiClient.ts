import { CandidatoViewDTO } from "../dto/candidato/CandidatoViewDTO"

export class CandidatoApiClient {
    private readonly baseUrl: string = "http://localhost:8080/candidatos"

    async buscarPorId(id: number): Promise<CandidatoViewDTO> {
        const resposta = await fetch(`${this.baseUrl}/${id}`)

        if (resposta.status === 404) {
            throw new Error(`Candidato de ID ${id} não encontrado.`)
        }

        if (!resposta.ok) {
            throw new Error(
                `Erro ao consultar candidato: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as CandidatoViewDTO
    }
}
