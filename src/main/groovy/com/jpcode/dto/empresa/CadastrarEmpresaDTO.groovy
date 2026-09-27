package com.jpcode.dto.empresa

class CadastrarEmpresaDTO {
    String nome
    String email
    String senha
    String cnpj
    String pais
    String estado
    String cep
    String descricao

    CadastrarEmpresaDTO(String nome, String email, String senha, String cnpj, String pais, String estado, String cep, String descricao) {
        this.nome = nome
        this.email = email
        this.senha = senha
        this.cnpj = cnpj
        this.pais = pais
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
    }
}
