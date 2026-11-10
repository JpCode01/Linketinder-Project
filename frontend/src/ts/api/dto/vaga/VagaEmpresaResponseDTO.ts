import { Competencia } from "../../../models/Competencia"

export interface VagaEmpresaResponseDTO {
    nomeRecrutador: string | null
    areaRecrutamento: string | null
    idRecrutador: number | null
    id: number
    nome: string
    descricao: string
    local: string
    qtdeCandidatosQueCurtiram: number
    competencias: Competencia[]
}