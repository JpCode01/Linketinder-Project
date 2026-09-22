package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.model.referencia.Competencia

class CompetenciaService {
    final CompetenciaDAO competenciaDAO

    CompetenciaService(CompetenciaDAO competenciaDAO) {
        this.competenciaDAO = competenciaDAO
    }

    List<String> listarCompetencias() {
        return competenciaDAO.listarTodasCompetencias()
    }

    Competencia buscarCompetencia(Long idCompetencia) {
        return competenciaDAO.buscarPorId(idCompetencia)
    }
}
