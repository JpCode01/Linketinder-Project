import { Competencia } from "./Competencia"

export class Vaga {
    private competencias: Competencia[]

    constructor(
        private _id: number,
        private _nome: string,
        private _descricao: string,
        private _tipo: string,
        private _localizacao: string,
        private _idEmpresa: number
    ) {
        this.competencias = []
    }

    get id(): number {
        return this._id
    }

    get nome(): string {
        return this._nome
    }

    get descricao(): string {
        return this._descricao
    }

    get tipo(): string {
        return this._tipo
    }

    get localização(): string {
        return this._localizacao
    }

    get getCompetencias(): Competencia[] {
        return this.competencias
    }

    get idEmpresa(): Number {
        return this._idEmpresa
    }

    addCompetencia(competencia: Competencia) {
        if (competencia != null) {
            this.competencias.push(competencia)
        } else {
            throw "Competência não pode ser nula"
        }
    }

    setCompetencias(competencias: Competencia[]) {
        this.competencias = competencias
    }
}