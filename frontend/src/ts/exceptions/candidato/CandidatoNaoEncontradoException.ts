export class CandidatoNaoEncontradoException extends Error {

    constructor(identificador: number | string) {
        if (typeof identificador === "number") {
            super("Candidato de ID " + identificador + " não encontrado!")
        } else {
            super("Candidato de email " + identificador + " não encontrado!")
        }
    }
}