
package com.jpcode.api.servlet

import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.match.MatchController
import com.jpcode.controller.vaga.VagaController
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class CurtidaServlet extends HttpServlet {

    private final VagaController vagaController
    private final EmpresaController empresaController
    private final MatchController matchController

    CurtidaServlet(
            VagaController vagaController,
            EmpresaController empresaController,
            MatchController matchController
    ) {
        this.vagaController = vagaController
        this.empresaController = empresaController
        this.matchController = matchController
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        String pathInfo = request.pathInfo

        try {
            Map dados = new JsonSlurper().parse(request.inputStream) as Map

            if (pathInfo == "/vagas") {
                Long idCandidato = dados.idCandidato as Long
                Long idVaga = dados.idVaga as Long

                vagaController.curtir(idCandidato, idVaga)

                response.status = HttpServletResponse.SC_CREATED
                response.writer.write(
                        JsonOutput.toJson([
                                mensagem: "Vaga curtida com sucesso."
                        ])
                )
                return
            }

            if (pathInfo == "/candidatos") {
                Long idEmpresa = dados.idEmpresa as Long
                Long idCandidato = dados.idCandidato as Long
                Long idVaga = dados.idVaga as Long

                matchController.curtirCandidato(
                        idEmpresa,
                        idCandidato,
                        idVaga
                )

                response.status = HttpServletResponse.SC_CREATED
                response.writer.write(
                        JsonOutput.toJson([
                                mensagem: "Candidato curtido com sucesso."
                        ])
                )
                return
            }

            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([erro: "Rota de curtida não encontrada."])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Não foi possível registrar a curtida."
                    ])
            )
        }
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
            
            if (partes.size() == 4 &&
                    partes[1] == "vagas" &&
                    partes[2] == "candidato" &&
                    partes[3] ==~ /\d+/) {

                Long idCandidato = partes[3].toLong()

                response.status = HttpServletResponse.SC_OK
                response.writer.write(
                        JsonOutput.toJson(
                                vagaController.listarVagasCurtidas(idCandidato)
                        )
                )
                return
            }

            if (partes.size() == 4 &&
                    partes[1] == "candidatos" &&
                    partes[2] == "empresa" &&
                    partes[3] ==~ /\d+/) {

                Long idEmpresa = partes[3].toLong()

                response.status = HttpServletResponse.SC_OK
                response.writer.write(
                        JsonOutput.toJson(
                                empresaController.buscarCandidatosCurtidos(idEmpresa)
                        )
                )
                return
            }

            if (partes.size() == 4 &&
                    partes[1] == "candidatos" &&
                    partes[2] == "vaga" &&
                    partes[3] ==~ /\d+/) {

                Long idVaga = partes[3].toLong()

                response.status = HttpServletResponse.SC_OK
                response.writer.write(
                        JsonOutput.toJson(
                                empresaController.buscarCandidatosQueCurtiram(idVaga)
                        )
                )
                return
            }

            if (partes.size() == 3 &&
                    partes[1] == "candidatos" &&
                    partes[2] == "verificar") {

                Long idEmpresa = request.getParameter("empresaId")?.toLong()
                Long idCandidato = request.getParameter("candidatoId")?.toLong()

                if (idEmpresa == null || idCandidato == null) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.writer.write(
                            JsonOutput.toJson([erro: "Informe empresaId e candidatoId."])
                    )
                    return
                }

                boolean jaCurtiu = empresaController.jaCurtiuCandidato(
                        idEmpresa,
                        idCandidato
                )

                response.status = HttpServletResponse.SC_OK
                response.writer.write(JsonOutput.toJson(jaCurtiu))
                return
            }

            if (partes.size() == 3 &&
                    partes[1] == "vagas" &&
                    partes[2] == "verificar") {

                String candidatoIdParametro = request.getParameter("candidatoId")
                String vagaIdParametro = request.getParameter("vagaId")

                if (!(candidatoIdParametro ==~ /\d+/) ||
                        !(vagaIdParametro ==~ /\d+/)) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.writer.write(
                            JsonOutput.toJson([
                                    erro: "Informe candidatoId e vagaId válidos."
                            ])
                    )
                    return
                }

                Long idCandidato = candidatoIdParametro.toLong()
                Long idVaga = vagaIdParametro.toLong()

                boolean jaCurtiu = vagaController.jaFoiCurtidaPorCandidato(
                        idCandidato,
                        idVaga
                )

                response.status = HttpServletResponse.SC_OK
                response.writer.write(JsonOutput.toJson(jaCurtiu))
                return
            }

            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Rota de consulta não encontrada."
                    ])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao consultar curtidas."
                    ])
            )
        }
    }
}