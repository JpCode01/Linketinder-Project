import { Candidato } from "../../../models/Candidato"

export interface CandidatoConsultaRepository {
    buscarTodos(): Candidato[]
    buscarPorId(id: number): Candidato | undefined
    buscarPorEmail(email: string): Candidato | undefined
    buscarPorCpf(cpf: string): Candidato | undefined
}