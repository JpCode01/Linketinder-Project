package com.jpcode.dto.competencia

class RemoverCompetenciaDTO {
    Long id
    String nome

    RemoverCompetenciaDTO(Long id, String nome) {
        this.id = id
        this.nome = nome
    }

    @Override
    public String toString() {
        return """
            ----------------------------------------------------------------

            COMPETENCIA:
            
            ID: ${id}
            NOME: ${nome}
        """
    }
}
