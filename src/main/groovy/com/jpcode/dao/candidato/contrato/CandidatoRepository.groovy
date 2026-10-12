package com.jpcode.dao.candidato.contrato

import com.jpcode.dto.candidato.CandidatoComReferenciasDTO
import com.jpcode.dto.candidato.CandidatoViewDTO
import com.jpcode.dto.candidato.CandidatoViewListDTO
import com.jpcode.model.core.Candidato

interface CandidatoRepository {

    Candidato salvar(Candidato candidato)

    Optional<Candidato> buscarPorId(Long id)

    Candidato atualizarDados(Candidato candidato)

    Optional<CandidatoComReferenciasDTO> buscarPorIdComReferencias(Long id)

    List<CandidatoViewListDTO> buscarCandidatos()
}