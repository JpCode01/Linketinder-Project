import { Candidato } from "../models/Candidato"
import { Empresa } from "../models/Empresa"

import { CandidatoService } from "../services/CandidatoService"
import { EmpresaService } from "../services/EmpresaService"

// import { candidatos, empresas } from "../data/DadosIniciais"

var candidatoService: CandidatoService = new CandidatoService
var empresaService: EmpresaService = new EmpresaService


export function procurar():void {

    document.getElementById("login-candidato")!.onclick = (): void => {
        const inputNome = document.getElementById("nome") as HTMLInputElement

        inputNome.classList.remove("input-error")
        document.getElementById("erro-nome")?.remove()

        const candidatoEncontrado: Candidato | undefined = candidatoService.buscarCandidato(inputNome.value)

        if (candidatoEncontrado !== undefined) {

            window.location.href = "./candidato.html"

        } else {

            inputNome.classList.add("input-error")

            const mensagemErro = document.createElement("span")

            mensagemErro.id = "erro-nome"
            mensagemErro.classList.add("error-message")
            mensagemErro.textContent = "Candidato não encontrado."

            inputNome.parentElement!.appendChild(mensagemErro)
        }
    };

    document.getElementById("login-empresa")!.onclick = (): void => {

        const inputNome = document.getElementById("nome") as HTMLInputElement

        inputNome.classList.remove("input-error")
        document.getElementById("erro-nome")?.remove()

        const empresaEncontrada = empresaService.buscarEmpresa(inputNome.value)

        if (empresaEncontrada !== undefined) {

            window.location.href = "./empresa.html"

        } else {

            inputNome.classList.add("input-error")

            const mensagemErro = document.createElement("span")

            mensagemErro.id = "erro-nome"
            mensagemErro.classList.add("error-message")
            mensagemErro.textContent = "Empresa não encontrada."

            inputNome.parentElement!.appendChild(mensagemErro)
        }
    };
   
    // Versão com Lista, sem Local Storage
    // document.getElementById("login-candidato")!.onclick = (): void => {

    //     const inputNome = document.getElementById("nome") as HTMLInputElement

    //     inputNome.classList.remove("input-error")
    //     document.getElementById("erro-nome")?.remove()

    //     const candidatoEncontrado: Candidato | undefined =
    //         candidatos.find((n) => n.nome === inputNome.value)

    //     if (candidatoEncontrado !== undefined) {

    //         window.location.href = "./candidato.html"

    //     } else {

    //         inputNome.classList.add("input-error")

    //         const mensagemErro = document.createElement("span")

    //         mensagemErro.id = "erro-nome"
    //         mensagemErro.classList.add("error-message")
    //         mensagemErro.textContent = "Candidato não encontrado."

    //         inputNome.parentElement!.appendChild(mensagemErro)
    //     }
    // };
    
    

}

export function abrirCadastro(): void {

    document.getElementById("cadastrar-candidato")!.onclick = (): void => {
        window.location.href = "./cadastro-candidato.html"
    }

    document.getElementById("cadastrar-empresa")!.onclick = (): void => {
        window.location.href = "./cadastro-empresa.html"
    }
}