import { Empresa } from "../../models/Empresa"
import { EmpresaConsultaRepository } from "./contrato/EmpresaConsultaRepository"
import { CadastroRepository } from "../CadastroRepository";
import { StorageFactory } from "../../factory/storage/StorageFactory"

export class EmpresaRepository implements
            EmpresaConsultaRepository,
            CadastroRepository<Empresa>
            {

    private readonly storage: Storage

    constructor(storageFactory: StorageFactory) {
        this.storage = storageFactory.criarStorage()
    }

    salvar(empresa: Empresa): void {
        const empresasSalvas = this.storage.getItem("empresas")

        const empresas: Empresa[] =
            empresasSalvas
                ? JSON.parse(empresasSalvas)
                : []

        empresa.id = empresas.length > 0
        ? Math.max(...empresas.map(empresa => empresa.id)) + 1
        : 1

        empresas.push(empresa)

        this.storage.setItem(
            "empresas",
            JSON.stringify(empresas)
        )
    }

    buscarTodos(): Empresa[] {
        const empresasSalvas = this.storage.getItem("empresas")

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