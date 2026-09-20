package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO

class CompetenciaService {
    final CompetenciaDAO competenciaDAO

    CompetenciaService(CompetenciaDAO competenciaDAO) {
        this.competenciaDAO = competenciaDAO
    }

    List<String> listarCompetencias() {
        return competenciaDAO.listarTodasCompetencias()
    }
}
