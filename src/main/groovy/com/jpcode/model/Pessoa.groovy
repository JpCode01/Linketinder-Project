package com.jpcode.model

import com.jpcode.enums.CompetenciasEnum

class Pessoa implements PessoaInterface {
    String nome
    String email
    String estado
    String cep
    String descricao
    List competencias = []

    Pessoa(String nome, String email, String estado, String cep, String descricao) {
        this.nome = nome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
    }

    String adicionarCompetencia(CompetenciasEnum competencia) {
        competencias.add(competencia)
        competencias.last()
    }
}
