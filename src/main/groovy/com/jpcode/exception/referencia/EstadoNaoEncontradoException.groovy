package com.jpcode.exception.referencia

class EstadoNaoEncontradoException extends RuntimeException {

    EstadoNaoEncontradoException(String sigla) {
        super("Estado não encontrado: ${sigla}")
    }

    EstadoNaoEncontradoException(Long id) {
        super("Estado não encontrado de ID ${id}")
    }
}
