package com.jpcode.exception.referencia

class CompetenciaNaoEncontradaException extends RuntimeException {

    CompetenciaNaoEncontradaException(Long idCompetencia) {
        super("Competência de id ${idCompetencia} não encontrada!")
    }

    CompetenciaNaoEncontradaException(String competencia) {
        super("Competência não encontrada: ${competencia}")
    }
}
