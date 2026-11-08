
export class CurtidaCandidatoApiClient {

    private readonly baseUrl: string = "http://localhost:8080/curtidas"

    async curtir(
        idEmpresa: number,
        idCandidato: number,
        idVaga: number
    ): Promise<void> {
        const resposta = await fetch(`${this.baseUrl}/candidatos`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                idEmpresa,
                idCandidato,
                idVaga
            })
        })

        if (!resposta.ok) {
            const mensagemPadrao: string =
                `Erro ao curtir candidato: HTTP ${resposta.status}`

            if (!resposta.headers.get("Content-Type")?.includes("application/json")) {
                throw new Error(mensagemPadrao)
            }

            try {
                const corpo = await resposta.json() as { erro?: string; mensagem?: string }

                const mensagem: string | undefined = corpo.erro ?? corpo.mensagem

                if (mensagem != null && mensagem.trim() !== "") {
                    throw new Error(mensagem)
                }
            } catch (erro: unknown) {
                if (erro instanceof Error && erro.message !== mensagemPadrao) {
                    throw erro
                }
            }

            throw new Error(mensagemPadrao)
        }
    }

    async jaCurtiuCandidato(
        idEmpresa: number,
        idCandidato: number
    ): Promise<boolean> {
        const parametros: URLSearchParams = new URLSearchParams({
            empresaId: idEmpresa.toString(),
            candidatoId: idCandidato.toString()
        })

        const resposta = await fetch(
            `${this.baseUrl}/candidatos/verificar?${parametros.toString()}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao verificar curtida do candidato: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as boolean
    }
}
