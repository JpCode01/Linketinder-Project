package com.jpcode.model

import java.time.LocalDate

class Candidato extends Pessoa {
    String cpf
    int idade
    String sobrenome
    LocalDate dataNascimento
    List<Vaga> vagasCurtidas = []

    Candidato(String nome, String sobrenome, String email, String cpf, LocalDate dataNascimento,Integer idPais, int idade, String estado, String cep, String descricao) {
        super(nome, email, estado, cep, descricao, idPais)
        this.cpf = cpf
        this.idade = idade
        this.sobrenome = sobrenome
        this.dataNascimento = dataNascimento
    }

    Candidato(Long id, String nome, String sobrenome, String email, String cpf, LocalDate dataNascimento, Integer idPais, int idade, String estado, String cep, String descricao) {
        super(nome, email, estado, cep, descricao, idPais, id)
        this.cpf = cpf
        this.idade = idade
        this.sobrenome = sobrenome
        this.dataNascimento = dataNascimento
    }

    @Override
    String toString() {
        return """
            ----------------------------------------------------------------

            Candidato:

            Nome: ${nome}
            Descricao: ${descricao}
            Idade: ${idade}
            Email: ${email}
            CPF: ${cpf}
            Estado: ${estado}
            CEP: ${cep}
            Competencia: ${competencias}
        """;
    }

    void adicionarVagaCurtida(Vaga vaga) {
        vagasCurtidas.add(vaga)
    }
}
