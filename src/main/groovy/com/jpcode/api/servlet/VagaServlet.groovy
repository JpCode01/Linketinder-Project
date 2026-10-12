package com.jpcode.api.servlet

import com.jpcode.controller.vaga.VagaController
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import groovy.json.JsonException
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
            Map dados = new JsonSlurper().parse(request.inputStream) as Map

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

        if (!idPath) {
            listarVagas(response)
            return
        }

        buscarVagaPorId(idPath, response)
    }


    @Override
    protected void doPut(
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

        atualizarVaga(idPath, request, response)
    }

    private void atualizarVaga(
            String idPath,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            Map dados = new JsonSlurper().parse(request.inputStream) as Map

            AtualizarVagaDTO dto = new AtualizarVagaDTO(
                    idPath.toLong(),
                    dados.nome as String,
                    dados.descricao as String,
                    dados.local as String,
                    dados.idEmpresa as Long
            )

            vagaController.atualizarVaga(dto)

            response.status = HttpServletResponse.SC_OK
            response.writer.write(
                    JsonOutput.toJson([
                            mensagem: "Vaga atualizada com sucesso."
                    ])
            )

        } catch (NumberFormatException e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID da vaga ou ID da empresa inválido."])
            )

        } catch (JsonException e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "JSON inválido."])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao atualizar vaga."
                    ])
            )
        }
    }


    private void listarVagas(HttpServletResponse response) {
        try {
            response.status = HttpServletResponse.SC_OK
            response.writer.write(
                    JsonOutput.toJson(vagaController.buscarTodasAsVagas())
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao listar vagas."
                    ])
            )
        }
    }

    private void buscarVagaPorId(
            String idPath,
            HttpServletResponse response
    ) {
        if (!(idPath ==~ /\d+/)) {
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
                    JsonOutput.toJson([
                            erro: "Erro interno ao consultar vaga."
                    ])
            )
        }
    }

}
