package com.jpcode.dto.vaga

class CadastrarVagaDTO {
    String nome
    String descricao
    String local
    Long idEmpresa
    List<String> competencias

    CadastrarVagaDTO(String nome, String descricao, String local, Long idEmpresa, List<String> competencias) {
        this.nome = nome
        this.descricao = descricao
        this.local = local
        this.idEmpresa = idEmpresa
        this.competencias = competencias
    }
}
