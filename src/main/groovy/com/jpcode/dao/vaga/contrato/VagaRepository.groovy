package com.jpcode.dao.vaga.contrato

import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga

interface VagaRepository {
    Vaga salvar(Vaga vaga)

    Optional<Vaga> buscarPorId(Long id)

    List<VagaEmpresaDTO> buscarVagasEmpresa(Long idEmpresa)

    List<VagaAnonimaDTO> buscarTodasAsVagas()

    void deletar(Long id)

    Vaga atualizarDados(Vaga vaga)
}