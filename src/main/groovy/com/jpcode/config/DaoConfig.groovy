package com.jpcode.config

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.match.MatchDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.relacionamento.EmpresaCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO

class DaoConfig {
    final PaisDAO paisDAO = new PaisDAO()
    final EstadoDAO estadoDAO = new EstadoDAO()
    final EmpresaDAO empresaDAO = new EmpresaDAO()
    final CandidatoDAO candidatoDAO = new CandidatoDAO()
    final CompetenciaDAO competenciaDAO = new CompetenciaDAO()

    final CandidatoCurtirDAO candidatoCurtirDAO = new CandidatoCurtirDAO()
    final EmpresaCurtirDAO empresaCurtirDAO = new EmpresaCurtirDAO()

    final CompetenciasVagaDAO competenciasVagaDAO = new CompetenciasVagaDAO()
    final CompetenciasCandidatoDAO competenciasCandidatoDAO = new CompetenciasCandidatoDAO()

    final VagaDAO vagaDAO = new VagaDAO()

    final MatchDAO matchDAO = new MatchDAO(
            competenciasCandidatoDAO,
            competenciasVagaDAO
    )
}
