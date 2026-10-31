
package com.jpcode.api.servlet

import com.jpcode.controller.match.MatchController
import groovy.json.JsonOutput
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class MatchServlet extends HttpServlet {

    private final MatchController matchController

    MatchServlet(MatchController matchController) {
        this.matchController = matchController
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        String pathInfo = request.pathInfo

        try {
            if (!pathInfo) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.writer.write(
                        JsonOutput.toJson([
                                erro: "Rota de consulta inválida."
                        ])
                )
                return
            }

            List<String> partes = pathInfo.split("/") as List<String>

            if (partes.size() == 3 &&
                    partes[1] == "empresa" &&
                    partes[2] ==~ /\d+/) {

                Long idEmpresa = partes[2].toLong()

                response.status = HttpServletResponse.SC_OK
                response.writer.write(
                        JsonOutput.toJson(
                                matchController.verMatchesPorEmpresa(idEmpresa)
                        )
                )
                return
            }

            if (partes.size() == 3 &&
                    partes[1] == "candidato" &&
                    partes[2] ==~ /\d+/) {

                Long idCandidato = partes[2].toLong()

                response.status = HttpServletResponse.SC_OK
                response.writer.write(
                        JsonOutput.toJson(
                                matchController.verMatchesPorCandidato(idCandidato)
                        )
                )
                return
            }

            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Rota de consulta não encontrada."
                    ])
            )

        } catch (Exception e) {
            e.printStackTrace()

            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao consultar matches."
                    ])
            )
        }
    }
}
