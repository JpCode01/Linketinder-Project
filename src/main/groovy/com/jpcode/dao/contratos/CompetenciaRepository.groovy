package com.jpcode.dao.contratos

import com.jpcode.model.referencia.Competencia

interface CompetenciaRepository {

    void salvar(Long idRegistro, Long idCompetencia)

    List<Competencia> buscarPorId(Long idRegistro)

    List<String> buscarEmString(Long id)

    void atualizar(Long idRegistro, List<Competencia> competencias)

    void removerCompetencia(Long idRegistro, Long idCompetencia)
}
