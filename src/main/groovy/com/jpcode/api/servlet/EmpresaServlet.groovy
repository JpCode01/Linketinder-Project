package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.dto.empresa.EmpresaViewDTO
import com.jpcode.exception.empresa.EmpresaNaoEncontradaPorIdException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class EmpresaServlet extends HttpServlet {

    private final SessaoHttp sessaoHttp
    private final EmpresaController empresaController

    EmpresaServlet(EmpresaController empresaController, SessaoHttp sessaoHttp) {
        this.empresaController = empresaController
        this.sessaoHttp = sessaoHttp
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
            HttpServletResponse response
    ) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        String idPath = request.pathInfo?.substring(1)

        if (!idPath) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.writer.write(JsonOutput.toJson([erro: "Listagem de perfis não permitida."]))
            return
        }

        if (!autorizarPerfilProprio(idPath, request, response)) return
        buscarEmpresaPorId(idPath, response)
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
                    JsonOutput.toJson([erro: "ID da empresa inválido."])
            )
            return
        }

        if (!autorizarPerfilProprio(idPath, request, response)) return
        atualizarEmpresa(idPath, request, response)
    }

    private void atualizarEmpresa(
            String idPath,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            Map dados = new JsonSlurper().parse(request.inputStream) as Map

            AtualizarEmpresaDTO dto = new AtualizarEmpresaDTO(
                    idPath.toLong(),
                    dados.nome as String,
                    dados.email as String,
                    dados.senha as String,
                    dados.cnpj as String,
                    dados.pais as String,
                    dados.estado as String,
                    dados.cep as String,
                    dados.descricao as String,
                    dados.ativo as Boolean
            )

            empresaController.atualizarEmpresa(dto)

            response.status = HttpServletResponse.SC_OK
            response.writer.write(
                    JsonOutput.toJson([
                            mensagem: "Empresa atualizada com sucesso."
                    ])
            )

        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: e.message])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao atualizar empresa."
                    ])
            )
        }
    }


    private boolean autorizarPerfilProprio(String idPath, HttpServletRequest request, HttpServletResponse response) {
        try {
            Map ator = sessaoHttp.recuperarAtor(request)
            if (!(idPath ==~ /[0-9]+/) || idPath.toLong() <= 0) {
                response.status = 400
                response.writer.write(JsonOutput.toJson([erro: 'ID inválido.']))
                return false
            }
            if (ator.tipo != 'empresa' || ator.id != idPath.toLong()) {
                response.status = 403
                response.writer.write(JsonOutput.toJson([erro: 'Acesso restrito ao próprio perfil.']))
                return false
            }
            return true
        } catch (SecurityException ignored) {
            response.status = 401
            response.writer.write(JsonOutput.toJson([erro: 'Sessão ausente ou conta inativa.']))
        } catch (NumberFormatException ignored) {
            response.status = 400
            response.writer.write(JsonOutput.toJson([erro: 'ID inválido.']))
        } catch (Exception ignored) {
            response.status = 500
            response.writer.write(JsonOutput.toJson([erro: 'Erro interno ao validar acesso.']))
        }
        return false
    }

    private void buscarEmpresaPorId(
            String idPath,
            HttpServletResponse response
    ) {
        if (!(idPath ==~ /\d+/)) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID da empresa inválido."])
            )
            return
        }

        try {
            EmpresaViewDTO empresa =
                    empresaController.buscarEmpresa(idPath.toLong())

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
