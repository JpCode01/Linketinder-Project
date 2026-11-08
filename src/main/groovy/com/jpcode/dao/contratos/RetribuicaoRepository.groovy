package com.jpcode.dao.contratos

import com.jpcode.model.core.Vaga
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import java.sql.Connection

/** Operações participantes da transação da fachada; não fecham a conexão recebida. */
interface RetribuicaoRepository {
    Long buscarEmpresaDaVaga(Connection conexao, Long idVaga)
    boolean bloquearEmpresaAtiva(Connection conexao, Long idEmpresa)
    Long bloquearRecrutadorAtivo(Connection conexao, Long idRecrutador)
    boolean bloquearCandidatoAtivo(Connection conexao, Long idCandidato)
    Vaga bloquearVaga(Connection conexao, Long idVaga)
    boolean bloquearInteresse(Connection conexao, Long idCandidato, Long idVaga)
    void salvar(Connection conexao, Long idEmpresa, Long idCandidato, Long idVaga, Long idRecrutadorAtor)
    List<CandidatoAnonimoDTO> buscarCurtidas(Long idEmpresa)
}
