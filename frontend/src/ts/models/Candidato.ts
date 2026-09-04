import { Pessoa } from "./Pessoa"
import { Competencia } from "./Competencia"
import { Vaga } from "./Vaga"

interface ICandidato {
    cpf: string
    idade: number
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
        return this.cpf
    }

    get idade(): number {
        return this.idade
    }

    get formacao(): string {
        return this.formacao
    } 


    get getCompetencias(): Competencia[] {
        return this.competencias
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

}
