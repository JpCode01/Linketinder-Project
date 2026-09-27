package com.jpcode.exception

class EstadoNaoEncontradoException extends RuntimeException {

    EstadoNaoEncontradoException(String sigla) {
        super("Estado não encontrado: ${sigla}")
    }
}
