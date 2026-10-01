import { Pessoa } from "./Pessoa"


export class Empresa extends Pessoa {

    constructor(public id: number,
                public cnpj: string,
                public pais: string,
                nome: string,
                email: string,
                estado: string,
                cep: string,
                descricao: string
    ) {
        super(nome, email, estado, cep, descricao)
    }
}

