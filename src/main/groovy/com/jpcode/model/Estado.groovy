package com.jpcode.model

class Estado {
    Long id
    String nome

    Estado(String nome) {
        this.nome = nome
    }

    Estado(Long id, String nome) {
        this.id = id
        this.nome = nome
    }
}
