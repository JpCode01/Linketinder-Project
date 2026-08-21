package com.jpcode.model

class Vaga {
    String nome
    String descricao
    List competencias = []
    List<Candidato> candidatosQueCurtiram = []

    Vaga(String nome, String descricao) {
        this.nome = nome
        this.descricao = descricao
    }

    void adicionarCandidatoQueCurtiu(Candidato candidato) {
        candidatosQueCurtiram.add(candidato)
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
