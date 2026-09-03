import { Pessoa } from "./Pessoa"
import { Candidato } from "./Candidato"
import { Vaga } from "./Vaga"

interface IEmpresa {
    cnpj: string
    pais: string
    
}

export class Empresa extends Pessoa implements IEmpresa {

    private candidatosCurtidos: Candidato[]
    private vagas: Vaga[]

    constructor(private _cnpj: string, 
                private _pais: string,
                _nome: string,
                _email:string,
                _estado:string, 
                _cep:string,
                _descricao:string
    ) {
        super(_nome, _email, _estado, _cep, _descricao)
        this.candidatosCurtidos = []
        this.vagas = []
    }

    get cnpj(): string {
        return this.cnpj
    }

    get pais(): string {
        return this.pais
    }

    get getCandidatosCurtidos(): Candidato[] {
        return this.candidatosCurtidos
    }

    get getVagas(): Vaga[] {
        return this.vagas
    }
}

