package com.jpcode.dao.contratos

interface ReferenciaRepository<T> {
    Optional<T> buscarPorId(Long id)
    Optional<Long> buscarIdPorNome(String nome)
}
