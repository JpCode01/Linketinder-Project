
package com.jpcode.dto.candidato

import com.jpcode.model.referencia.Competencia
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

import java.time.LocalDate

class CandidatoProfileViewDTO {
    Long id
    String nome
    String sobrenome
    String email
    Estado estado
    String cep
    String descricao
    Pais pais
    int idade
    LocalDate dataNascimento
    List<Competencia> competencias

    CandidatoProfileViewDTO(
            Long id,
            String nome,
            String sobrenome,
            String email,
            Estado estado,
            String cep,
            String descricao,
            Pais pais,
            int idade,
            LocalDate dataNascimento,
            List<Competencia> competencias
    ) {
        this.id = id
        this.nome = nome
        this.sobrenome = sobrenome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.pais = pais
        this.idade = idade
        this.dataNascimento = dataNascimento
        this.competencias = competencias
    }
}
