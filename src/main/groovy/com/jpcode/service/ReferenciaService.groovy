package com.jpcode.service

import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO

class ReferenciaService {
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO

    ReferenciaService(PaisDAO paisDAO, EstadoDAO estadoDAO) {
        this.paisDAO = paisDAO
        this.estadoDAO = estadoDAO
    }

    String converterIdPaisParaString(Long idPais) {
        return paisDAO.buscarPorId(idPais).nome
    }

    String converterIdEstadoParaString(Long idEstado) {
        return estadoDAO.buscarPorId(idEstado).sigla
    }


}
