package com.jpcode.exception

class CompetenciaNaoEncontradaException extends RuntimeException {

    CompetenciaNaoEncontradaException(String competencia) {
        super("Competência não encontrada: ${competencia}")
    }
}
