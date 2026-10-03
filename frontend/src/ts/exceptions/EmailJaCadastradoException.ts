export class EmailJaCadastradoException extends Error {

    constructor() {
        super("E-mail já cadastrado")
    }
}