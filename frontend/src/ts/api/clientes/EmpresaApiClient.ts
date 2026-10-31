import { EmpresaViewDTO } from "../dto/empresa/EmpresaViewDto"
import { CadastrarEmpresaRequestDTO } from "../dto/empresa/CadastrarEmpresaRequestDTO"

export class EmpresaApiClient {
    private readonly baseUrl: string = "http://localhost:8080/empresas"

    async cadastrar(dados: CadastrarEmpresaRequestDTO): Promise<void> {
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
        `Erro ao cadastrar empresa: HTTP ${resposta.status}`

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

    async buscarPorId(id: number): Promise<EmpresaViewDTO> {
        const resposta = await fetch(`${this.baseUrl}/${id}`)

        if (resposta.status === 404) {
            throw new Error(`Empresa de ID ${id} não encontrada.`)
        }

        if (!resposta.ok) {
            throw new Error(`Erro ao consultar empresa: HTTP ${resposta.status}`)
        }

        return await resposta.json() as EmpresaViewDTO
    }
}