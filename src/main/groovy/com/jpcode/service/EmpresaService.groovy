package com.jpcode.service

import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.relacionamento.EmpresaCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.model.core.Empresa

class EmpresaService {
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO
    final EmpresaDAO empresaDAO
    final CandidatoCurtirDAO candidatoCurtirDAO
    final EmpresaCurtirDAO empresaCurtirDAO
    final CompetenciasVagaDAO competenciasVagaDAO
    final CompetenciaDAO competenciaDAO

    EmpresaService(PaisDAO paisDAO, EstadoDAO estadoDAO, EmpresaDAO empresaDAO, CandidatoCurtirDAO candidatoCurtirDAO, EmpresaCurtirDAO empresaCurtirDAO, CompetenciasVagaDAO competenciasVagaDAO, CompetenciaDAO competenciaDAO) {
        this.paisDAO = paisDAO
        this.estadoDAO = estadoDAO
        this.empresaDAO = empresaDAO
        this.candidatoCurtirDAO = candidatoCurtirDAO
        this.empresaCurtirDAO = empresaCurtirDAO
        this.competenciasVagaDAO = competenciasVagaDAO
        this.competenciaDAO = competenciaDAO
    }

    Empresa cadastrarEmpresa(CadastrarEmpresaDTO cadastrarEmpresaDTO) {
        Long paisId = paisDAO.buscarIdPorNome(cadastrarEmpresaDTO.pais)
        Long estadoId = estadoDAO.buscarIdPorSigla(cadastrarEmpresaDTO.estado)
      
        if (paisId != null && estadoId != null) {

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
            return empresaDAO.salvar(empresa)
        } else {
            return null
        }
    }

    Empresa logar(String email, String senha) {
        Empresa empresaEncontrada = null
        if ((!email.isBlank()) && (!senha.isBlank())) {
            empresaEncontrada = empresaDAO.buscarPorEmailESenha(email, senha)
        }
        return empresaEncontrada
    }
    
    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga) {
        return candidatoCurtirDAO.buscarCandidatosQueCurtiram(idVaga)
    }

    void curtirCandidato(Long idEmpresa,Long idCandidato) {
        empresaCurtirDAO.salvar(idEmpresa, idCandidato)
    }

    List<CandidatoAnonimoDTO> buscarCandidatosCurtidos(Long idEmpresa) {
        return empresaCurtirDAO.buscarCandidatosCurtidos(idEmpresa)
    }

    void desativarEmpresa(Long idEmpresa) {
        empresaDAO.desativar(idEmpresa)
    }

    void atualizarEmpresa(
            Long id,
            String nome,
            String email,
            String senha,
            String cnpj,
            String pais,
            String estado,
            String cep,
            String descricao,
            boolean ativo
    ) {
        Long idPais = paisDAO.buscarIdPorNome(pais)
        Long idEstado = estadoDAO.buscarIdPorSigla(estado)

        empresaDAO.atualizarDados(
                new Empresa(
                        id,
                        nome,
                        email,
                        senha,
                        cnpj,
                        idPais,
                        idEstado,
                        cep,
                        descricao,
                        ativo
                )
        )
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idVaga) {
        return competenciaDAO.converterCompetenciasParaDTO(competenciasVagaDAO.buscarPorVaga(idVaga))
    }
    
    void removerCompetencia(Long idVaga, Long idCompetencia) {
        competenciasVagaDAO.removerCompetencia(idVaga, idCompetencia)
    }
}
