import { CadastrarCandidatoRequestDTO } from "../dto/candidato/CadastrarCandidatoRequestDTO";
import { CandidatoViewDTO } from "../dto/candidato/CandidatoViewDTO"

export class CandidatoApiClient {
    private readonly baseUrl: string = "http://localhost:8080/candidatos"

    async cadastrar(dados: CadastrarCandidatoRequestDTO): Promise<void> {
        const resposta = await fetch(this.baseUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        })

        if (!resposta.ok) {
            const mensagem: string =
                await this.obterMensagemErro(resposta)

            throw new Error(mensagem)
        }
    }

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

    private async obterMensagemErro(
        resposta: Response
    ): Promise<string> {
        const mensagemPadrao: string =
            `Erro ao cadastrar candidato: HTTP ${resposta.status}`

        if (!resposta.headers.get("Content-Type")?.includes("application/json")) {
            return mensagemPadrao
        }

        try {
            const corpo = await resposta.json() as { erro?: string }

            if (corpo.erro != null && corpo.erro.trim() !== "") {
                return corpo.erro
            }
        } catch (erro: unknown) {
            if (!(erro instanceof SyntaxError)) {
                throw erro
            }
        }

        return mensagemPadrao
    }
}
