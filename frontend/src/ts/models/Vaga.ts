import { Competencia } from "./Competencia"

export class Vaga {
    public competencias: Competencia[]

    constructor(
        public id: number,
        public nome: string,
        public descricao: string,
        public tipo: string,
        public localizacao: string,
        public idEmpresa: number
    ) {
        this.competencias = []
    }
}