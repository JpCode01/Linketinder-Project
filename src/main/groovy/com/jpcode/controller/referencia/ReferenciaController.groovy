package com.jpcode.controller.referencia

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
}