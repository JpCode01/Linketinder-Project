import { Empresa } from "../../../models/Empresa"

export interface EmpresaConsultaRepository {
    buscarTodos(): Empresa[]
    buscarPorEmail(email: string): Empresa | undefined
    buscarPorCnpj(cnpj: string): Empresa | undefined
    buscarPorId(id: number): Empresa | undefined
}