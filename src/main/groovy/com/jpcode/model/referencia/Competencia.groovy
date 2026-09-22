package com.jpcode.model.referencia


class Competencia {
    Long id
    String nome

    Competencia(Long id, String nome) {
        this.id = id
        this.nome = nome
    }


    @Override
    public String toString() {
        return nome
    }
}
