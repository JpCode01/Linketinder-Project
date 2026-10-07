import { CandidatoService } from "../../services/CandidatoService"
import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"
import { LoginCandidatoStrategy } from "../../strategy/login/LoginCandidatoStrategy"

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

const loginStrategy: LoginCandidatoStrategy =
    new LoginCandidatoStrategy(candidatoService)

export function loginCandidato(): void {

    document.getElementById("login-candidato")!.onclick = (): void => {

        const inputEmail =
            document.getElementById("email") as HTMLInputElement

        limparErroEmail(inputEmail)

        try {

            loginStrategy.login(inputEmail.value)

        } catch {

            mostrarErroEmail(
                inputEmail,
                "Candidato não encontrado."
            )
        }
    }
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