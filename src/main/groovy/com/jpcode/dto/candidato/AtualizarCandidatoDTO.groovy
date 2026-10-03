package com.jpcode.dto.candidato

import java.time.LocalDate

class AtualizarCandidatoDTO {
    Long id
    String nome
    String sobrenome
    String email
    String senha
    String cpf
    LocalDate dataNascimento
    String pais
    int idade
    String estado
    String cep
    String descricao
    boolean ativo

    AtualizarCandidatoDTO(Long id, String nome, String sobrenome, String email, String senha, String cpf, LocalDate dataNascimento, String pais, int idade, String estado, String cep, String descricao, boolean ativo) {
        this.id = id
        this.nome = nome
        this.sobrenome = sobrenome
        this.email = email
        this.senha = senha
        this.cpf = cpf
        this.dataNascimento = dataNascimento
        this.pais = pais
        this.idade = idade
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.ativo = ativo
    }
}
