package com.jpcode.service


import com.jpcode.dao.contratos.MatchRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dao.contratos.RecrutadorRepository
import java.sql.Connection
import com.jpcode.dto.Match.MatchEncontradoDTO

class MatchService {
    final MatchRepository matchRepository
    private final UsuarioRepository candidatoRepository
    private final UsuarioRepository empresaRepository
    private final RecrutadorRepository recrutadorRepository

    MatchService(MatchRepository matchRepository, UsuarioRepository candidatoRepository = null,
                 UsuarioRepository empresaRepository = null, RecrutadorRepository recrutadorRepository = null) {
        this.matchRepository = matchRepository
        this.candidatoRepository = candidatoRepository
        this.empresaRepository = empresaRepository
        this.recrutadorRepository = recrutadorRepository
    }

    void salvarNaTransacao(Connection conexao, Long idCandidato, Long idEmpresa, Long idVaga) {
        matchRepository.salvar(conexao, idCandidato, idEmpresa, idVaga)
    }

    List<MatchEncontradoDTO> verMatchesPorEmpresa(Long idEmpresa) {
        return consultarMatchesEmpresa(idEmpresa, idEmpresa)
    }

    List<MatchEncontradoDTO> verMatchesPorCandidato(Long idCandidato) {
        return consultarMatchesCandidato(idCandidato, idCandidato)
    }

    List<MatchEncontradoDTO> consultarMatchesEmpresa(Long idEmpresaOperante, Long idEmpresa) {
        validarTitular(idEmpresaOperante, idEmpresa)
        validarAtividade(empresaRepository, idEmpresaOperante)
        return matchRepository.buscarMatchesPorEmpresa(idEmpresaOperante)
    }

    List<MatchEncontradoDTO> consultarMatchesCandidato(Long idCandidatoOperante, Long idCandidato) {
        validarTitular(idCandidatoOperante, idCandidato)
        validarAtividade(candidatoRepository, idCandidatoOperante)
        return matchRepository.buscarMatchesPorCandidato(idCandidatoOperante)
    }

    List<MatchEncontradoDTO> consultarMatchesRecrutador(Long idRecrutadorOperante, Long idRecrutador) {
        validarTitular(idRecrutadorOperante, idRecrutador)
        def recrutador = recrutadorRepository?.buscarPorId(idRecrutadorOperante)?.orElse(null)
        if (recrutador == null || !recrutador.ativo) throw new SecurityException('Titular inexistente ou inativo.')
        validarAtividade(empresaRepository, recrutador.idEmpresa)
        return matchRepository.buscarMatchesPorRecrutador(idRecrutadorOperante)
    }

    private static void validarTitular(Long operante, Long titular) {
        if (operante == null || operante <= 0 || operante != titular) {
            throw new SecurityException('Consulta restrita ao próprio titular.')
        }
    }

    private static void validarAtividade(UsuarioRepository repository, Long id) {
        if (!repository?.buscarPorId(id)?.map { it.ativo }?.orElse(false)) {
            throw new SecurityException('Titular inexistente ou inativo.')
        }
    }

}
