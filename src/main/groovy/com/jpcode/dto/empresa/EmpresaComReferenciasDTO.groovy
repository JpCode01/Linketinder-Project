package com.jpcode.dto.empresa

import com.jpcode.model.core.Empresa
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class EmpresaComReferenciasDTO {
    Empresa empresa
    Estado estado
    Pais pais

    EmpresaComReferenciasDTO(
            Empresa empresa,
            Estado estado,
            Pais pais
    ) {
        this.empresa = empresa
        this.estado = estado
        this.pais = pais
    }
}