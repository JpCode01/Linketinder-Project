import { LoginException } from "../../exceptions/LoginException";
import { LoginStrategy } from "./LoginStrategy"

export class LoginCandidatoStrategy implements LoginStrategy {

    async login(email: string, senha: string): Promise<void> {

        const resposta = await fetch(
             "http://localhost:8080/login/candidato",
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

        const candidato = await resposta.json()
        
        localStorage.setItem(
            "sessao",
            JSON.stringify({
                id: candidato.id,
                tipo: "CANDIDATO"
            })
        )

        window.location.href = "./candidato.html"
    }
}