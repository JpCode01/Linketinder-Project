export interface LoginStrategy {
    login(email: string, senha: string): Promise<void>
}