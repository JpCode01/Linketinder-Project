package com.jpcode.dao.contratos

interface CurtidaRepository<T> {
    void salvar(Long idOrigem, Long idDestino)
    List<T> buscarCurtidas(Long id)
    boolean jaCurtiu(Long idOrigem, Long idDestino)
}