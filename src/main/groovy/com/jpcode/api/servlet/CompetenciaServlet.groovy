package com.jpcode.api.servlet

import com.jpcode.controller.competencia.CompetenciaController
import com.jpcode.model.referencia.Competencia
import groovy.json.JsonOutput
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class CompetenciaServlet extends HttpServlet {

    private final CompetenciaController competenciaController

    CompetenciaServlet(CompetenciaController competenciaController) {
        this.competenciaController = competenciaController
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        List<Competencia> competenicas = competenciaController.listarCompetencias()

        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")
        response.setStatus(HttpServletResponse.SC_OK)
        response.writer.write(
                JsonOutput.toJson(competenicas)
        )
    }
}
