import { Estado } from "../../../models/Estado"
import { Pais } from "../../../models/Pais"
import { Competencia } from "../../../models/Competencia"

export interface CandidatoViewDTO {
    id: number
    nome: string
    sobrenome: string
    email: string
    estado: Estado
    cep: string
    descricao: string
    pais: Pais
    idade: number
    dataNascimento: string
    competencias: Competencia[]
}