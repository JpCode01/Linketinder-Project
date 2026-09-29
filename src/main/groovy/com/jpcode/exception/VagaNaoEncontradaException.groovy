package com.jpcode.exception

class VagaNaoEncontradaException extends RuntimeException{

    VagaNaoEncontradaException(Long id) {
        super("Vaga de ID ${id} não encontrada")
    }
}
