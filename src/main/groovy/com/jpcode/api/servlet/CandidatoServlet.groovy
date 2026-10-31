package com.jpcode.api.servlet

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.candidato.CandidatoViewDTO
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
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

    private final CandidatoController candidatoController

    CandidatoServlet(CandidatoController candidatoController) {
        this.candidatoController = candidatoController
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        try {
            CadastrarCandidatoDTO dto = criarCadastrarCandidatoDTO(request)

            candidatoController.cadastrarCandidato(dto)

            response.status = HttpServletResponse.SC_CREATED

        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException | CompetenciaNaoEncontradaException e) {
            responderErro(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.message
            )
        } catch (DateTimeParseException e) {
            responderErro(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Data de nascimento em formato inválido."
            )

        } catch (NumberFormatException e) {
            responderErro(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Idade em formato inválido."
            )

        } catch (Exception e) {
            responderErro(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro interno ao cadastrar candidato."
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
            listarCandidatos(response)
            return
        }

        buscarCandidatoPorId(idPath, response)
    }

    private void listarCandidatos(HttpServletResponse response) {
        try {
            response.status = HttpServletResponse.SC_OK
            response.writer.write(
                    JsonOutput.toJson(candidatoController.buscarCandidatos())
            )
        } catch (Exception e) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(
                    JsonOutput.toJson([erro: "Erro interno ao listar candidatos."])
            )
        }
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

    private CadastrarCandidatoDTO criarCadastrarCandidatoDTO(
            HttpServletRequest request
    ) {
        Map dados = new JsonSlurper().parse(request.inputStream) as Map

        return new CadastrarCandidatoDTO(
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
    }

    private void responderErro(
            HttpServletResponse response,
            int status,
            String mensagem
    ) {
        response.status = status
        response.writer.write(
                JsonOutput.toJson([erro: mensagem])
        )
    }
}
