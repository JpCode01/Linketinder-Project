interface IPessoa {
    nome: string,
    email: string,
    estado: string,
    cep: string
    descricao: string
}

export class Pessoa implements IPessoa{
    constructor(private _nome:string, 
                private _email:string,
                private _estado:string, 
                private _cep:string,
                private _descricao:string
    ) {

    }

    get nome(): string {
        return this._nome
    }

    
    get email(): string {
        return this._email
    }

    
    get estado(): string {
        return this._estado
    }

    
    get cep(): string {
        return this._cep
    }

        get descricao(): string {
        return this._descricao
    }
}