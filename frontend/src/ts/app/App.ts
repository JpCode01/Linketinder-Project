import { loginCandidato, loginEmpresa } from "../pages/LoginPage"
import { abrirCadastro } from "../pages/LoginPage"
import { cadastrarCandidato } from "../pages/CadastroCandidatoPage"
import { cadastrarEmpresa } from "../pages/CadastroEmpresaPage"
import { exibirPageCandidato } from "../pages/CandidatoPage"
import { exibirPageEmpresa } from "../pages/EmpresaPage"

export class App {

    start(): void {

        const paginaAtual = window.location.pathname
        console.log("Página:", paginaAtual)

        if (paginaAtual.endsWith("/index.html")) {
            loginCandidato()
            loginEmpresa()
            abrirCadastro()
        }

        if (paginaAtual.endsWith("/cadastro-candidato.html")) {
            cadastrarCandidato()
        }

        if (paginaAtual.endsWith("/cadastro-empresa.html")) {
            cadastrarEmpresa()
        }

        if (paginaAtual.endsWith("/candidato.html")) {
            exibirPageCandidato()
        }

        if (paginaAtual.endsWith("/empresa.html")) {
            exibirPageEmpresa()
        }
    }
}