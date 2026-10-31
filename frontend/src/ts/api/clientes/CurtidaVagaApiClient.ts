import { CandidatoAnonimoResponseDTO } from "../dto/candidato/CandidatoAnonimoResponseDTO"
import { VagaAnonimaResponseDTO } from "../dto/vaga/VagaAnonimaResponseDTO";

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

    async buscarVagasCurtidas(
        idCandidato: number
    ): Promise<VagaAnonimaResponseDTO[]> {
        const resposta = await fetch(
            `${this.baseUrl}/vagas/candidato/${idCandidato}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar vagas curtidas: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as VagaAnonimaResponseDTO[]
    }

    async curtirVaga(
        idCandidato: number,
        idVaga: number
    ): Promise<void> {
        const resposta = await fetch(`${this.baseUrl}/vagas`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                idCandidato,
                idVaga
            })
        })

        if (!resposta.ok) {
            throw new Error(
                `Erro ao curtir vaga: HTTP ${resposta.status}`
            )
        }
    }

    
    async jaCurtiuVaga(
        idCandidato: number,
        idVaga: number
    ): Promise<boolean> {
        const parametros: URLSearchParams = new URLSearchParams({
            candidatoId: idCandidato.toString(),
            vagaId: idVaga.toString()
        })

        const resposta = await fetch(
            `${this.baseUrl}/vagas/verificar?${parametros.toString()}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao verificar curtida da vaga: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as boolean
    }
}