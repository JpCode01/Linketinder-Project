package com.jpcode.model

class Pais { 
    Long id
    String nome

    Pais(String nome) {
        this.nome = nome
    }

    Pais(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }


    
}
