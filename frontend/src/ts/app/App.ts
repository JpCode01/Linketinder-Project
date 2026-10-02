import { loginCandidato, loginEmpresa } from "../pages/LoginPage"
import { abrirCadastro } from "../pages/LoginPage"
import { cadastrarCandidato, cadastrarEmpresa } from "../pages/CadastroPage"
import { exibirPageCandidato } from "../pages/CandidatoPage"
import { exibirPageEmpresa, criarVaga } from "../pages/EmpresaPage"

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
            criarVaga()
        }
    }
}