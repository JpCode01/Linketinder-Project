import { LoginEmpresaStrategy } from "../../strategy/login/LoginEmpresaStrategy"
import { configurarLogin } from "./FormularioLogin"

export function loginEmpresa(): void {
    configurarLogin("login-empresa", new LoginEmpresaStrategy())
}
