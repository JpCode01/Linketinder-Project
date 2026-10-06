package com.jpcode.dao.relacionamento.contrato

import com.jpcode.dto.vaga.VagaAnonimaDTO

interface CandidatoCurtirRepository {
    void salvar(Long idCandidato, Long idVaga)

    List<VagaAnonimaDTO> buscarVagasCurtidas(Long idCandidato)
}