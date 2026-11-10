import { ApiError } from "../../api/clientes/ApiClient"
import { SessaoResponseDTO } from "../../api/dto/login/SessaoResponseDTO"

export function obterSessaoDoPerfil(tipo: SessaoResponseDTO["tipo"]): SessaoResponseDTO | undefined {
    try {
        const sessao: unknown = JSON.parse(localStorage.getItem("sessao") ?? "null")
        if (typeof sessao === "object" && sessao !== null && "id" in sessao && "tipo" in sessao &&
            typeof sessao.id === "number" && Number.isSafeInteger(sessao.id) && sessao.id > 0 && sessao.tipo === tipo) {
            return { id: sessao.id, tipo }
        }
    } catch {
    }
    voltarAoLogin()
    return undefined
}

export function tratarErroSessao(erro: unknown): boolean {
    if (erro instanceof ApiError && (erro.status === 401 || erro.status === 403)) {
        voltarAoLogin()
        return true
    }
    return false
}

function voltarAoLogin(): void {
    localStorage.removeItem("sessao")
    window.location.href = "./index.html"
}
