package com.jpcode.service

import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.model.referencia.Competencia

class CompetenciaService {
    final ReferenciaCompetenciaRepository referenciaCompetenciaRepository
    final ReferenciaRepository<Competencia> referenciaRepository

    CompetenciaService(ReferenciaCompetenciaRepository referenciaCompetenciaRepository, ReferenciaRepository<Competencia> referenciaRepository) {
        this.referenciaCompetenciaRepository = referenciaCompetenciaRepository
        this.referenciaRepository = referenciaRepository
    }

    List<String> listarCompetencias() {
        return referenciaCompetenciaRepository.listarTodasCompetencias()
    }

    Competencia buscarCompetencia(Long idCompetencia) {
        return referenciaRepository.buscarPorId(idCompetencia)
        .orElseThrow(() ->
                new CompetenciaNaoEncontradaException(idCompetencia))
    }
}
