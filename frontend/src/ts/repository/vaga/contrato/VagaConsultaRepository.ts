import { Vaga } from "../../../models/Vaga"

export interface VagaConsultaRepository {

    buscarTodos(): Vaga[]

    buscarPorId(id: number): Vaga | undefined

    buscarPorEmpresa(idEmpresa: number): Vaga[]

}