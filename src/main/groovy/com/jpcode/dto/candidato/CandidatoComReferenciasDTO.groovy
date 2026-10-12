
package com.jpcode.dto.candidato

import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class CandidatoComReferenciasDTO {
    Candidato candidato
    Estado estado
    Pais pais

    CandidatoComReferenciasDTO(
            Candidato candidato,
            Estado estado,
            Pais pais
    ) {
        this.candidato = candidato
        this.estado = estado
        this.pais = pais
    }
}
