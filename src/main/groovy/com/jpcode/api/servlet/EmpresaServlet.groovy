package com.jpcode.api.servlet

import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.dto.empresa.EmpresaProfileViewDTO
import com.jpcode.exception.empresa.EmpresaNaoEncontradaPorIdException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class EmpresaServlet extends HttpServlet {

    private final EmpresaController empresaController

    EmpresaServlet(EmpresaController empresaController) {
        this.empresaController = empresaController
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        try {
            Map dados = new JsonSlurper().parse(request.inputStream)

            CadastrarEmpresaDTO dto = new CadastrarEmpresaDTO(
                    dados.nome as String,
                    dados.email as String,
                    dados.senha as String,
                    dados.cnpj as String,
                    dados.pais as String,
                    dados.estado as String,
                    dados.cep as String,
                    dados.descricao as String
            )

            empresaController.cadastrarEmpresa(dto)

            response.status = HttpServletResponse.SC_CREATED
        } catch (Exception e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write([erro: "JSON inválido ou campos em formato incorreto, verifique se o Estado e País estão disponíveis."])
        }
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) {

        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        String idPath = request.pathInfo?.substring(1)

        if (!idPath || !(idPath ==~ /\d+/)) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID da empresa inválido."])
            )
            return
        }

        try {
            Long idEmpresa = idPath.toLong()

            EmpresaProfileViewDTO empresa =
                    empresaController.buscarEmpresa(idEmpresa)

            response.status = HttpServletResponse.SC_OK
            response.writer.write(JsonOutput.toJson(empresa))

        } catch (EmpresaNaoEncontradaPorIdException e) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([erro: "Empresa não encontrada."])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([erro: "Erro interno ao consultar empresa."])
            )
        }
    }

}
