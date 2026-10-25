package com.jpcode.dao.contratos

import com.jpcode.dto.candidato.CandidatoAnonimoDTO

interface CandidatoCurtidasConsultaRepository {
    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga)
}