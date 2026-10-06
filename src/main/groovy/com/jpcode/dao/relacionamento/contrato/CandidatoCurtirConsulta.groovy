package com.jpcode.dao.relacionamento.contrato

import com.jpcode.dto.candidato.CandidatoAnonimoDTO

interface CandidatoCurtirConsulta {
    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga)

}