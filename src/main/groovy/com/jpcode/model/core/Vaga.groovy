package com.jpcode.model.core

class Vaga {
    Long id
    String nome
    String descricao
    List competencias = []
    String local
    Long idEmpresa
    List<Candidato> candidatosQueCurtiram = []

    Vaga(String nome, String descricao, String local, Long idEmpresa) {
        this.nome = nome
        this.descricao = descricao
        competencias = []
        candidatosQueCurtiram = []
        this.local = local
        this.idEmpresa = idEmpresa
    }

    Vaga(Long id, String nome, String descricao, String local,Long idEmpresa) {
        this.id = id
        this.nome = nome
        this.descricao = descricao
        competencias = []
        candidatosQueCurtiram = []
        this.local = local
        this.idEmpresa = idEmpresa
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
        LOCAL: ${local} 
        """
    }
}
