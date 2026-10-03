package com.jpcode.dto.empresa

class AtualizarEmpresaDTO {
    Long id
    String nome
    String email
    String senha
    String cnpj
    String pais
    String estado
    String cep
    String descricao
    boolean ativo

    AtualizarEmpresaDTO(Long id, String nome, String email, String senha, String cnpj, String pais, String estado, String cep, String descricao, boolean ativo) {
        this.id = id
        this.nome = nome
        this.email = email
        this.senha = senha
        this.cnpj = cnpj
        this.pais = pais
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.ativo = ativo
    }
}
