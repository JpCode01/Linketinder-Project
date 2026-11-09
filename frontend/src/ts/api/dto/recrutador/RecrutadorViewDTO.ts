import { AreaRecrutamentoResponseDTO } from "./AreaRecrutamentoResponseDTO"
import { CandidatoViewDTO } from "../candidato/CandidatoViewDTO"

export interface RecrutadorViewDTO {
    id: number
    nome: string
    cpf: string
    email: string
    dataNascimento: CandidatoViewDTO["dataNascimento"]
    descricao: string
    idEmpresa: number
    areaRecrutamento: AreaRecrutamentoResponseDTO
}
