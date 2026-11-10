package com.jpcode.model.core

import java.time.LocalDate

class Recrutador {
    Long id
    String nome
    Long idAreaRecrutamento
    String descricao
    String email
    String cpf
    LocalDate dataNascimento
    String senha
    Long idEmpresa
    boolean ativo = true
}
