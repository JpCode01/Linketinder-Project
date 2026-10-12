package com.jpcode.dto.empresa

class EmpresaViewListDTO {
    Long id
    String nome
    String email
    String descricao

    EmpresaViewListDTO(
            Long id,
            String nome,
            String email,
            String descricao
    ) {
        this.id = id
        this.nome = nome
        this.email = email
        this.descricao = descricao
    }
}