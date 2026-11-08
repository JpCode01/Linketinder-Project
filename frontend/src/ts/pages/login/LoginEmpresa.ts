import { LoginException } from "../../exceptions/LoginException";
import { LoginEmpresaStrategy } from "../../strategy/login/LoginEmpresaStrategy"
import { LoginStrategy } from "../../strategy/login/LoginStrategy";

const loginStrategy: LoginStrategy =
    new LoginEmpresaStrategy()

export function loginEmpresa(): void {

    document.getElementById("login-empresa")!.onclick = async (): Promise<void> => {

        const inputEmail =
            document.getElementById("email") as HTMLInputElement

        const inputSenha = 
            document.getElementById("senha") as HTMLInputElement
        
        limparErroLogin()

        try {

            await loginStrategy.login(
                inputEmail.value,
                inputSenha.value
            )

        } catch(erro) {
            if (erro instanceof TypeError) {
                mostrarErroLogin(
                    "Não foi possível conectar ao servidor. Tente novamente."
                )
                return
            }

            if (erro instanceof LoginException) {
                mostrarErroLogin(erro.message)
                return
            }
        }
    }
}

function limparErroLogin(): void {
    document.getElementById("erro-login")?.remove()
}

function mostrarErroLogin(mensagem: string): void {
    limparErroLogin()

    const mensagemErro = document.createElement("p")
    mensagemErro.id = "erro-login"
    mensagemErro.classList.add("error-message")
    mensagemErro.textContent = mensagem

    const formulario = document.querySelector("form")

    formulario!.appendChild(mensagemErro)
}