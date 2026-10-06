package com.jpcode.service

import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class ReferenciaService {
    final ReferenciaRepository<Pais> paisRepository
    final ReferenciaRepository<Estado> estadoRepository

    ReferenciaService(
            ReferenciaRepository<Pais> paisRepository,
            ReferenciaRepository<Estado> estadoRepository
    ) {
        this.paisRepository = paisRepository
        this.estadoRepository = estadoRepository
    }

    String converterIdPaisParaString(Long idPais) {
        return paisRepository.buscarPorId(idPais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(idPais))
                .nome
    }

    String converterIdEstadoParaString(Long idEstado) {
        return estadoRepository.buscarPorId(idEstado)
                .orElseThrow(() ->
                        new EstadoNaoEncontradoException(idEstado))
                .sigla
    }
}