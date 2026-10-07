import { EmpresaService } from "../../services/EmpresaService"
import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"
import { LoginEmpresaStrategy } from "../../strategy/login/LoginEmpresaStrategy"

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const empresaService: EmpresaService =
    serviceConfig.criarEmpresaService()

const loginStrategy: LoginEmpresaStrategy =
    new LoginEmpresaStrategy(empresaService)

export function loginEmpresa(): void {

    document.getElementById("login-empresa")!.onclick = (): void => {

        const inputEmail =
            document.getElementById("email") as HTMLInputElement

        limparErroEmail(inputEmail)

        try {

            loginStrategy.login(inputEmail.value)

        } catch {

            mostrarErroEmail(
                inputEmail,
                "Empresa não encontrada."
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