package com.jpcode.model.referencia

import com.jpcode.enums.CompetenciasEnum

class Competencia {
    Long id
    String nome

    Competencia(Long id, String nome) {
        this.id = id
        this.nome = nome
    }
}
