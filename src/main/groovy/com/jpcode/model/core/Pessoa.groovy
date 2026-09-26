package com.jpcode.model.core

class Pessoa {
    Long id
    String nome
    String email
    Long idEstado
    String cep
    String descricao
    Long idPais
    String senha
    boolean ativo

    Pessoa(String nome, String email, String senha, Long idEstado, String cep, String descricao, Long idPais) {
        this.nome = nome
        this.email = email
        this.idEstado = idEstado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
        this.ativo = true
        this.senha = senha
    }
    
    Pessoa(String nome, String email, String senha, Long idEstado, String cep, String descricao,  Long idPais, Long id, boolean ativo) {
        this.nome = nome
        this.email = email
        this.idEstado = idEstado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
        this.id = id
        this.senha = senha
        this.ativo = ativo
    }
}
