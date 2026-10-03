import { Pessoa } from "./Pessoa"
import { Competencia } from "./Competencia"


export class Candidato extends Pessoa {

    public id: number
    public competencias: Competencia[]

    constructor(public cpf: string,
                public idade: number,
                public formacao: string,
                nome: string,
                email: string,
                estado: string,
                cep: string,
                descricao: string
    ) {
        super(nome, email, estado, cep, descricao)
        this.id = 0
        this.competencias = []
    }
}