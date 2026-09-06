import { Candidato } from "../models/Candidato";
import { Empresa, IEmpresaJSON } from "../models/Empresa"
import { Vaga } from "../models/Vaga";
import { ICandidatoJSON } from "../models/Candidato";


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

        const empresaJson: IEmpresaJSON[] = JSON.parse(empreasSalvas)

        const empresaEncontrada = empresaJson.find(
            empresa => empresa._nome === nomeProcurado
        )

        if (empresaEncontrada == null) {
            return undefined
        }

        return new Empresa(
            empresaEncontrada._cnpj,
            empresaEncontrada._pais,
            empresaEncontrada._nome,
            empresaEncontrada._email,
            empresaEncontrada._estado,
            empresaEncontrada._cep,
            empresaEncontrada._descricao

        )
    }

    buscarEmpresaLogada(): Empresa | undefined {
        const empresaSalva = localStorage.getItem("empresaLogada")
        if (empresaSalva == null) {
            return undefined
        }

        const empresaJson: IEmpresaJSON =
        JSON.parse(empresaSalva)

        return new Empresa(
            empresaJson._cnpj,
            empresaJson._pais,
            empresaJson._nome,
            empresaJson._email,
            empresaJson._estado,
            empresaJson._cep,
            empresaJson._descricao

        )
    }


}