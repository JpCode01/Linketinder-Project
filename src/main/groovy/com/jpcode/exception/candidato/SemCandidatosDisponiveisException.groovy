package com.jpcode.exception.candidato

class SemCandidatosDisponiveisException extends RuntimeException {

    SemCandidatosDisponiveisException() {
        super("Nao ha candidatos disponiveis!")
    }
}
