package com.jpcode.dto.recrutador

import com.jpcode.model.core.Recrutador
import com.jpcode.model.referencia.AreaRecrutamento
import java.time.LocalDate

/** Perfil próprio; nunca reutilizar na descoberta ou no match. */
class RecrutadorViewDTO {
    Long id
    String nome
    String cpf
    String email
    LocalDate dataNascimento
    String descricao
    Long idEmpresa
    AreaRecrutamento areaRecrutamento

    RecrutadorViewDTO(Recrutador recrutador, AreaRecrutamento area) {
        id = recrutador.id
        nome = recrutador.nome
        cpf = recrutador.cpf
        email = recrutador.email
        dataNascimento = recrutador.dataNascimento
        descricao = recrutador.descricao
        idEmpresa = recrutador.idEmpresa
        areaRecrutamento = area
    }
}
