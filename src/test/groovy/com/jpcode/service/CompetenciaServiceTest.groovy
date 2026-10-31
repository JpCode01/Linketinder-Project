package com.jpcode.service

import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.model.referencia.Competencia
import spock.lang.Specification

class CompetenciaServiceTest extends Specification {

    ReferenciaCompetenciaRepository referenciaCompetenciaRepository = Mock()
    ReferenciaRepository<Competencia> referenciaRepository = Mock()

    CompetenciaService service

    def setup() {
        service = new CompetenciaService(
                referenciaCompetenciaRepository,
                referenciaRepository
        )
    }

    def "deve listar todas as competencias"() {
        given:
        List<String> competencias = ["JAVA", "SPRING", "GROOVY"]

        referenciaCompetenciaRepository.listarTodasCompetencias() >> competencias

        expect:
        service.listarCompetenciasEmString() == competencias
    }

    def "deve lançar exceção quando competencia não for encontrada pelo id"() {
        given:
        referenciaRepository.buscarPorId(1L) >> Optional.empty()

        when:
        service.buscarCompetencia(1L)

        then:
        thrown(CompetenciaNaoEncontradaException)
    }
}