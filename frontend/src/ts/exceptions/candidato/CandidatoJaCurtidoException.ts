export class CandidatoJaCurtidoException extends Error {
    
    constructor(idCandidato: number) {
        super("Candidato de ID " + idCandidato + " já curtido pela empresa!")
    }
}