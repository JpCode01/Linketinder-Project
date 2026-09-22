package com.jpcode.model.formacao

import java.time.LocalDate

class Formacao {
    Long id
    String curso
    Long idCandidato
    Long idInstituicao
    LocalDate inicio
    LocalDate terminoOuPossivel

    Formacao(
            String curso,
            Long idCandidato,
            Long idInstituicao,
            LocalDate inicio,
            LocalDate terminoOuPossivel
    ) {
        this.curso = curso
        this.idCandidato = idCandidato
        this.idInstituicao = idInstituicao
        this.inicio = inicio
        this.terminoOuPossivel = terminoOuPossivel
    }

    Formacao(
            Long id,
            String curso,
            Long idCandidato,
            Long idInstituicao,
            LocalDate inicio,
            LocalDate terminoOuPossivel
    ) {
        this.id = id
        this.curso = curso
        this.idCandidato = idCandidato
        this.idInstituicao = idInstituicao
        this.inicio = inicio
        this.terminoOuPossivel = terminoOuPossivel
    }
}