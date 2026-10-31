import { Estado } from "./Estado"
import { Pais } from "./Pais"

export class Pessoa {
    constructor(
        public id: number,
        public nome: string,
        public email: string,
        public estado: Estado,
        public pais: Pais,
        public cep: string,
        public descricao: string
    ) {}
}