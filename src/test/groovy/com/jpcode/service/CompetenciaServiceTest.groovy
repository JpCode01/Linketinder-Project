package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.model.referencia.Competencia
import spock.lang.Specification

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

    def "deve buscar competencia pelo id"() {
        given:
        Competencia competencia = new Competencia(
                1L,
                "JAVA"
        )

        competenciaDAO.buscarPorId(1L) >> competencia

        expect:
        service.buscarCompetencia(1L) == competencia
    }
}