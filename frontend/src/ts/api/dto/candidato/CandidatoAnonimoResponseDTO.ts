import { Competencia } from "../../../models/Competencia"

export interface CandidatoAnonimoResponseDTO {
    id: number
    descricao: string
    competencias: Competencia[]
}