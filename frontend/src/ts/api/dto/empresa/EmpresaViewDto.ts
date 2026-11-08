import { Estado } from "../../../models/Estado";
import { Pais } from "../../../models/Pais";

export interface EmpresaViewDTO {
    id: number
    nome: string
    email: string
    estado: Estado
    cep: string
    descricao: string
    pais: Pais
}