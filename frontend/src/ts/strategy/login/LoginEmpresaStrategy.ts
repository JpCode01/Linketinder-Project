import { LoginException } from "../../exceptions/LoginException"
import { ApiError } from "../../api/clientes/ApiClient"
import { LoginApiClient } from "../../api/clientes/LoginApiClient"
import { LoginStrategy } from "./LoginStrategy"

export class LoginEmpresaStrategy implements LoginStrategy {
    constructor(private loginApiClient = new LoginApiClient()) {}

    async login(email: string, senha: string): Promise<void> {
        try {
            const sessao = await this.loginApiClient.loginEmpresa(email, senha)
            localStorage.setItem("sessao", JSON.stringify(sessao))
            window.location.href = "./empresa.html"
        } catch (erro: unknown) {
            if (erro instanceof ApiError && erro.status === 401) throw new LoginException()
            throw erro
        }
    }
}
