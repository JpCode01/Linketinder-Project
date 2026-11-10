package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.vaga.VagaController
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

class VagaServletTest extends Specification {
    def 'GET por ID não serializa entidade nem expõe propriedade ou autoria'() {
        given:
        VagaController controller = Mock()
        HttpServletRequest request = Mock()
        HttpServletResponse response = Mock()
        StringWriter corpo = new StringWriter()
        request.pathInfo >> '/7'
        response.writer >> new PrintWriter(corpo)
        Vaga vaga = new Vaga(7L, 'Nome interno', 'Descrição', 'Local interno', 10L)
        vaga.idRecrutador = 20L
        controller.buscarVaga(7L) >> vaga
        controller.buscarCompetenciasDeVaga(7L) >> [new Competencia(1L, 'RH')]

        when:
        new VagaServlet(controller, Stub(SessaoHttp, constructorArgs: [null, null, null]) { recuperarAtor(_) >> [id: 1L, tipo: 'candidato'] }).doGet(request, response)
        def json = new JsonSlurper().parseText(corpo.toString())

        then:
        1 * response.setStatus(200)
        json.keySet() == ['id', 'descricao', 'competencias'] as Set
        json.id == 7
        json.descricao == 'Descrição'
        json.competencias == [[id: 1, nome: 'RH']]
    }
}
