package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.candidato.CandidatoViewDTO
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import java.time.LocalDate
import java.time.format.DateTimeParseException

class CandidatoServlet extends HttpServlet {

    private final SessaoHttp sessaoHttp
    private final CandidatoController candidatoController

    CandidatoServlet(CandidatoController candidatoController, SessaoHttp sessaoHttp) {
        this.candidatoController = candidatoController
        this.sessaoHttp = sessaoHttp
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        try {
            Map dados = new JsonSlurper().parse(request.inputStream)

            CadastrarCandidatoDTO dto = new CadastrarCandidatoDTO(
                    dados.nome as String,
                    dados.sobrenome as String,
                    dados.email as String,
                    dados.senha as String,
                    dados.cpf as String,
                    LocalDate.parse(dados.dataNascimento as String),
                    dados.pais as String,
                    dados.idade as int,
                    dados.estado as String,
                    dados.cep as String,
                    dados.descricao as String,
                    dados.competencias as List<String>
            )
            candidatoController.cadastrarCandidato(dto)
            response.status  = HttpServletResponse.SC_CREATED
        } catch (Exception e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "JSON inválido ou campos em formato incorreto, verifique se o Estado e País estão disponíveis."])
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
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.writer.write(JsonOutput.toJson([erro: "Listagem de perfis não permitida."]))
            return
        }

        if (!autorizarPerfilProprio(idPath, request, response)) return
        buscarCandidatoPorId(idPath, response)
    }

    private boolean autorizarPerfilProprio(String idPath, HttpServletRequest request, HttpServletResponse response) {
        try {
            Map ator = sessaoHttp.recuperarAtor(request)
            if (!(idPath ==~ /[0-9]+/) || idPath.toLong() <= 0) {
                response.status = 400
                response.writer.write(JsonOutput.toJson([erro: 'ID inválido.']))
                return false
            }
            if (ator.tipo != 'candidato' || ator.id != idPath.toLong()) {
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

    private void buscarCandidatoPorId(
            String idPath,
            HttpServletResponse response
    ) {
        if (!(idPath ==~ /\d+/)) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID do candidato inválido."])
            )
            return
        }

        try {
            CandidatoViewDTO candidato =
                    candidatoController.buscarCandidato(idPath.toLong())

            response.status = HttpServletResponse.SC_OK
            response.writer.write(JsonOutput.toJson(candidato))

        } catch (CandidatoNaoEncontradoPorIdException e) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([erro: "Candidato não encontrado."])
            )
        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([erro: "Erro interno ao consultar candidato."])
            )
        }
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
                    JsonOutput.toJson([erro: "ID do candidato inválido."])
            )
            return
        }

        if (!autorizarPerfilProprio(idPath, request, response)) return
        atualizarCandidato(idPath, request, response)
    }


    private void atualizarCandidato(
            String idPath,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            Map dados = new JsonSlurper().parse(request.inputStream) as Map

            AtualizarCandidatoDTO dto = new AtualizarCandidatoDTO(
                    idPath.toLong(),
                    dados.nome as String,
                    dados.sobrenome as String,
                    dados.email as String,
                    dados.senha as String,
                    dados.cpf as String,
                    LocalDate.parse(dados.dataNascimento as String),
                    dados.pais as String,
                    dados.idade as Integer,
                    dados.estado as String,
                    dados.cep as String,
                    dados.descricao as String,
                    dados.ativo as Boolean
            )

            candidatoController.atualizarCandidato(dto)

            response.status = HttpServletResponse.SC_OK
            response.writer.write(
                    JsonOutput.toJson([
                            mensagem: "Candidato atualizado com sucesso."
                    ])
            )

        } catch (CandidatoNaoEncontradoPorIdException e) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.write(
                    JsonOutput.toJson([erro: "Candidato não encontrado."])
            )

        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: e.message])
            )

        } catch (NumberFormatException | DateTimeParseException e) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write(
                    JsonOutput.toJson([erro: "ID, data ou campo numérico inválido."])
            )

        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([
                            erro: "Erro interno ao atualizar candidato."
                    ])
            )
        }
    }

}
