export class CnpjJaCadastradoException extends Error {
    
    constructor() {
        super("CNPJ já cadastrado")
    }
}