import { CandidatoService } from "../../services/CandidatoService"
import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

export function loginCandidato(): void {

    document.getElementById("login-candidato")!.onclick = (): void => {

        const inputEmail =
            document.getElementById("email") as HTMLInputElement

        limparErroEmail(inputEmail)

        try {

            const candidato =
                candidatoService.buscarPorEmail(inputEmail.value)

            adicionarNaSessao(candidato.id, "CANDIDATO")

            window.location.href = "./candidato.html"

        } catch {

            mostrarErroEmail(
                inputEmail,
                "Candidato não encontrado."
            )
        }
    }
}

function adicionarNaSessao(id: number, tipo: string): void {
    localStorage.setItem(
        "sessao",
        JSON.stringify({
            id: id,
            tipo: tipo
        })
    )
}

function limparErroEmail(inputEmail: HTMLInputElement): void {
    inputEmail.classList.remove("input-error")
    document.getElementById("erro-email")?.remove()
}

function mostrarErroEmail(
    inputEmail: HTMLInputElement,
    mensagem: string
): void {
    inputEmail.classList.add("input-error")

    const mensagemErro = document.createElement("span")
    mensagemErro.id = "erro-email"
    mensagemErro.classList.add("error-message")
    mensagemErro.textContent = mensagem

    inputEmail.parentElement!.appendChild(mensagemErro)
}