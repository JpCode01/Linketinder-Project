package com.jpcode.exception.referencia

class VagaNaoEncontradaException extends RuntimeException {

    VagaNaoEncontradaException(Long id) {
        super("Vaga de ID ${id} não encontrada")
    }
}
