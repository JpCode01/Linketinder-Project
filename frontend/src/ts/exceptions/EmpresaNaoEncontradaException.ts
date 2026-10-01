export class EmpresaNaoEncontradaException extends Error {
    constructor(idEmpresa: number) {
        super("Empresa de ID " + idEmpresa + " não encontrada!")
    }
}