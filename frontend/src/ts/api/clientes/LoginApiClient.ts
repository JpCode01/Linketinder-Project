import { ApiClient } from "./ApiClient"
import { SessaoResponseDTO } from "../dto/login/SessaoResponseDTO"

export class LoginApiClient {
    constructor(private api = new ApiClient()) {}

    loginCandidato(email: string, senha: string): Promise<SessaoResponseDTO> {
        return this.login("candidato", email, senha)
    }

    loginEmpresa(email: string, senha: string): Promise<SessaoResponseDTO> {
        return this.login("empresa", email, senha)
    }

    loginRecrutador(email: string, senha: string): Promise<SessaoResponseDTO> {
        return this.login("recrutador", email, senha)
    }

    private async login(tipo: SessaoResponseDTO["tipo"], email: string, senha: string): Promise<SessaoResponseDTO> {
        const sessao = await this.api.postarComResposta<SessaoResponseDTO>(`/login/${tipo}`, { email, senha })
        if (!Number.isSafeInteger(sessao?.id) || sessao.id <= 0 || sessao.tipo !== tipo) {
            throw new Error("Resposta de login inválida.")
        }
        return { id: sessao.id, tipo: sessao.tipo }
    }
}
