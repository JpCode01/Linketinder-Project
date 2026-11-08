export interface CadastrarCandidatoRequestDTO {
    nome: string
    sobrenome: string
    email: string
    senha: string
    cpf: string
    dataNascimento: string
    pais: string
    idade: number
    estado: string
    cep: string
    descricao: string
    competencias: string[]
}
