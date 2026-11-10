package com.jpcode.dao.contratos

import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import java.sql.Connection

interface CandidatoCurtidasConsultaRepository {
    List<Long> buscarIdsCandidatosRetribuidos(Connection conexao, Long idVaga)
    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Connection conexao, Long idVaga)
}
