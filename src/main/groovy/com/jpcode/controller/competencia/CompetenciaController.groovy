package com.jpcode.controller.competencia

import com.jpcode.model.referencia.Competencia
import com.jpcode.service.CompetenciaService

class CompetenciaController {

    final CompetenciaService competenciaService

    CompetenciaController(CompetenciaService competenciaService) {
        this.competenciaService = competenciaService
    }

    List<String> listarCompetenciasEmString() {
        return competenciaService.listarCompetenciasEmString()
    }

    List<Competencia> listarCompetencias() {
        return competenciaService.listarCompetencias()
    }

    Competencia buscarCompetencia(Long idCompetencia) {
        return competenciaService.buscarCompetencia(idCompetencia)
    }
}