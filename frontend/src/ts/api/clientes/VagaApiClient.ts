import { CadastrarVagaRequestDTO } from "../dto/vaga/CadastrarVagaRequestDTO"
import { VagaEmpresaResponseDTO } from "../dto/vaga/VagaEmpresaResponseDTO";

export class VagaApiClient {

    private readonly baseUrl: string = "http://localhost:8080/vagas"

    async cadastrar(dados: CadastrarVagaRequestDTO): Promise<void> {
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

    private async obterMensagemErro(
        resposta: Response
    ): Promise<string> {
        const mensagemPadrao: string =
            `Erro ao cadastrar vaga: HTTP ${resposta.status}`

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

    async buscarPorEmpresa(
        idEmpresa: number
    ): Promise<VagaEmpresaResponseDTO[]> {
        const resposta = await fetch(
            `${this.baseUrl}/empresa/${idEmpresa}`
        )

        if (!resposta.ok) {
            throw new Error(
                `Erro ao carregar vagas da empresa: HTTP ${resposta.status}`
            )
        }

        return await resposta.json() as VagaEmpresaResponseDTO[]
}
}
