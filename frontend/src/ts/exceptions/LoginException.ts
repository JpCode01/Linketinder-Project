export class LoginException extends Error {

    constructor() {
        super("E-mail ou senha inválidos.")
    }
}