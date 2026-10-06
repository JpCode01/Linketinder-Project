import { cadastrarCandidato } from "../pages/candidato/CadastroCandidatoPage"
import { cadastrarEmpresa } from "../pages/empresa/CadastroEmpresaPage"
import { exibirPageCandidato } from "../pages/candidato/CandidatoPage"
import { exibirPageEmpresa } from "../pages/empresa/EmpresaPage"
import { exibirLogin } from "../pages/LoginPage"

export class App {

    start(): void {

        const paginaAtual = window.location.pathname
        console.log("Página:", paginaAtual)

        if (paginaAtual.endsWith("/index.html")) {
            exibirLogin()
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