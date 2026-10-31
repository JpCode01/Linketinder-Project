package com.jpcode.api.servlet

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.exception.LoginException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class LoginServlet extends HttpServlet {

    private final CandidatoController candidatoController
    private final EmpresaController empresaController

    LoginServlet(CandidatoController candidatoController, EmpresaController empresaController) {
        this.candidatoController = candidatoController
        this.empresaController = empresaController
    }

    @Override   
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        String tipo = request.pathInfo?.substring(1)

        if (!(tipo in ["candidato", "empresa"])) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "Tipo de login inválido."])
            )
            return
        }

        try {
            Map dados = new JsonSlurper().parse(request.inputStream) as Map

            String email = dados.email as String
            String senha = dados.senha as String

            if (!email?.trim() || !senha) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.writer.write(
                        JsonOutput.toJson([
                                erro: "E-mail e senha são obrigatórios."
                        ])
                )
                return
            }

            def usuario

            if (tipo == "candidato") {
                usuario = candidatoController.logar(email, senha)
            } else {
                usuario = empresaController.logar(email, senha)
            }

            response.status = HttpServletResponse.SC_OK
            response.writer.write(
                    JsonOutput.toJson([
                            id  : usuario.id,
                            tipo: tipo
                    ])
            )

        } catch (LoginException e) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "E-mail ou senha inválidos."
                    ])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao realizar login."
                    ])
            )
        }
    }
}
