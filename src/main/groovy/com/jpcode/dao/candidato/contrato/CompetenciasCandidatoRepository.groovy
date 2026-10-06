package com.jpcode.dao.candidato.contrato

import com.jpcode.model.referencia.Competencia

interface CompetenciasCandidatoRepository {
    void salvar(Long idCandidato, Long idCompetencia)

    List<Competencia> buscarPorCandidato(Long idCandidato)

    List<String> buscarPorCandidatoString(Long idCandidato)

    void removerCompetencia(Long idCandidato, Long idCompetencia)

    void atualizar(Long idCandidato, List<Competencia> competencias)
}