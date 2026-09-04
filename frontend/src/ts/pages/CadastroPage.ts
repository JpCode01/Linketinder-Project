import { Candidato } from "../models/Candidato"
import { Empresa } from "../models/Empresa"

import { CandidatoService } from "../services/CandidatoService"
import { EmpresaService } from "../services/EmpresaService"

var candidatoService: CandidatoService = new CandidatoService
var empresaService: EmpresaService = new EmpresaService

export function cadastrarCandidato() {
    document.getElementById("register-candidate")!.onclick = (): void => {
        const nome: string = (document.getElementById("name") as HTMLInputElement).value
        const email: string = (document.getElementById("email") as HTMLInputElement).value 
        const cpf: string = (document.getElementById("cpf") as HTMLInputElement).value
        const idade: number = parseInt((document.getElementById("age") as HTMLInputElement).value)
        const formacao: string = (document.getElementById("education") as HTMLInputElement).value
        // const competencia: Competencia =
        // Competencia[
        //     (document.getElementById("skills") as HTMLInputElement)
        //     .value
        //     .toUpperCase() as keyof typeof Competencia
        // ];
        const estado: string = (document.getElementById("state") as HTMLInputElement).value
        const cep: string = (document.getElementById("cep") as HTMLInputElement).value 
        const description: string = (document.getElementById("description") as HTMLInputElement).value
        
        let candidato: Candidato = new Candidato(cpf, idade, formacao, nome, email, estado, cep, description)
        candidatoService.cadastrar(candidato)
        window.location.href = "./candidato.html"
    }
}

export function cadastrarEmpresa() {
    document.getElementById("register-company")!.onclick = (): void => {
            const nome: string = (document.getElementById("name") as HTMLInputElement).value
            const email: string = (document.getElementById("email") as HTMLInputElement).value
            const cnpj: string = (document.getElementById("cnpj") as HTMLInputElement).value
            const pais: string = (document.getElementById("country") as HTMLInputElement).value 
            const estado: string = (document.getElementById("state") as HTMLInputElement).value
            const cep: string = (document.getElementById("cep") as HTMLInputElement).value 
            const description: string = (document.getElementById("description") as HTMLInputElement).value
            
            let empresa: Empresa = new Empresa(cnpj, pais, nome, email, estado, cep, description)
            empresaService.cadastrar(empresa)
            window.location.href = "./empresa.html"
    }
}

