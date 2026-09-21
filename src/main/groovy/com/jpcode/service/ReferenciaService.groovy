package com.jpcode.service

import com.jpcode.dao.referencia.PaisDAO

class ReferenciaService {
    final PaisDAO paisDAO

    ReferenciaService(PaisDAO paisDAO) {
        this.paisDAO = paisDAO
    }

    String converterIdPaisParaString(Long idPais) {
        return paisDAO.buscarPorId(idPais).nome
    }
}
