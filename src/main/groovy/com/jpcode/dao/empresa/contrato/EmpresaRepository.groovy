package com.jpcode.dao.empresa.contrato

import com.jpcode.dto.empresa.EmpresaComReferenciasDTO
import com.jpcode.dto.empresa.EmpresaViewListDTO
import com.jpcode.model.core.Empresa

interface EmpresaRepository {
    Empresa salvar(Empresa empresa)

    Optional<Empresa> buscarPorId(Long id)

    Empresa atualizarDados(Empresa empresa)

    Optional<EmpresaComReferenciasDTO> buscarPorIdComReferencias(Long id)

    List<EmpresaViewListDTO> buscarEmpresas()
}