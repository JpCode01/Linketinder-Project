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
    public boolean equals(Object objeto) {
        if (this.is(objeto)) {
            return true
        }

        if (!(objeto instanceof VagaAnonimaDTO)) {
            return false
        }

        VagaAnonimaDTO outraVaga = (VagaAnonimaDTO) objeto

        return this.id == outraVaga.id
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0
    }


    @Override
    public String toString() {
        return """
            ----------------------------------------------------------------

            VAGA ANONIMA:
            
            ID: ${id}
            DESCRICAO: ${descricao}
            COMPETENCIAS: ${competencias*.nome.join(", ")}
        """;
    }
}
