package com.jpcode.service

import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais
import spock.lang.Specification

class ReferenciaServiceTest extends Specification {

    ReferenciaRepository<Pais> paisRepository = Mock()
    ReferenciaRepository<Estado> estadoRepository = Mock()

    ReferenciaService service

    def setup() {
        service = new ReferenciaService(
                paisRepository,
                estadoRepository
        )
    }

    def "deve converter id do pais para nome"() {
        given:
        Pais pais = new Pais(
                1L,
                "BRASIL"
        )

        paisRepository.buscarPorId(1L) >> Optional.of(pais)

        expect:
        service.converterIdPaisParaString(1L) == "BRASIL"
    }

    def "deve converter id do estado para sigla"() {
        given:
        Estado estado = new Estado(
                1L,
                "SP"
        )

        estadoRepository.buscarPorId(1L) >> Optional.of(estado)

        expect:
        service.converterIdEstadoParaString(1L) == "SP"
    }
}