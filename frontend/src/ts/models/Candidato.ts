import { Pessoa } from "./Pessoa"
import { Competencia } from "./Competencia"
import { Estado } from "./Estado"
import { Pais } from "./Pais"

export class Candidato extends Pessoa {
    public competencias: Competencia[]

    constructor(
        public cpf: string,
        public idade: number,
        public sobrenome: string,
        public dataNascimento: string,
        id: number,
        nome: string,
        email: string,
        estado: Estado,
        pais: Pais,
        cep: string,
        descricao: string
    ) {
        super(id, nome, email, estado, pais, cep, descricao)
        this.competencias = []
    }
}