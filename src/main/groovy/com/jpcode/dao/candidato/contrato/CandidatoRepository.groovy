package com.jpcode.dao.candidato.contrato

import com.jpcode.model.core.Candidato

interface CandidatoRepository {

    Candidato salvar(Candidato candidato)

    Optional<Candidato> buscarPorId(Long id)

    Candidato atualizarDados(Candidato candidato)
}