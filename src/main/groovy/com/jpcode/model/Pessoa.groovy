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
    String senha
    boolean ativo

    Pessoa(String nome, String email, String senha, String estado, String cep, String descricao, Long idPais) {
        this.nome = nome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
        this.ativo = true
        this.senha = senha
    }
    
    Pessoa(String nome, String email, String senha, String estado, String cep, String descricao,  Long idPais, Long id) {
        this.nome = nome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
        this.id = id
        this.senha = senha
    }

    String adicionarCompetencia(CompetenciasEnum competencia) {
        competencias.add(competencia)
        competencias.last()
    }

    void desativarUser() {
        if (this.ativo == true) {
            this.ativo = false
        }
    }
}
