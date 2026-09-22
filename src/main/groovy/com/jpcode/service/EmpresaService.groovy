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
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Empresa
import com.jpcode.model.core.Vaga
import com.jpcode.validation.CompetenciaValidation

class EmpresaService {
    final CompetenciaValidation validation
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO
    final EmpresaDAO empresaDAO
    final CandidatoCurtirDAO candidatoCurtirDAO
    final EmpresaCurtirDAO empresaCurtirDAO
    final CompetenciasVagaDAO competenciasVagaDAO
    final CompetenciaDAO competenciaDAO

    EmpresaService(CompetenciaValidation validation, PaisDAO paisDAO, EstadoDAO estadoDAO, EmpresaDAO empresaDAO, CandidatoCurtirDAO candidatoCurtirDAO, EmpresaCurtirDAO empresaCurtirDAO, CompetenciasVagaDAO competenciasVagaDAO, CompetenciaDAO competenciaDAO) {
        this.validation = validation
        this.paisDAO = paisDAO
        this.estadoDAO = estadoDAO
        this.empresaDAO = empresaDAO
        this.candidatoCurtirDAO = candidatoCurtirDAO
        this.empresaCurtirDAO = empresaCurtirDAO
        this.competenciasVagaDAO = competenciasVagaDAO
        this.competenciaDAO = competenciaDAO
    }

    Empresa cadastrarEmpresa(
            String nome,
            String email,
            String senha,
            String cnpj,
            String pais,
            String estado,
            String cep,
            String descricao
    ) {
        Long paisId = paisDAO.buscarIdPorNome(pais)
        Long estadoId = estadoDAO.buscarIdPorSigla(estado)
        if (paisId != null && estadoId != null) {

            Empresa empresa = new Empresa(
                    nome,
                    email,
                    senha,
                    cnpj,
                    paisId,
                    estadoId,
                    cep,
                    descricao
            )
            return empresaDAO.salvar(empresa)
        } else {
            return null
        }
    }

    void curtirCandidato(Candidato candidato, Empresa empresa) {
        empresa.adicionarCandidatoCurtido(candidato)
    }

    List<Vaga> ListarVagasPorEmpresa(Empresa empresa) {
        empresa.vagas
    }

    void ListarCandidatosPorVaga(Vaga vaga) {
        vaga.candidatosQueCurtiram.eachWithIndex { candidato, index ->
            println "$index - Candidato anônimo ${index + 1}"
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
