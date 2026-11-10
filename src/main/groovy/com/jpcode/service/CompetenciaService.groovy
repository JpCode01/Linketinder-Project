package com.jpcode.service

import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.model.referencia.Competencia

class CompetenciaService {
    final ReferenciaRepository<Competencia> referenciaRepository
    final ReferenciaCompetenciaRepository referenciaCompetenciaRepository

    CompetenciaService(ReferenciaRepository<Competencia> referenciaRepository, ReferenciaCompetenciaRepository referenciaCompetenciaRepository) {
        this.referenciaRepository = referenciaRepository
        this.referenciaCompetenciaRepository = referenciaCompetenciaRepository
    }

    List<String> listarCompetenciasEmString() {
        return referenciaCompetenciaRepository.listarCompetenciasEmString()
    }

    List<Competencia> listarCompetencias() {
        return referenciaRepository.listarTodos()
    }

    Competencia buscarCompetencia(Long idCompetencia) {
        return referenciaRepository.buscarPorId(idCompetencia)
        .orElseThrow(() ->
                new CompetenciaNaoEncontradaException(idCompetencia))
    }
}
