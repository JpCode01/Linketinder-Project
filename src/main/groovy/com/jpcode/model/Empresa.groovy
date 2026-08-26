package com.jpcode.model

class Empresa extends Pessoa {

    String cnpj
    String pais
    List<Vaga> vagas = []
    List<Candidato> candidatosCurtidos = []

    Empresa(String nome, String email, String cnpj, String pais, String estado, String cep, String descricao) {
        super(nome, email, estado, cep, descricao)
        this.cnpj = cnpj
        this.pais = pais
    }

    @Override
    String toString() {
        return """
            ----------------------------------------------------------------

            Empresa:

            Nome: ${nome}
            Descricao: ${descricao}
            Email: ${email}
            CNPJ: ${cnpj}
            Pais: ${pais}
            Estado: ${estado}
            CEP: ${cep}
            Competencia: ${competencias}
        """;
    }

    void adicionarVaga(Vaga vaga) {
        if (vaga && !vagas.contains(vaga)) {
            vagas.add(vaga)
        }
    }

    void adicionarCandidatoCurtido(Candidato candidato) {
        if (candidato && !candidatosCurtidos.contains(candidato)) {
            candidatosCurtidos.add(candidato)
        }
    }
}
