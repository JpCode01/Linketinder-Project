import { LoginCandidatoStrategy } from "../../strategy/login/LoginCandidatoStrategy"
import { configurarLogin } from "./FormularioLogin"

export function loginCandidato(): void {
    configurarLogin("login-candidato", new LoginCandidatoStrategy())
}
