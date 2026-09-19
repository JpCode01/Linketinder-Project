package com.jpcode.dto.candidato

import com.jpcode.model.referencia.Competencia

class CandidatoMatchDTO {
    Long id
    String nome
    String sobrenome
    String email
    String cpf
    Date nascimento
    String cep
    String descricao
    int idade
    String estado
    String pais
    List<Competencia> competencias

    CandidatoMatchDTO(Long id, String nome, String sobrenome, String email, String cpf, Date nascimento, String cep, String descricao, int idade, String estado, String pais, List<Competencia> competencias) {
        this.id = id
        this.nome = nome
        this.sobrenome = sobrenome
        this.email = email
        this.cpf = cpf
        this.nascimento = nascimento
        this.cep = cep
        this.descricao = descricao
        this.idade = idade
        this.estado = estado
        this.pais = pais
        this.competencias = competencias
    }

    @Override
    public String toString() {
        return """
            ----------------------------------------------------------------

            CANDIDATO:
            
            ID: ${id}
            NOME: ${nome} ${sobrenome}
            EMAIL: ${email}
            DESCRICAO: ${descricao}
            IDADE: ${idade}
            NASCIMENTO: ${nascimento}
            CPF: ${cpf}
            CEP: ${cep}
            ESTADO: ${estado}
            PAIS: ${pais}
            COMPETENCIAS: ${competencias.forEach {competencia -> print(competencia.nome + ", ")}}
        """;
    }

}
