import { Competencia } from "./Competencia"

export class Vaga {
    public id: number
    public competencias: Competencia[]

    constructor(
        public nome: string,
        public descricao: string,
        public tipo: string,
        public localizacao: string,
        public idEmpresa: number
    ) {
        this.id = 0
        this.competencias = []
    }
}