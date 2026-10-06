import { CurtidaCandidato } from "../../../models/CurtidaCandidato"

export interface CurtidaCandidatoConsultaRepository {

    buscarTodos(): CurtidaCandidato[]

    buscarPorEmpresa(idEmpresa: number): CurtidaCandidato[]

    buscarPorCandidato(idCandidato: number): CurtidaCandidato[]

    buscar(
        idEmpresa: number,
        idCandidato: number,
        idVaga: number
    ): CurtidaCandidato | undefined
}