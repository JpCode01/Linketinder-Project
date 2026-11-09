export class ApiError extends Error {
    constructor(public readonly status: number, mensagem: string) {
        super(mensagem)
        this.name = "ApiError"
    }
}

export class ApiClient {
    private readonly baseUrl = "http://127.0.0.1:8080"

    async obter<T>(caminho: string): Promise<T> {
        const resposta = await this.enviar(caminho, { method: "GET" })
        try {
            return await resposta.json() as T
        } catch {
            throw new Error("Resposta JSON inválida do servidor.")
        }
    }

    async postar(caminho: string, dados: unknown): Promise<void> {
        // Cadastros e curtidas podem retornar 201/204 sem corpo.
        await this.enviar(caminho, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        })
    }

    async postarComResposta<T>(caminho: string, dados: unknown): Promise<T> {
        const resposta = await this.enviar(caminho, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dados)
        })
        try {
            return await resposta.json() as T
        } catch {
            throw new Error("Resposta JSON inválida do servidor.")
        }
    }

    private async enviar(caminho: string, opcoes: RequestInit): Promise<Response> {
        const resposta = await fetch(this.baseUrl + caminho, {
            ...opcoes,
            credentials: "include"
        })
        if (!resposta.ok) {
            let mensagem = `Erro na requisição: HTTP ${resposta.status}`
            try {
                const corpo: unknown = await resposta.json()
                if (typeof corpo === "object" && corpo !== null && "erro" in corpo &&
                    typeof corpo.erro === "string" && corpo.erro.trim() !== "") {
                    mensagem = corpo.erro
                }
            } catch {
                // HTML, corpo vazio ou JSON malformado: preservar status e erro genérico.
            }
            throw new ApiError(resposta.status, mensagem)
        }
        return resposta
    }
}
