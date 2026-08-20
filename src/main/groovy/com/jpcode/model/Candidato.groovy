package com.jpcode.model

import com.jpcode.enums.CompetenciasEnum

class Candidato extends Pessoa {
    String cpf
    int idade
    List vagasCurtidas = []


    Candidato(String nome, String email, String cpf, int idade, String estado, String cep, String descricao) {
        super(nome, email, estado, cep, descricao)
        this.cpf = cpf
        this.idade = idade
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
