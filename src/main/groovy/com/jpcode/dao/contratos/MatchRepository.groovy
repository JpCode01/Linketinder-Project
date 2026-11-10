package com.jpcode.dao.contratos

import com.jpcode.dto.Match.MatchEncontradoDTO
import java.sql.Connection

interface MatchRepository {
    
    void salvar(Connection conexao, Long idCandidato, Long idEmpresa, Long idVaga)

    List<MatchEncontradoDTO> buscarMatchesPorEmpresa(Long idEmpresa)

    List<MatchEncontradoDTO> buscarMatchesPorCandidato(Long idCandidato)

    List<MatchEncontradoDTO> buscarMatchesPorRecrutador(Long idRecrutador)
}
