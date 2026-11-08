import { Pessoa } from "./Pessoa"
import { Estado } from "./Estado"
import { Pais } from "./Pais"

export class Empresa extends Pessoa {
    constructor(
        public cnpj: string,
        id: number,
        nome: string,
        email: string,
        estado: Estado,
        pais: Pais,
        cep: string,
        descricao: string
    ) {
        super(id, nome, email, estado, pais, cep, descricao)
    }
}