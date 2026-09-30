package com.jpcode.exception

class CandidatoNaoEncontradoPorIdException extends RuntimeException {

    CandidatoNaoEncontradoPorIdException(Long id) {
        super("Candidato de ID ${id} não encontrado")
    }
}
