package com.jpcode.exception.referencia

class EstadoNaoEncontradoException extends RuntimeException {

    EstadoNaoEncontradoException(String sigla) {
        super("Estado não encontrado: ${sigla}")
    }
}
