package com.jpcode.dao.contratos

interface ReferenciaRepository<T> {
    default List<T> buscar() { throw new UnsupportedOperationException() }

    Optional<T> buscarPorId(Long id)
    Optional<Long> buscarIdPorNome(String nome)
    List<T> listarTodos()
}
