package com.jpcode.api.servlet

import com.jpcode.controller.competencia.CompetenciaController
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class CompetenciaServlet extends JsonServlet {

    private final CompetenciaController competenciaController

    CompetenciaServlet(CompetenciaController competenciaController) {
        this.competenciaController = competenciaController
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) { json(response, competenciaController.listarCompetenciasEmString()) }
    }
}
