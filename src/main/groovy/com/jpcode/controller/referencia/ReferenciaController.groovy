package com.jpcode.controller.referencia

import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais
import com.jpcode.service.ReferenciaService

class ReferenciaController {

    final ReferenciaService referenciaService

    ReferenciaController(ReferenciaService referenciaService) {
        this.referenciaService = referenciaService
    }

    String converterIdPaisParaString(Long idPais) {
        return referenciaService.converterIdPaisParaString(idPais)
    }

    String converterIdEstadoParaString(Long idEstado) {
        return referenciaService.converterIdEstadoParaString(idEstado)
    }

    List<Pais> listarPaises() {
        return referenciaService.listarPaises()
    }

    List<Estado> listarEstados() {
        return referenciaService.listarEstados()
    }
}