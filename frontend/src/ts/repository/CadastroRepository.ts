export interface CadastroRepository<T> {
    salvar(objeto: T): void
}