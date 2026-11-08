package com.jpcode.dao.contratos

import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga

interface VagaRepository {
    Vaga salvar(Vaga vaga)
    Vaga salvarComCompetencias(Vaga vaga, List<Long> idsCompetencias)

    Optional<Vaga> buscarPorId(Long id)

    List<VagaEmpresaDTO> buscarVagasEmpresa(Long idEmpresa)
    List<VagaEmpresaDTO> buscarVagasRecrutador(Long idRecrutador)

    List<VagaAnonimaDTO> buscarTodasAsVagas()

    void deletar(Long id)

    Vaga atualizarDados(Vaga vaga)
}
