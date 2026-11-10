import { LoginStrategy } from "../../strategy/login/LoginStrategy"

export function configurarLogin(idBotao: string, estrategia: LoginStrategy): void {
    const botao = document.getElementById(idBotao) as HTMLButtonElement
    const formulario = document.querySelector("form") as HTMLFormElement
    const email = document.getElementById("email") as HTMLInputElement
    const senha = document.getElementById("senha") as HTMLInputElement
    const botoes = document.querySelectorAll<HTMLButtonElement>('[id^="login-"]')

    formulario.onsubmit = (evento: SubmitEvent): void => evento.preventDefault()
    botao.onclick = async (): Promise<void> => {
        if (botao.disabled || !formulario.reportValidity()) return
        document.getElementById("erro-login")?.remove()
        document.getElementById("login-status")?.remove()
        botoes.forEach(item => item.disabled = true)
        localStorage.removeItem("sessao")
        try {
            await estrategia.login(email.value, senha.value)
        } catch (erro: unknown) {
            const mensagem = document.createElement("p")
            mensagem.id = "erro-login"
            mensagem.setAttribute("role", "alert")
            mensagem.classList.add("error-message")
            mensagem.textContent = erro instanceof TypeError
                ? "Não foi possível conectar ao servidor. Tente novamente."
                : erro instanceof Error ? erro.message : "Não foi possível entrar."
            formulario.appendChild(mensagem)
        } finally {
            senha.value = ""
            botoes.forEach(item => item.disabled = false)
        }
    }
}
