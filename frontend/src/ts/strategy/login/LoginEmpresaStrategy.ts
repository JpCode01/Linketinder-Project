import { LoginException } from "../../exceptions/LoginException";
import { LoginStrategy } from "./LoginStrategy"

export class LoginEmpresaStrategy implements LoginStrategy {

    async login(email: string, senha: string): Promise<void> {

        const resposta = await fetch(
            "http://localhost:8080/login/empresa",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    senha: senha
                })
            }
        )

        if (resposta.status === 401) {
            throw new LoginException()
        }

        if (!resposta.ok) {
            throw new Error("ERRO_SERVIDOR")
        }


        const empresa = await resposta.json()

        localStorage.setItem(
            "sessao",
            JSON.stringify({
                id: empresa.id,
                tipo: "EMPRESA"
            })
        )

        window.location.href = "./empresa.html"
    }
}