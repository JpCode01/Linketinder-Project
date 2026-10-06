package com.jpcode.dao.referencia.contrato

interface ReferenciaRepository<T> {
    Optional<T> buscarPorId(Long id)
    Optional<Long> buscarIdPorNome(String nome)
}
