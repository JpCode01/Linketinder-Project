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
    // JsonOutput serializa LocalDate como objeto, não como a data ISO de entrada.
    dataNascimento: {
        year: number
        monthValue: number
        dayOfMonth: number
        dayOfYear: number
        dayOfWeek: string
        month: string
        era: string
        leapYear: boolean
        chronology: { calendarType: string; id: string; isoBased: boolean }
    }
    competencias: Competencia[]
}
