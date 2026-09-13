interface IPessoa {
    nome: string,
    email: string,
    estado: string,
    cep: string
    descricao: string
}

export class Pessoa implements IPessoa{
    constructor(protected _nome:string, 
                protected _email:string,
                protected _estado:string, 
                protected _cep:string,
                protected _descricao:string
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