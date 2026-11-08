package com.jpcode.controller.recrutador

import com.jpcode.dto.recrutador.CadastrarRecrutadorDTO
import com.jpcode.dto.recrutador.RecrutadorViewDTO
import com.jpcode.model.referencia.AreaRecrutamento
import com.jpcode.service.RecrutadorService

class RecrutadorController {
    private final RecrutadorService recrutadorService

    RecrutadorController(RecrutadorService recrutadorService) {
        this.recrutadorService = recrutadorService
    }

    RecrutadorViewDTO cadastrarRecrutador(Long idEmpresaOperante, CadastrarRecrutadorDTO dados) {
        return recrutadorService.cadastrarRecrutador(idEmpresaOperante, dados)
    }

    RecrutadorViewDTO buscarRecrutador(Long idRecrutadorOperante, Long idRecrutador) {
        return recrutadorService.buscarRecrutador(idRecrutadorOperante, idRecrutador)
    }

    List<AreaRecrutamento> buscarAreas() {
        return recrutadorService.buscarAreas()
    }
}
