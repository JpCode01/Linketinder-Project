export class VagaNaoEncontradaException extends Error {
    
    constructor(idVaga: number) {
        super("Vaga de ID " + idVaga + " não encontrada!")
    }
}