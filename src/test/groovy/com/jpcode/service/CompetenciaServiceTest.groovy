package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import spock.lang.Specification
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException

class CompetenciaServiceTest extends Specification {

    CompetenciaDAO competenciaDAO = Mock()

    CompetenciaService service

    def setup() {
        service = new CompetenciaService(competenciaDAO)
    }

    def "deve listar todas as competencias"() {
        given:
        List<String> competencias = ["JAVA", "SPRING", "GROOVY"]

        competenciaDAO.listarTodasCompetencias() >> competencias

        expect:
        service.listarCompetencias() == competencias
    }

    def "deve lançar exceção quando competencia não for encontrada pelo id"() {
        given:
        competenciaDAO.buscarPorId(1L) >> Optional.empty()

        when:
        service.buscarCompetencia(1L)

        then:
        thrown(CompetenciaNaoEncontradaException)
    }
}