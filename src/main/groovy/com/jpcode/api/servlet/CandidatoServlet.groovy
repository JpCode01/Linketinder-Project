package com.jpcode.api.servlet

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class CandidatoServlet extends HttpServlet {

    private final CandidatoController candidatoController

    CandidatoServlet(CandidatoController candidatoController) {
        this.candidatoController = candidatoController
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


}
