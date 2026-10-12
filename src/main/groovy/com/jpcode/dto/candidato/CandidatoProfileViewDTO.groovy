package com.jpcode.dto.candidato

import com.jpcode.model.referencia.Competencia

import java.time.LocalDate

class CandidatoProfileViewDTO {
    Long id
    String nome
    String sobrenome
    String email
    Long idEstado
    String cep
    String descricao
    Long idPais
    int idade
    LocalDate dataNascimento
    List<Competencia> competencias

    CandidatoProfileViewDTO(
            Long id,
            String nome,
            String sobrenome,
            String email,
            Long idEstado,
            String cep,
            String descricao,
            Long idPais,
            int idade,
            LocalDate dataNascimento,
            List<Competencia> competencias
    ) {
        this.id = id
        this.nome = nome
        this.sobrenome = sobrenome
        this.email = email
        this.idEstado = idEstado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
        this.idade = idade
        this.dataNascimento = dataNascimento
        this.competencias = competencias
    }
}