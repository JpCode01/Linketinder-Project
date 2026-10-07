import { EmpresaService } from "../../services/EmpresaService"
import { LoginStrategy } from "./LoginStrategy"

export class LoginEmpresaStrategy implements LoginStrategy {

    constructor(
        private empresaService: EmpresaService
    ) {}

    login(email: string): void {

        const empresa =
            this.empresaService.buscarPorEmail(email)

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