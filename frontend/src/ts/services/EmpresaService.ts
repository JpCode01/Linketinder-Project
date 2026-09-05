import { Candidato } from "../models/Candidato";
import { Empresa } from "../models/Empresa"
import { Vaga } from "../models/Vaga";

export class EmpresaService {
    cadastrar(empresa: Empresa): void {
        const empresasSalvas = localStorage.getItem("empresas")
        

        // Pega as empresas do LocalStorage e cadastra novamente

        const empresas: Empresa[] =
            empresasSalvas ? JSON.parse(empresasSalvas)
            : []

        empresas.push(empresa)

        localStorage.setItem(
            "empresas",
            JSON.stringify(empresas)
        )
    }

    curtirCandidato(empresa: Empresa,candidato: Candidato) {
        if (empresa != null && candidato != null) {
            empresa.addCandidatoCurtido(candidato)
        } else {
            throw "Empresa ou candidato não podem ser nulos"
        }
    }

    adicionarVaga(empresa: Empresa, vaga: Vaga) {
        if (empresa != null && vaga != null) {
            empresa.addVaga(vaga)
        } else {
            throw "Empresa ou vaga não podem ser nulos"
        }
    }

    buscarEmpresa(nomeProcurado: string): Empresa | undefined {
        const empreasSalvas = localStorage.getItem("empresas")

        if (empreasSalvas == null) {
            return undefined
        }

        const empresas: Empresa[] = JSON.parse(empreasSalvas)

        empresas.forEach((empresa) =>
        {
            Object.setPrototypeOf(empresa, Empresa.prototype)
        })
   
        return empresas.find((n) => n.nome === nomeProcurado)
    }


}