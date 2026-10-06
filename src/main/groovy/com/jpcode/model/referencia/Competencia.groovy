package com.jpcode.model.referencia


class Competencia {
    Long id
    String nome

    Competencia(Long id, String nome) {
        this.id = id
        this.nome = nome
    }


    @Override
    String toString() {
        return """
            ----------------------------------------------------------------

            Competencia:
            
            Id: ${id}
            Nome: ${nome}
        """;
    }
}
