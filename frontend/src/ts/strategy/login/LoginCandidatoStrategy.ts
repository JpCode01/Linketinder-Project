import { CandidatoService } from "../../services/CandidatoService"
import { LoginStrategy } from "./LoginStrategy"

export class LoginCandidatoStrategy implements LoginStrategy {

    constructor(
        private candidatoService: CandidatoService
    ) {}

    login(email: string): void {

        const candidato =
            this.candidatoService.buscarPorEmail(email)

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