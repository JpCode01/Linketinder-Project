package com.jpcode.service

import com.jpcode.dao.contratos.CandidatoCurtidasConsultaRepository
import com.jpcode.dao.contratos.CompetenciaRepository
import com.jpcode.dao.contratos.RetribuicaoRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.*
import com.jpcode.exception.empresa.EmpresaLoginException
import com.jpcode.exception.empresa.EmpresaNaoEncontradaPorIdException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Empresa
import com.jpcode.model.referencia.Competencia
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class EmpresaService {
    final SenhaService senhaService
    final ReferenciaRepository<Pais> paisRepository
    final ReferenciaRepository<Estado> estadoRepository
    final UsuarioRepository<Empresa, EmpresaComReferenciasDTO, EmpresaViewListDTO> empresaRepository
    final CandidatoCurtidasConsultaRepository candidatoCurtidasConsultaRepository
    final RetribuicaoRepository empresaCurtirRepository
    final CompetenciaRepository competenciasVagaRepository

    EmpresaService(ReferenciaRepository<Pais> paisRepository, ReferenciaRepository<Estado> estadoRepository, UsuarioRepository<Empresa, EmpresaComReferenciasDTO, EmpresaViewListDTO> empresaRepository, CandidatoCurtidasConsultaRepository candidatoCurtidasConsultaRepository, RetribuicaoRepository empresaCurtirRepository, CompetenciaRepository competenciasVagaRepository, SenhaService senhaService = new SenhaService()) {
        this.senhaService = senhaService
        this.paisRepository = paisRepository
        this.estadoRepository = estadoRepository
        this.empresaRepository = empresaRepository
        this.candidatoCurtidasConsultaRepository = candidatoCurtidasConsultaRepository
        this.empresaCurtirRepository = empresaCurtirRepository
        this.competenciasVagaRepository = competenciasVagaRepository
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
                senhaService.derivar(cadastrarEmpresaDTO.senha),
                cadastrarEmpresaDTO.cnpj,
                paisId,
                estadoId,
                cadastrarEmpresaDTO.cep,
                cadastrarEmpresaDTO.descricao
        )
        
        return empresaRepository.salvar(empresa)
    }

    EmpresaViewDTO buscarEmpresa(Long idEmpresa) {
        EmpresaComReferenciasDTO resultado = empresaRepository
                .buscarPorIdComReferencias(idEmpresa)
                .orElseThrow(() ->
                        new EmpresaNaoEncontradaPorIdException(idEmpresa)
                )

        return new EmpresaViewDTO(
                resultado.empresa.id,
                resultado.empresa.nome,
                resultado.empresa.email,
                resultado.estado,
                resultado.empresa.cep,
                resultado.empresa.descricao,
                resultado.pais
        )
    }

    boolean estaAtivo(Long id) {
        return empresaRepository.buscarPorId(id).map { it.ativo }.orElse(false)
    }

    Empresa logar(String email, String senha) {
        Empresa empresa = empresaRepository.buscarAtivoPorEmail(email)
                .orElseThrow(() -> new EmpresaLoginException())
        if (!empresa.ativo || !senhaService.verificar(senha, empresa.senha)) {
            throw new EmpresaLoginException()
        }
        return empresa
    }
    
    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga) {
        throw new UnsupportedOperationException('Consulta exige ator empresarial ou recrutador e escopo de vaga.')
    }

    void curtirCandidato(Long idEmpresa,Long idCandidato) {
        throw new UnsupportedOperationException('Retribuição exige vaga e transação pela MatchFacade.')
    }

    List<CandidatoAnonimoDTO> buscarCandidatosCurtidos(Long idEmpresa) {
        return empresaCurtirRepository.buscarCurtidas(idEmpresa)
    }

    void desativarEmpresa(Long idEmpresa) {
        empresaRepository.desativar(idEmpresa)
    }

    void atualizarEmpresa(AtualizarEmpresaDTO atualizarEmpresaDTO) {
        Empresa atual = empresaRepository.buscarPorId(atualizarEmpresaDTO.id)
                .orElseThrow(() -> new EmpresaNaoEncontradaPorIdException(atualizarEmpresaDTO.id))
        if (!atual.ativo) {
            throw new IllegalStateException('Empresa inativa não pode ser atualizada.')
        }
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
                        senhaService.derivar(atualizarEmpresaDTO.senha),
                        atualizarEmpresaDTO.cnpj,
                        paisId,
                        estadoId,
                        atualizarEmpresaDTO.cep,
                        atualizarEmpresaDTO.descricao,
                        atual.ativo
                )
        )
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idVaga) {
        return converterCompetenciasParaDTO(competenciasVagaRepository.buscarPorId(idVaga))
    }
    
    void removerCompetencia(Long idVaga, Long idCompetencia) {
        competenciasVagaRepository.removerCompetencia(idVaga, idCompetencia)
    }

    List<EmpresaViewListDTO> buscarEmpresas() {
        return empresaRepository.buscar()
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
