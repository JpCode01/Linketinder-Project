package com.jpcode.service

import com.jpcode.dao.contratos.CompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dto.candidato.*
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.exception.candidato.CandidatoLoginException
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Competencia
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class CandidatoService {
    final ReferenciaRepository<Pais> paisRepository
    final ReferenciaRepository<Estado> estadoRepository
    final ReferenciaRepository<Competencia> competenciaRepository
    final ReferenciaCompetenciaRepository referenciaCompetenciaRepository
    final CompetenciaRepository competenciasCandidatoRepository
    final UsuarioRepository<Candidato, CandidatoComReferenciasDTO, CandidatoViewListDTO> candidatoRepository

    CandidatoService(ReferenciaRepository<Pais> paisRepository, ReferenciaRepository<Estado> estadoRepository, ReferenciaRepository<Competencia> competenciaRepository, ReferenciaCompetenciaRepository referenciaCompetenciaRepository, CompetenciaRepository competenciasCandidatoRepository, UsuarioRepository<Candidato, CandidatoComReferenciasDTO, CandidatoViewListDTO> candidatoRepository) {
        this.paisRepository = paisRepository
        this.estadoRepository = estadoRepository
        this.competenciaRepository = competenciaRepository
        this.referenciaCompetenciaRepository = referenciaCompetenciaRepository
        this.competenciasCandidatoRepository = competenciasCandidatoRepository
        this.candidatoRepository = candidatoRepository
    }

    void cadastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        Long paisId = paisRepository.buscarIdPorNome(cadastrarCandidatoDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(cadastrarCandidatoDTO.pais)
                )
        Long estadoId = estadoRepository.buscarIdPorNome(cadastrarCandidatoDTO.estado)
                .orElseThrow(() ->
                new EstadoNaoEncontradoException(cadastrarCandidatoDTO.estado)
        )

        Candidato candidatoSalvo = candidatoRepository.salvar(
                    new Candidato(
                            cadastrarCandidatoDTO.nome,
                            cadastrarCandidatoDTO.sobrenome,
                            cadastrarCandidatoDTO.email,
                            cadastrarCandidatoDTO.senha,
                            cadastrarCandidatoDTO.cpf,
                            cadastrarCandidatoDTO.dataNascimento,
                            paisId,
                            cadastrarCandidatoDTO.idade,
                            estadoId,
                            cadastrarCandidatoDTO.cep,
                            cadastrarCandidatoDTO.descricao
                    )
        )
        salvarCompetencias(cadastrarCandidatoDTO.competencias, candidatoSalvo.id)
    }

    private salvarCompetencias(List<String> competencias, Long idCandidato) {
        competencias.each {
            competencia ->
                Long idCompetencia = competenciaRepository
                        .buscarIdPorNome(competencia)
                        .orElseThrow(() ->
                                new CompetenciaNaoEncontradaException(competencia)
                        )

                competenciasCandidatoRepository.salvar(
                        idCandidato,
                        idCompetencia
                )
        }
    }

    CandidatoViewDTO buscarCandidato(Long idCandidato) {
        CandidatoComReferenciasDTO resultado = candidatoRepository
                .buscarPorIdComReferencias(idCandidato)
                .orElseThrow(() ->
                        new CandidatoNaoEncontradoPorIdException(idCandidato)
                )

        List<Competencia> competencias =
                competenciasCandidatoRepository.buscarPorId(idCandidato)

        Candidato candidato = resultado.candidato

        return new CandidatoViewDTO(
                candidato.id,
                candidato.nome,
                candidato.sobrenome,
                candidato.email,
                resultado.estado,
                candidato.cep,
                candidato.descricao,
                resultado.pais,
                candidato.idade,
                candidato.dataNascimento,
                competencias
        )
    }
    
    Candidato logar(String email, String senha) {
        return candidatoRepository.buscarPorEmailESenha(email, senha)
        .orElseThrow(() ->
        new CandidatoLoginException(email, senha))
    }

    void desativarCandidato(Long idCandidato) {
        candidatoRepository.desativar(idCandidato)
    }
    
    void atualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        Long paisId = paisRepository.buscarIdPorNome(atualizarCandidatoDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(atualizarCandidatoDTO.pais)
                )
        Long estadoId = estadoRepository.buscarIdPorNome(atualizarCandidatoDTO.estado)
                .orElseThrow(() ->
                        new EstadoNaoEncontradoException(atualizarCandidatoDTO.estado)
                )

        candidatoRepository.atualizarDados(
                new Candidato(
                        atualizarCandidatoDTO.id,
                        atualizarCandidatoDTO.nome,
                        atualizarCandidatoDTO.sobrenome,
                        atualizarCandidatoDTO.email,
                        atualizarCandidatoDTO.senha,
                        atualizarCandidatoDTO.cpf,
                        atualizarCandidatoDTO.dataNascimento,
                        paisId,
                        atualizarCandidatoDTO.idade,
                        estadoId,
                        atualizarCandidatoDTO.cep,
                        atualizarCandidatoDTO.descricao,
                        atualizarCandidatoDTO.ativo
                ))
    }
    
    void adicionarCompetencias(Long idCandidato, List<String> competenciasNovas) {
        List<Competencia> converterParaCompetencia = []
        competenciasNovas.each {competenciaString ->
            converterParaCompetencia.add(
                    referenciaCompetenciaRepository.buscarPorNome(competenciaString))
        }
        competenciasCandidatoRepository.atualizar(idCandidato, converterParaCompetencia)
    }

    void removerCompetencia(Long idCandidato, Long idCompetencia) {
        competenciasCandidatoRepository.removerCompetencia(idCandidato, idCompetencia)
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idCandidato) {
        return converterCompetenciasParaDTO(competenciasCandidatoRepository.buscarPorId(idCandidato))
    }

    List<String> competenciasEmString(Long idCandidato) {
        return competenciasCandidatoRepository
                .buscarEmString(idCandidato)
    }

    List<CandidatoViewListDTO> buscarCandidatos() {
        return candidatoRepository.buscar().collect { candidato ->
            List<Competencia> competencias =
                    competenciasCandidatoRepository.buscarPorId(candidato.id)

            return new CandidatoViewListDTO(
                    candidato.id,
                    candidato.nome,
                    candidato.sobrenome,
                    candidato.email,
                    competencias
            )
        }
    }

    private List<RemoverCompetenciaDTO> converterCompetenciasParaDTO(List<Competencia> competencias) {
        List<RemoverCompetenciaDTO> competenciasConvertidas = []
        competencias.each { competencia ->
            competenciasConvertidas.add(
                    new RemoverCompetenciaDTO(competencia.id,
                            competencia.nome
                    ))
        }

        return competenciasConvertidas
    }
}