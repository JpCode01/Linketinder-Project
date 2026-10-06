import { CurtidaVaga } from "../../../models/CurtidaVaga"

export interface CurtidaVagaConsultaRepository {

    buscarTodos(): CurtidaVaga[]

    buscarPorCandidato(idCandidato: number): CurtidaVaga[]

    buscarPorVaga(idVaga: number): CurtidaVaga[]

    buscar(
        idCandidato: number, 
        idVaga: number
    ): CurtidaVaga | undefined
}