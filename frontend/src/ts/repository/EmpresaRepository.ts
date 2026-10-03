import { Empresa } from "../models/Empresa"

export class EmpresaRepository {

    salvar(empresa: Empresa): void {
        const empresasSalvas = localStorage.getItem("empresas")

        const empresas: Empresa[] =
            empresasSalvas
                ? JSON.parse(empresasSalvas)
                : []

        empresa.id = empresas.length > 0
        ? Math.max(...empresas.map(empresa => empresa.id)) + 1
        : 1

        empresas.push(empresa)

        localStorage.setItem(
            "empresas",
            JSON.stringify(empresas)
        )
    }

    buscarTodos(): Empresa[] {
        const empresasSalvas = localStorage.getItem("empresas")

        if (empresasSalvas == null) {
            return []
        }

        return JSON.parse(empresasSalvas)
    }

    buscarPorId(id: number): Empresa | undefined {
        const empresas = this.buscarTodos()

        return empresas.find(
            empresa => empresa.id === id
        )
    }

    buscarPorEmail(email: string): Empresa | undefined {
        const empresas = this.buscarTodos()

        return empresas.find(
            empresa => empresa.email === email
        )
    }

    buscarPorCnpj(cnpj: string): Empresa | undefined {
        const empresas = this.buscarTodos()

        return empresas.find(
            empresa => empresa.cnpj === cnpj
        )
    }
}