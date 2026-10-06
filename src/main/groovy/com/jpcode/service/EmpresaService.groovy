package com.jpcode.service

import com.jpcode.dao.empresa.contrato.EmpresaAutenticacao
import com.jpcode.dao.empresa.contrato.EmpresaDesativacao
import com.jpcode.dao.empresa.contrato.EmpresaRepository
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.dao.relacionamento.contrato.CandidatoCurtirConsulta
import com.jpcode.dao.relacionamento.contrato.EmpresaCurtirRepository
import com.jpcode.dao.vaga.contrato.CompetenciasVagaRepository
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.exception.empresa.EmpresaLoginException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Empresa
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class EmpresaService {
    final ReferenciaRepository<Pais> paisRepository
    final ReferenciaRepository<Estado> estadoRepository
    final EmpresaRepository empresaRepository
    final EmpresaAutenticacao empresaAutenticacao
    final EmpresaDesativacao empresaDesativacao
    final CandidatoCurtirConsulta candidatoCurtirConsulta
    final EmpresaCurtirRepository empresaCurtirRepository
    final CompetenciasVagaRepository competenciasVagaRepository
    final CompetenciaDAO competenciaDAO

    EmpresaService(ReferenciaRepository<Pais> paisRepository, ReferenciaRepository<Estado> estadoRepository, EmpresaRepository empresaRepository, EmpresaAutenticacao empresaAutenticacao, EmpresaDesativacao empresaDesativacao, CandidatoCurtirConsulta candidatoCurtirConsulta, EmpresaCurtirRepository empresaCurtirRepository, CompetenciasVagaRepository competenciasVagaRepository, CompetenciaDAO competenciaDAO) {
        this.paisRepository = paisRepository
        this.estadoRepository = estadoRepository
        this.empresaRepository = empresaRepository
        this.empresaAutenticacao = empresaAutenticacao
        this.empresaDesativacao = empresaDesativacao
        this.candidatoCurtirConsulta = candidatoCurtirConsulta
        this.empresaCurtirRepository = empresaCurtirRepository
        this.competenciasVagaRepository = competenciasVagaRepository
        this.competenciaDAO = competenciaDAO
    }

    Empresa cadastrarEmpresa(CadastrarEmpresaDTO cadastrarEmpresaDTO) {
       
        Long paisId = paisRepository.buscarIdPorNome(cadastrarEmpresaDTO.pais)
                .orElseThrow(() ->
                    new PaisNaoEncontradoException(cadastrarEmpresaDTO.pais)
                )
        Long estadoId = estadoRepository.buscarIdPorNome(cadastrarEmpresaDTO.estado)
                .orElseThrow(() ->
                    new EstadoNaoEncontradoException(cadastrarEmpresaDTO.estado)
                )
    
        Empresa empresa = new Empresa(
                cadastrarEmpresaDTO.nome,
                cadastrarEmpresaDTO.email,
                cadastrarEmpresaDTO.senha,
                cadastrarEmpresaDTO.cnpj,
                paisId,
                estadoId,
                cadastrarEmpresaDTO.cep,
                cadastrarEmpresaDTO.descricao
        )
        
        return empresaRepository.salvar(empresa)
    }

    Empresa logar(String email, String senha) {
        return empresaAutenticacao.buscarPorEmailESenha(email, senha)
        .orElseThrow(() ->
        new EmpresaLoginException(email, senha))
    }
    
    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga) {
        return candidatoCurtirConsulta.buscarCandidatosQueCurtiram(idVaga)
    }

    void curtirCandidato(Long idEmpresa,Long idCandidato) {
        empresaCurtirRepository.salvar(idEmpresa, idCandidato)
    }

    List<CandidatoAnonimoDTO> buscarCandidatosCurtidos(Long idEmpresa) {
        return empresaCurtirRepository.buscarCandidatosCurtidos(idEmpresa)
    }

    void desativarEmpresa(Long idEmpresa) {
        empresaDesativacao.desativar(idEmpresa)
    }

    void atualizarEmpresa(AtualizarEmpresaDTO atualizarEmpresaDTO) {
        Long paisId = paisRepository.buscarIdPorNome(atualizarEmpresaDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(atualizarEmpresaDTO.pais)
                )
        Long estadoId = estadoRepository.buscarIdPorNome(atualizarEmpresaDTO.estado)
                .orElseThrow(() ->
                        new EstadoNaoEncontradoException(atualizarEmpresaDTO.estado)
                )

        empresaRepository.atualizarDados(
                new Empresa(
                        atualizarEmpresaDTO.id,
                        atualizarEmpresaDTO.nome,
                        atualizarEmpresaDTO.email,
                        atualizarEmpresaDTO.senha,
                        atualizarEmpresaDTO.cnpj,
                        paisId,
                        estadoId,
                        atualizarEmpresaDTO.cep,
                        atualizarEmpresaDTO.descricao,
                        atualizarEmpresaDTO.ativo
                )
        )
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idVaga) {
        return competenciaDAO.converterCompetenciasParaDTO(competenciasVagaRepository.buscarPorVaga(idVaga))
    }
    
    void removerCompetencia(Long idVaga, Long idCompetencia) {
        competenciasVagaRepository.removerCompetencia(idVaga, idCompetencia)
    }
}
