import { Pessoa } from "./Pessoa"


export class Empresa extends Pessoa {

    constructor(private _id: number,
                private _cnpj: string,
                private _pais: string,
                _nome: string,
                _email: string,
                _estado: string,
                _cep: string,
                _descricao: string
    ) {
        super(_nome, _email, _estado, _cep, _descricao)
    }

    get id(): number {
        return this._id
    }

    get cnpj(): string {
        return this._cnpj
    }

    get pais(): string {
        return this._pais
    }
}

