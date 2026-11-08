import { Competencia } from "../../../models/Competencia"

export interface VagaEmpresaResponseDTO {
    id: number
    nome: string
    descricao: string
    local: string
    qtdeCandidatosQueCurtiram: number
    competencias: Competencia[]
}