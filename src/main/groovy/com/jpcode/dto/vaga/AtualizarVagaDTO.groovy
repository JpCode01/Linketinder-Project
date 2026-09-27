package com.jpcode.dto.vaga

class AtualizarVagaDTO {
    Long id
    String nome
    String descricao
    String local
    Long idEmpresa

    AtualizarVagaDTO(Long id, String nome, String descricao, String local, Long idEmpresa) {
        this.id = id
        this.nome = nome
        this.descricao = descricao
        this.local = local
        this.idEmpresa = idEmpresa
    }
}
