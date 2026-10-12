package com.jpcode.dto.empresa

import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class EmpresaProfileViewDTO {
    Long id
    String nome
    String email
    Estado estado
    String cep
    String descricao
    Pais pais

    EmpresaProfileViewDTO(
            Long id,
            String nome,
            String email,
            Estado estado,
            String cep,
            String descricao,
            Pais pais
    ) {
        this.id = id
        this.nome = nome
        this.email = email
        this.estado = estado
        this.cep = cep
        this.descricao = descricao
        this.pais = pais
    }
}