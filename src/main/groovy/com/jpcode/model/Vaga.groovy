package com.jpcode.model

class Vaga {
    String nome
    String descricao
    List competencias = []

    Vaga(String nome, String descricao) {
        this.nome = nome
        this.descricao = descricao
    }


    @Override
    String toString() {
        return """
        ---------------------------------
        NOME DA VAGA: ${nome}
        DESCRIÇÃO: ${descricao}
        COMPETENCIAS ${competencias} 
        """
    }
}
