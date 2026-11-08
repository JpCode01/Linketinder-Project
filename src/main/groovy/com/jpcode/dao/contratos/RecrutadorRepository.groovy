package com.jpcode.dao.contratos

import com.jpcode.model.core.Recrutador

interface RecrutadorRepository {
    Optional<Recrutador> buscarAtivoPorEmail(String email)
    Recrutador salvar(Recrutador recrutador)
    Optional<Recrutador> buscarPorId(Long id)
}
