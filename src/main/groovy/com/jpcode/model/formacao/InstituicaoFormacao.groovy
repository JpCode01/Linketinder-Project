package com.jpcode.model.formacao

class InstituicaoFormacao {
    Long id
    String nomeInstituicao

    InstituicaoFormacao(String nomeInstituicao) {
        this.nomeInstituicao = nomeInstituicao
    }

    InstituicaoFormacao(Long id, String nomeInstituicao) {
        this.id = id
        this.nomeInstituicao = nomeInstituicao
    }
}
