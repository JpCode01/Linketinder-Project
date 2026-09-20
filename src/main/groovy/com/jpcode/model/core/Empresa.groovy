package com.jpcode.model.core

class Empresa extends Pessoa {

    String cnpj
    List<Vaga> vagas = []
    List<Candidato> candidatosCurtidos = []

    Empresa(String nome, String email, String senha, String cnpj, Long idPais, Long idEstado, String cep, String descricao) {
        super(nome, email, senha, idEstado, cep, descricao, idPais)
        this.cnpj = cnpj
    }

    Empresa(Long id, String nome, String email, String senha, String cnpj, Long idPais, Long idEstado, String cep, String descricao, boolean ativo) {
        super(nome, email, senha, idEstado, cep, descricao, idPais, id, ativo)
        this.cnpj = cnpj
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
            CEP: ${cep}
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
