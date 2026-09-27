package com.jpcode.exception

class PaisNaoEncontradoException extends RuntimeException {

    PaisNaoEncontradoException(String nome) {
        super("País não encontrado: ${nome}")
    }
}