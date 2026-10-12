package com.jpcode.dto.empresa

class EmpresaProfileViewDTO {
    Long id
    String nome
    String email
    Long idEstado
    String cep
    String descricao
    Long idPais

    EmpresaProfileViewDTO(
            Long id,
            String nome,
            String email,
            Long idEstado,
            String cep,
            String descricao,
            Long idPais
    ) {
        this.id = id
        this.nome = nome
        this.email = email
        this.idEstado = idEstado
        this.cep = cep
        this.descricao = descricao
        this.idPais = idPais
    }
}
