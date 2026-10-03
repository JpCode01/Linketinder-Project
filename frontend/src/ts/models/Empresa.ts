import { Pessoa } from "./Pessoa"


export class Empresa extends Pessoa {
    public id: number
    constructor(
                public cnpj: string,
                public pais: string,
                nome: string,
                email: string,
                estado: string,
                cep: string,
                descricao: string
    ) {
        super(nome, email, estado, cep, descricao)
        this.id = 0
    }
}

