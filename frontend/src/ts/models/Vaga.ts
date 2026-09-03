import { Candidato } from "./Candidato"
import { Empresa } from "./Empresa"
import { Competencia } from "./Competencia"


interface IVaga {
    nome: string
    descricao: string
    empresa: Empresa
}

export class Vaga implements IVaga {
    private competencias: Competencia[]
    private candidatosQueCurtiram: Candidato[]

    constructor(private _nome: string,
                private _descricao: string,
                private _empresa: Empresa
    ) {
        this.competencias = []
        this.candidatosQueCurtiram = []
    }

    get nome(): string {
        return this.nome
    }

    
    get descricao(): string {
        return this.descricao
    }


    get empresa(): Empresa {
        return this.empresa
    }
}
