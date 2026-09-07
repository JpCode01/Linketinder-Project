import { Candidato } from "./Candidato"
import { Empresa } from "./Empresa"
import { Competencia } from "./Competencia"


interface IVaga {
    nome: string
    descricao: string
    empresa: string
    tipo: string
    localização: string
}

export interface IVagaJSON {
    _nome: string
    _descricao: string
    _empresa: string
    _tipo: string
    _localizacao: string
}

export class Vaga implements IVaga {
    private competencias: Competencia[]
    private candidatosQueCurtiram: Candidato[]

    constructor(
        private _nome: string,
        private _descricao: string,
        private _empresa: string,
        private _tipo: string,
        private _localizacao: string
    ) {
        this.competencias = []
        this.candidatosQueCurtiram = []
    }

    get nome(): string {
        return this._nome
    }

    get descricao(): string {
        return this._descricao
    }

    get empresa(): string {
        return this._empresa
    }

    get tipo(): string {
        return this._tipo
    }

    get localização(): string {
        return this._localizacao
    }

    addCandidatoQueCurtiu(candidato: Candidato) {
        if (candidato != null) {
            this.candidatosQueCurtiram.push(candidato)
        } else {
            throw "Candidato não pode ser nulo"
        }
    }

    addCompetencia(competencia: Competencia) {
        if (competencia != null) {
            this.competencias.push(competencia)
        } else {
            throw "Competência não pode ser nula"
        }
    }
}