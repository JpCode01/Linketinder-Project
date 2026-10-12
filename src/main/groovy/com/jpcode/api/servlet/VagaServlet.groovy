package com.jpcode.api.servlet

import com.jpcode.controller.vaga.VagaController
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class VagaServlet extends HttpServlet {

    private final VagaController vagaController

    VagaServlet(VagaController vagaController) {
        this.vagaController = vagaController
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        try {
            Map dados = new JsonSlurper().parse(request.inputStream)

            CadastrarVagaDTO dto = new CadastrarVagaDTO(
                    dados.nome as String,
                    dados.descricao as String,
                    dados.local as String,
                    dados.idEmpresa as Long,
                    dados.competencias as List<String>
            )

            vagaController.criarVaga(dto)
            response.status = HttpServletResponse.SC_CREATED
        } catch (Exception e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "JSON inválido ou campos em formato incorreto, verifique se as competencias existem."])
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

        String idPath = request.pathInfo?.substring(1)

        if (!idPath || !(idPath ==~ /\d+/)) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID da vaga inválido."])
            )
            return
        }

        try {
            Long idVaga = idPath.toLong()
            Vaga vaga = vagaController.buscarVaga(idVaga)

            response.status = HttpServletResponse.SC_OK
            response.writer.write(JsonOutput.toJson(vaga))

        } catch (VagaNaoEncontradaException e) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([erro: "Vaga não encontrada."])
            )

        } catch (NumberFormatException e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID da vaga inválido."])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([erro: "Erro interno ao consultar vaga."])
            )
        }
    }

}
