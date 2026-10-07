export interface LoginStrategy {
    login(email: string): void
}