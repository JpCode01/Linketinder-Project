import { Pessoa } from "./Pessoa"
import { Candidato, ICandidatoJSON } from "./Candidato"
import { Vaga } from "./Vaga"
import { IVagaJSON } from "../models/Vaga";

interface IEmpresa {
    cnpj: string
    pais: string
    
}

export interface IEmpresaJSON {
    _cnpj: string
    _nome: string
    _email: string
    _estado: string
    _pais: string
    _cep: string
    _descricao: string
    candidatosCurtidos: ICandidatoJSON[]
    vagas: IVagaJSON[]
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
        return this._cnpj
    }

    get pais(): string {
        return this._pais
    }

    get getCandidatosCurtidos(): Candidato[] {
        return this.candidatosCurtidos
    }

    get getVagas(): Vaga[] {
        return this.vagas
    }

    addCandidatoCurtido(candidato: Candidato) {
        if (candidato != null) {
            this.candidatosCurtidos.push(candidato)
        } else {
            throw "Candidato não pode ser nulo"
        }
    }

    addVaga(vaga: Vaga) {
        if (vaga != null) {
            this.vagas.push(vaga)
        } else {
            throw "Vaga não pode ser nula"
        }
    }

    setVagas(vagas: Vaga[]) {
        this.vagas = vagas
    }

    setCandidatosCurtidos(candidatos: Candidato[]) {
        this.candidatosCurtidos = candidatos
    }
    
}

