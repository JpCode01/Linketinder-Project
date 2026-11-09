import { LoginRecrutadorStrategy } from "../../strategy/login/LoginRecrutadorStrategy"
import { configurarLogin } from "./FormularioLogin"

export function loginRecrutador(): void {
    configurarLogin("login-recrutador", new LoginRecrutadorStrategy())
}
