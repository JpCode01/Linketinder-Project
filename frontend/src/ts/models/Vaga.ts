import { Competencia } from "./Competencia"

export class Vaga {
    public competencias: Competencia[]

    constructor(
        public _id: number,
        public _nome: string,
        public _descricao: string,
        public _tipo: string,
        public _localizacao: string,
        public _idEmpresa: number
    ) {
        this.competencias = []
    }
}