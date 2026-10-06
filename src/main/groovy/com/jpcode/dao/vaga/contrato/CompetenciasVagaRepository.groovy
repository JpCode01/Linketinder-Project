package com.jpcode.dao.vaga.contrato

import com.jpcode.model.referencia.Competencia

interface CompetenciasVagaRepository {

    void salvar(Long idVaga, Long idCompetencia)

    void atualizar(Long idVaga, List<Competencia> competencias)

    void removerCompetencia(Long idVaga, Long idCompetencia)

    List<String> buscarPorVagaString(Long idVaga)

    List<Competencia> buscarPorVaga(Long idVaga)
}