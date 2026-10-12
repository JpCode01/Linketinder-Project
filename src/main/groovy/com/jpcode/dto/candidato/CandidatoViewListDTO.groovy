package com.jpcode.dto.candidato

import com.jpcode.model.referencia.Competencia

class CandidatoViewListDTO {
    Long id
    String nome
    String sobrenome
    String email
    List<Competencia> competencias

    CandidatoViewListDTO(
            Long id,
            String nome,
            String sobrenome,
            String email,
            List<Competencia> competencias
    ) {
        this.id = id
        this.nome = nome
        this.sobrenome = sobrenome
        this.email = email
        this.competencias = competencias
    }
}