package com.jpcode.dao.contratos

import com.jpcode.model.referencia.Competencia

interface ReferenciaCompetenciaRepository {
    Competencia buscarPorNome(String nome)
    List<String> listarCompetenciasEmString()
}