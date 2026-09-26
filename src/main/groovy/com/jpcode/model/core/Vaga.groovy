package com.jpcode.model.core

class Vaga {
    Long id
    String nome
    String descricao
    String local
    Long idEmpresa

    Vaga(String nome, String descricao, String local, Long idEmpresa) {
        this.nome = nome
        this.descricao = descricao
        this.local = local
        this.idEmpresa = idEmpresa
    }

    Vaga(Long id, String nome, String descricao, String local,Long idEmpresa) {
        this.id = id
        this.nome = nome
        this.descricao = descricao
        this.local = local
        this.idEmpresa = idEmpresa
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
