package com.jpcode.service

import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Empresa
import com.jpcode.model.core.Vaga
import com.jpcode.validation.CompetenciaValidation

class EmpresaService {
    final CompetenciaValidation validation
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO
    final EmpresaDAO empresaDAO

    EmpresaService(CompetenciaValidation validation, PaisDAO paisDAO, EstadoDAO estadoDAO, EmpresaDAO empresaDAO) {
        this.validation = validation
        this.paisDAO = paisDAO
        this.estadoDAO = estadoDAO
        this.empresaDAO = empresaDAO
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
}
