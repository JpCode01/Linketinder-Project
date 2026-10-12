package com.jpcode.exception.empresa

class EmpresaNaoEncontradaPorIdException extends RuntimeException {

    EmpresaNaoEncontradaPorIdException(Long id) {
        super("Empresa de ID ${id} não encontrada")
    }
}
