import { Competencia } from "../../../models/Competencia"

export interface VagaAnonimaResponseDTO {
    id: number
    descricao: string
    competencias: Competencia[]
}