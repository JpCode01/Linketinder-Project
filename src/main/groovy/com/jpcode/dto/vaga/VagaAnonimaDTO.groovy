package com.jpcode.dto.vaga

import com.jpcode.model.referencia.Competencia

class VagaAnonimaDTO {
    Long id
    String descricao
    List<Competencia> competencias

    VagaAnonimaDTO(Long id, String descricao, List<Competencia> competencias) {
        this.id = id
        this.descricao = descricao
        this.competencias = competencias
    }


    @Override
    public String toString() {
        return """
            ----------------------------------------------------------------

            VAGA ANONIMA:
            
            ID: ${id}
            DESCRICAO: ${descricao}
            COMPETENCIAS: ${competencias.forEach {competencia -> print(competencia.nome + ", ")}}
        """;
    }
}
