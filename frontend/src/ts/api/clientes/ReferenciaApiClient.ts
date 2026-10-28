
import { Pais } from "../../models/Pais"
import { Estado } from "../../models/Estado"

export class ReferenciaApiClient {
    private readonly baseUrl: string =
        "http://localhost:8080/referencias"

    async buscarPaises(): Promise<Pais[]> {
        const resposta = await fetch(`${this.baseUrl}/paises`)

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar países: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as Pais[]
    }

    async buscarEstados(): Promise<Estado[]> {
        const resposta = await fetch(`${this.baseUrl}/estados`)

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar estados: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as Estado[]
    }
}
