package com.jpcode.model

import com.jpcode.enums.CompetenciasEnum

class Pessoa implements PessoaInterface {
    Long id
    String nome
    String email
    String estado
    String cep
    String descricao
    Long idPais
    List competencias = []

    Pessoa(String nome, String email, String estado, String cep, String descricao, Long idPais) {
        this.nome = nome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
    }
    
    Pessoa(String nome, String email, String estado, String cep, String descricao,  Long idPais, Long id) {
        this.nome = nome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
        this.id = id
    }

    String adicionarCompetencia(CompetenciasEnum competencia) {
        competencias.add(competencia)
        competencias.last()
    }
}
