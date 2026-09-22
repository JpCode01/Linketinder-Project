package com.jpcode.dto.candidato

import com.jpcode.model.referencia.Competencia

class CandidatoAnonimoDTO {
    Long id
    String descricao
    List<Competencia> competencias

    CandidatoAnonimoDTO(Long id, String descricao, List<Competencia> competencias) {
        this.id = id
        this.descricao = descricao
        this.competencias = competencias
    }


    @Override
    public String toString() {
        return """
            ----------------------------------------------------------------

            CANDIDATO ANONIMO:
            
            ID: ${id}
            DESCRICAO: ${descricao}
            COMPETENCIAS: ${competencias*.nome.join(", ")}
        """
    }
}
