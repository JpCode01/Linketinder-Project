package com.jpcode.exception

class CandidatoNaoEncontradoException extends RuntimeException {

    CandidatoNaoEncontradoException(Long id) {
        super("Candidato de ID ${id} não encontrado")
    }
}
