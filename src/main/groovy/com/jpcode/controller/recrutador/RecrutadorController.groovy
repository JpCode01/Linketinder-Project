package com.jpcode.controller.recrutador

import com.jpcode.dto.recrutador.CadastrarRecrutadorDTO
import com.jpcode.dto.recrutador.RecrutadorViewDTO
import com.jpcode.model.referencia.AreaRecrutamento
import com.jpcode.service.RecrutadorService
import com.jpcode.model.core.Recrutador

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

    Recrutador logar(String email, String senha) {
        return recrutadorService.logar(email, senha)
    }

    boolean estaAtivo(Long id) {
        return recrutadorService.estaAtivo(id)
    }

    List<AreaRecrutamento> buscarAreas() {
        return recrutadorService.buscarAreas()
    }
}
