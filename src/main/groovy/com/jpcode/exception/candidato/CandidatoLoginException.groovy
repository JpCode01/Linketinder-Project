package com.jpcode.exception.candidato

class CandidatoLoginException extends RuntimeException {

    CandidatoLoginException() {
        super('Credenciais inválidas para candidato.')
    }

}
