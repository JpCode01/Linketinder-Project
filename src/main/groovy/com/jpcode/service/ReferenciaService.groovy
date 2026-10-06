package com.jpcode.service

import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException

class ReferenciaService {
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO

    ReferenciaService(PaisDAO paisDAO, EstadoDAO estadoDAO) {
        this.paisDAO = paisDAO
        this.estadoDAO = estadoDAO
    }

    String converterIdPaisParaString(Long idPais) {
        return paisDAO.buscarPorId(idPais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(idPais))
                .nome
    }

    String converterIdEstadoParaString(Long idEstado) {
        return estadoDAO.buscarPorId(idEstado)
                .orElseThrow(() ->
                        new EstadoNaoEncontradoException(idEstado))
                .sigla
    }
}
