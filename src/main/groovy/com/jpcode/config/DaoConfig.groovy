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
import com.jpcode.database.ConnectionFactory
import com.jpcode.database.PostgresConnectionFactory

class DaoConfig {

    final ConnectionFactory connectionFactory =
            new PostgresConnectionFactory()

    final PaisDAO paisDAO =
            new PaisDAO(connectionFactory)

    final EstadoDAO estadoDAO =
            new EstadoDAO(connectionFactory)

    final EmpresaDAO empresaDAO =
            new EmpresaDAO(connectionFactory)

    final CandidatoDAO candidatoDAO =
            new CandidatoDAO(connectionFactory)

    final CompetenciaDAO competenciaDAO =
            new CompetenciaDAO(connectionFactory)

    final CandidatoCurtirDAO candidatoCurtirDAO =
            new CandidatoCurtirDAO(connectionFactory)

    final EmpresaCurtirDAO empresaCurtirDAO =
            new EmpresaCurtirDAO(connectionFactory)

    final CompetenciasVagaDAO competenciasVagaDAO =
            new CompetenciasVagaDAO(connectionFactory)

    final CompetenciasCandidatoDAO competenciasCandidatoDAO =
            new CompetenciasCandidatoDAO(connectionFactory)

    final VagaDAO vagaDAO =
            new VagaDAO(connectionFactory)

    final MatchDAO matchDAO = new MatchDAO(
            competenciasCandidatoDAO,
            competenciasVagaDAO,
            connectionFactory
    )
}