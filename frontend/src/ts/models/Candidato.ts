import { Pessoa } from "./Pessoa"
import { Competencia } from "./Competencia"

interface ICandidato {
    cpf: string
    idade: number
}

export class Candidato extends Pessoa implements ICandidato {

    private competencias: Competencia[]

    constructor(private _cpf: string, 
                private _idade: number,
                _nome: string,
                _email:string,
                _estado:string, 
                _cep:string,
                _descricao:string
    ) {
        super(_nome, _email, _estado, _cep, _descricao)
        this.competencias = []
    }

    get cpf(): string {
        return this.cpf
    }

    get idade(): number {
        return this.idade
    }

    get getCompetencias(): Competencia[] {
        return this.competencias
    }




}
