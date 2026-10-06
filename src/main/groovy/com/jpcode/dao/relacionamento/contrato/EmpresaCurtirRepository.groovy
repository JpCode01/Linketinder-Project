package com.jpcode.dao.relacionamento.contrato

import com.jpcode.dto.candidato.CandidatoAnonimoDTO

interface EmpresaCurtirRepository {
    void salvar(Long idEmpresa, Long idCandidato)

    List<CandidatoAnonimoDTO> buscarCandidatosCurtidos(Long idEmpresa)
}