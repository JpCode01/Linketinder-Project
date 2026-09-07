import { Pessoa } from "./Pessoa"
import { Competencia } from "./Competencia"
import { IVagaJSON, Vaga } from "./Vaga"

export interface ICandidato {
    cpf: string
    idade: number
}

export interface ICandidatoJSON {
    _cpf: string
    _idade: number
    _formacao: string
    _nome: string
    _email: string
    _estado: string
    _cep: string
    _descricao: string
    vagasCurtidas: IVagaJSON[]
}



export class Candidato extends Pessoa implements ICandidato {

    private competencias: Competencia[]
    private vagasCurtidas: Vaga[]

    constructor(private _cpf: string, 
                private _idade: number,
                private _formacao: string,
                _nome: string,
                _email:string,
                _estado:string, 
                _cep:string,
                _descricao:string
    ) {
        super(_nome, _email, _estado, _cep, _descricao)
        this.competencias = []
        this.vagasCurtidas = []
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

    get getVagasCurtidas(): Vaga[] {
        return this.vagasCurtidas
    }


    addCompetencia(competencia: Competencia) {
        if (competencia != null) {
            this.competencias.push(competencia)
        } else {
            throw "Competência não pode ser nula"
        }
    }

    addVaga(vaga: Vaga) {
        if (vaga != null) {
            this.vagasCurtidas.push(vaga)
        } else {
            throw "Vaga não pode ser nula"
        }
    }

    setVagasCurtidas(vagas: Vaga[]) {
        this.vagasCurtidas = vagas
    }

}
