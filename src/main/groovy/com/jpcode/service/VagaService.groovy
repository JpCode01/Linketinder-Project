package com.jpcode.service

import com.jpcode.dao.contratos.*
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

class VagaService {

    final VagaRepository vagaRepository
    final ReferenciaRepository<Competencia> referenciaRepository
    final ReferenciaCompetenciaRepository referenciaCompetencia
    final CompetenciaRepository competenciaRepository
    final CurtidaRepository<VagaAnonimaDTO> curtidaRepository
    final UsuarioRepository empresaRepository
    final RecrutadorRepository recrutadorRepository

    VagaService(VagaRepository vagaRepository, ReferenciaRepository<Competencia> referenciaRepository, ReferenciaCompetenciaRepository referenciaCompetencia, CompetenciaRepository competenciaRepository, CurtidaRepository<VagaAnonimaDTO> curtidaRepository, UsuarioRepository empresaRepository, RecrutadorRepository recrutadorRepository) {
        this.vagaRepository = vagaRepository
        this.referenciaRepository = referenciaRepository
        this.referenciaCompetencia = referenciaCompetencia
        this.competenciaRepository = competenciaRepository
        this.curtidaRepository = curtidaRepository
        this.empresaRepository = empresaRepository
        this.recrutadorRepository = recrutadorRepository
    }

    void criarVaga(CadastrarVagaDTO cadastrarVagaDTO) {
        criarVagaEmpresa(cadastrarVagaDTO.idEmpresa, cadastrarVagaDTO)
    }

    // O contexto HTTP fornecerá o ator separadamente do corpo da requisição.
    void criarVagaEmpresa(Long idEmpresaOperante, CadastrarVagaDTO dados) {
        validarEmpresa(idEmpresaOperante)
        validarEmpresaInformada(dados, idEmpresaOperante)
        gravarVaga(dados, idEmpresaOperante, null)
    }

    void criarVagaRecrutador(Long idRecrutadorOperante, CadastrarVagaDTO dados) {
        def recrutador = validarRecrutador(idRecrutadorOperante)
        validarEmpresa(recrutador.idEmpresa)
        validarEmpresaInformada(dados, recrutador.idEmpresa)
        gravarVaga(dados, recrutador.idEmpresa, recrutador.id)
    }

    private static void validarEmpresaInformada(CadastrarVagaDTO dados, Long idEmpresa) {
        if (dados.idEmpresa != null && dados.idEmpresa != idEmpresa) {
            throw new IllegalArgumentException('Empresa incompatível com o ator.')
        }
    }

    private void validarEmpresa(Long id) {
        if (id == null || !empresaRepository.buscarPorId(id).map { it.ativo }.orElse(false)) {
            throw new IllegalArgumentException('Empresa inexistente ou inativa.')
        }
    }

    private def validarRecrutador(Long id) {
        if (id == null) throw new IllegalArgumentException('Recrutador obrigatório.')
        def recrutador = recrutadorRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException('Recrutador inexistente.'))
        if (!recrutador.ativo) throw new IllegalStateException('Recrutador inativo.')
        return recrutador
    }

    private void gravarVaga(CadastrarVagaDTO dados, Long idEmpresa, Long idRecrutador) {
        if (dados.competencias == null) throw new IllegalArgumentException('Competências obrigatórias.')
        List<Long> ids = dados.competencias.collect { nome ->
            referenciaRepository.buscarIdPorNome(nome)
                    .orElseThrow(() -> new CompetenciaNaoEncontradaException(nome))
        }.unique()
        Vaga vaga = new Vaga(dados.nome, dados.descricao, dados.local, idEmpresa)
        vaga.idRecrutador = idRecrutador
        vagaRepository.salvarComCompetencias(vaga, ids)
    }

    void curtir(Long idCandidato, Long idVaga) {
        curtidaRepository.salvar(idCandidato, idVaga)
    }

    List<VagaEmpresaDTO> listarVagas(Long idEmpresa) {
        validarEmpresa(idEmpresa)
        return vagaRepository.buscarVagasEmpresa(idEmpresa)
    }

    List<VagaEmpresaDTO> listarVagasEmpresa(Long idEmpresaOperante, Long idEmpresa) {
        if (idEmpresaOperante == null || idEmpresaOperante != idEmpresa) {
            throw new IllegalArgumentException('Consulta restrita à própria empresa.')
        }
        return listarVagas(idEmpresaOperante)
    }

    List<VagaEmpresaDTO> listarVagasRecrutador(Long idRecrutadorOperante, Long idRecrutador) {
        if (idRecrutadorOperante == null || idRecrutadorOperante != idRecrutador) {
            throw new IllegalArgumentException('Consulta restrita ao próprio recrutador.')
        }
        def recrutador = validarRecrutador(idRecrutadorOperante)
        validarEmpresa(recrutador.idEmpresa)
        return vagaRepository.buscarVagasRecrutador(idRecrutadorOperante)
    }

    Vaga buscarVaga(Long idVaga) {
        return vagaRepository.buscarPorId(idVaga)
                .orElseThrow(() ->
                        new VagaNaoEncontradaException(idVaga))
    }

    List<VagaAnonimaDTO> listarVagasCurtidas(long idCandidato) {
        return curtidaRepository.buscarCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> buscarTodasAsVagas() {
        return vagaRepository.buscarTodasAsVagas()
    }

    List<Competencia> buscarCompetenciasDeVaga(Long idVaga) {
        return competenciaRepository.buscarPorId(idVaga)
    }

    void deletarVaga(Long idVaga) {
        vagaRepository.deletar(idVaga)
    }

    void atualizarVaga(AtualizarVagaDTO atualizarVagaDTO) {
        validarEmpresa(atualizarVagaDTO.idEmpresa)
        Vaga persistida = buscarVaga(atualizarVagaDTO.id)
        if (persistida.idEmpresa != atualizarVagaDTO.idEmpresa) {
            throw new IllegalArgumentException('Vaga fora do escopo da empresa.')
        }
        vagaRepository.atualizarDados(
                new Vaga(
                        atualizarVagaDTO.id,
                        atualizarVagaDTO.nome,
                        atualizarVagaDTO.descricao,
                        atualizarVagaDTO.local,
                        atualizarVagaDTO.idEmpresa
                )
        )
    }

    List<String> competenciasEmString(Long idVaga) {
        return competenciaRepository.buscarEmString(idVaga)
    }

    void adicionarCompetencias(Long idVaga, List<String> competenciasNovas) {
        List<Competencia> converterParaCompetencia = []
        competenciasNovas.each { competenciaString ->
            converterParaCompetencia.add(
                    referenciaCompetencia.buscarPorNome(competenciaString))
        }
        competenciaRepository.atualizar(idVaga, converterParaCompetencia)
    }
}
