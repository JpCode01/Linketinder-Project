package com.jpcode.dao.contratos

import com.jpcode.dto.Match.MatchEncontradoDTO

interface MatchRepository {
    
    void salvar(Long idCandidato, Long idEmpresa, Long idVaga)

    List<MatchEncontradoDTO> buscarMatchesPorEmpresa(Long idEmpresa)

    List<MatchEncontradoDTO> buscarMatchesPorCandidato(Long idCandidato)
}