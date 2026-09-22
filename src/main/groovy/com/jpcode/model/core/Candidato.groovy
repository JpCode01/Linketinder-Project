package com.jpcode.model.core


import java.time.LocalDate

class Candidato extends Pessoa {
    String cpf
    int idade
    String sobrenome
    LocalDate dataNascimento
    List<Vaga> vagasCurtidas = []

    Candidato(String nome, String sobrenome, String email, String senha, String cpf, LocalDate dataNascimento,Long idPais, int idade, Long idEstado, String cep, String descricao) {
        super(nome, email, senha, idEstado, cep, descricao, idPais)
        this.cpf = cpf
        this.idade = idade
        this.sobrenome = sobrenome
        this.dataNascimento = dataNascimento
    }

    Candidato(Long id, String nome, String sobrenome, String email, String senha, String cpf, LocalDate dataNascimento, Long idPais, int idade, Long idEstado, String cep, String descricao, boolean ativo) {
        super(nome, email, senha, idEstado, cep, descricao, idPais, id, ativo)
        this.cpf = cpf
        this.idade = idade
        this.sobrenome = sobrenome
        this.dataNascimento = dataNascimento
    }

    void exibirParaEmpresa() {
        println """
            ----------------------------------------------------------------

            CANDIDATO:
            
            NOME: CANDIDATO ANONIMO
            DESCRIÇÃO: ${descricao}
        """
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
            CEP: ${cep}
        """;
    }

    void adicionarVagaCurtida(Vaga vaga) {
        vagasCurtidas.add(vaga)
    }
}
