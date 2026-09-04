import { procurar, abrirCadastro } from "../pages/LoginPage"
import { cadastrarCandidato, cadastrarEmpresa } from "../pages/CadastroPage"

export class App {

    start(): void {

        const paginaAtual = window.location.pathname

        if (paginaAtual.includes("index.html")) {
            procurar()
            abrirCadastro()
        }

        if (paginaAtual.includes("cadastro-candidato.html")) {
            cadastrarCandidato()
        }

        if (paginaAtual.includes("cadastro-empresa.html")) {
            cadastrarEmpresa()
        }
    }
}