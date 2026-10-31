package com.jpcode.api.servlet

import com.jpcode.controller.referencia.ReferenciaController
import groovy.json.JsonOutput
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class ReferenciaServlet extends HttpServlet {

    private final ReferenciaController referenciaController

    ReferenciaServlet(ReferenciaController referenciaController) {
        this.referenciaController = referenciaController
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String caminho = request.pathInfo

        Object resultado

        switch (caminho) {
            case "/paises":
                resultado = referenciaController.listarPaises()
                break

            case "/estados":
                resultado = referenciaController.listarEstados()
                break

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
                return
        }

        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")
        response.setStatus(HttpServletResponse.SC_OK)
        response.writer.write(JsonOutput.toJson(resultado))
    }
}