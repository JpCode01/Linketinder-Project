import { Competencia } from "../../../models/Competencia"

export interface MatchResponseDTO {
    id: number
    nomeCandidato: string
    sobrenomeCandidato: string
    emailCandidato: string
    cpfCandidato: string
    descricaoCandidato: string
    idadeCandidato: number
    estadoCandidato: string
    paisCandidato: string
    competenciasCandidato: Competencia[]

    nomeEmpresa: string
    cnpjEmpresa: string
    emailCorporativo: string
    descricaoEmpresa: string
    paisEmpresa: string
    estadoEmpresa: string
    
    nome: string
    descricao: string
    competencias: string[]
}