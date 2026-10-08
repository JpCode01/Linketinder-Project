package com.jpcode.controller.empresa

import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.model.core.Empresa
import com.jpcode.service.EmpresaService

class EmpresaController {

    final EmpresaService empresaService

    EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService
    }

    Empresa cadastrarEmpresa(CadastrarEmpresaDTO cadastrarEmpresaDTO) {
        return empresaService.cadastrarEmpresa(cadastrarEmpresaDTO)
    }

    Empresa logar(String email, String senha) {
        return empresaService.logar(email, senha)
    }

    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga) {
        return empresaService.buscarCandidatosQueCurtiram(idVaga)
    }

    void curtirCandidato(
            Long idEmpresa,
            Long idCandidato
    ) {
        empresaService.curtirCandidato(
                idEmpresa,
                idCandidato
        )
    }

    List<CandidatoAnonimoDTO> buscarCandidatosCurtidos(Long idEmpresa) {
        return empresaService.buscarCandidatosCurtidos(idEmpresa)
    }

    void desativarEmpresa(Long idEmpresa) {
        empresaService.desativarEmpresa(idEmpresa)
    }

    void atualizarEmpresa(AtualizarEmpresaDTO atualizarEmpresaDTO) {
        empresaService.atualizarEmpresa(atualizarEmpresaDTO)
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idVaga) {
        return empresaService.listaParaRemover(idVaga)
    }

    void removerCompetencia(
            Long idVaga,
            Long idCompetencia
    ) {
        empresaService.removerCompetencia(
                idVaga,
                idCompetencia
        )
    }
}