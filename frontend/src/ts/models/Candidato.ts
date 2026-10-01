import { Pessoa } from "./Pessoa"
import { Competencia } from "./Competencia"


export class Candidato extends Pessoa {

    private competencias: Competencia[]

    constructor(private _id: number,
                private _cpf: string,
                private _idade: number,
                private _formacao: string,
                _nome: string,
                _email: string,
                _estado: string,
                _cep: string,
                _descricao: string
    ) {
        super(_nome, _email, _estado, _cep, _descricao)
        this.competencias = []
    }

    get id(): number {
        return this._id
    }

    get cpf(): string {
        return this._cpf
    }

    get idade(): number {
        return this._idade
    }

    get formacao(): string {
        return this._formacao
    }

    get getCompetencias(): Competencia[] {
        return this.competencias
    }

    addCompetencia(competencia: Competencia): void {
        if (competencia != null) {
            this.competencias.push(competencia)
        } else {
            throw "Competência não pode ser nula"
        }
    }

    setCompetencias(competencias: Competencia[]): void {
        this.competencias = competencias
    }
}