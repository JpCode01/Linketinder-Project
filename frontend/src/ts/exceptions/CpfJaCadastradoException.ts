export class CpfJaCadastradoException extends Error {

    constructor() {
        super("CPF já cadastrado")
    }
}