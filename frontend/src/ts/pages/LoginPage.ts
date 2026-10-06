import { loginCandidato } from "./login/LoginCandidato"
import { loginEmpresa } from "./login/LoginEmpresa"

export function exibirLogin(): void {
    loginCandidato()
    loginEmpresa()
    abrirCadastro()
}

function abrirCadastro(): void {

    document.getElementById("cadastrar-candidato")!.onclick = (): void => {
        window.location.href = "./cadastro-candidato.html"
    }

    document.getElementById("cadastrar-empresa")!.onclick = (): void => {
        window.location.href = "./cadastro-empresa.html"
    }
}