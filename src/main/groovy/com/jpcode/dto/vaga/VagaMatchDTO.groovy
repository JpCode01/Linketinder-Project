package com.jpcode.dto.vaga

class VagaMatchDTO {
    Long id
    String nome
    String descricao
    List<String> competencias

    VagaMatchDTO(Long id, String nome, String descricao, List<String> competencias) {
        this.id = id
        this.nome = nome
        this.descricao = descricao
        this.competencias = competencias
    }

    @Override
    public String toString() {
        return """
        ---------------------------------

        VAGA:
        
        ID: ${id}
        NOME DA VAGA: ${nome}
        DESCRIÇÃO: ${descricao}
        COMPETENCIAS: ${competencias.join(", ")}
        """
    }
}
