export class EmpresaNaoEncontradaException extends Error {
    constructor(identificador: number | string) {
        if (typeof identificador === "number") {
            super("Empresa de ID " + identificador + " não encontrada!")
        } else {
            super("Empresa de email " + identificador + " não encontrada!")
        }
    }
}