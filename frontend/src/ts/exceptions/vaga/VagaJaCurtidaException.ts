export class VagaJaCurtidaException extends Error {

    constructor(idVaga: number) {
        super("Vaga de ID " + idVaga + " já foi curtida pelo candidato!")
    }
}