package com.jpcode.service

import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais
import spock.lang.Specification

class ReferenciaServiceTest extends Specification {

    PaisDAO paisDAO = Mock()
    EstadoDAO estadoDAO = Mock()

    ReferenciaService service

    def setup() {
        service = new ReferenciaService(
                paisDAO,
                estadoDAO
        )
    }

    def "deve converter id do pais para nome"() {
        given:
        Pais pais = new Pais(
                1L,
                "BRASIL"
        )

        paisDAO.buscarPorId(1L) >> pais

        expect:
        service.converterIdPaisParaString(1L) == "BRASIL"
    }

    def "deve converter id do estado para sigla"() {
        given:
        Estado estado = new Estado(
                1L,
                "SP"
        )

        estadoDAO.buscarPorId(1L) >> estado

        expect:
        service.converterIdEstadoParaString(1L) == "SP"
    }
}