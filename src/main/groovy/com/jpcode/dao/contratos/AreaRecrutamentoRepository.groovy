package com.jpcode.dao.contratos

import com.jpcode.model.referencia.AreaRecrutamento

interface AreaRecrutamentoRepository extends ReferenciaRepository<AreaRecrutamento> {
    List<AreaRecrutamento> buscar()
}
