export class CandidatoNaoEncontradoException extends Error {

    constructor(idCandidato: number) {
        super("Candidato de ID " + idCandidato + " não encontrado!")
    }
}