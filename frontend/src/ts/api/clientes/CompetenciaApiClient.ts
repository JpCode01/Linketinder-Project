import { Competencia } from "../../models/Competencia"

export class CompetenciaApiClient {
    private readonly baseUrl: string = "http://localhost:8080/competencias"

    async buscarTodas(): Promise<Competencia[]> {
        const resposta = await fetch(this.baseUrl)

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar competências: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as Competencia[]
    }
}
