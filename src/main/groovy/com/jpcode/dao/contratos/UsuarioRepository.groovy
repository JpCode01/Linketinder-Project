package com.jpcode.dao.contratos

interface UsuarioRepository<T, R, L> {

    Optional<T> buscarPorEmailESenha(String email, String senha)

    T salvar(T objeto)

    Optional<T> buscarPorId(Long id)

    T atualizarDados(T objeto)

    Optional<R> buscarPorIdComReferencias(Long id)

    List<L> buscar()

    void desativar(Long id)
}
