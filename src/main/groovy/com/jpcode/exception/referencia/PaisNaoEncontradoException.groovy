package com.jpcode.exception.referencia

class PaisNaoEncontradoException extends RuntimeException {

    PaisNaoEncontradoException(String nome) {
        super("País não encontrado: ${nome}")
    }
}