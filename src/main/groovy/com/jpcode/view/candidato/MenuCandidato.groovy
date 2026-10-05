package com.jpcode.view.candidato

import com.jpcode.config.DaoConfig
import com.jpcode.config.MenuCandidatoConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.exception.candidato.CandidatoLoginException
import com.jpcode.model.core.Candidato
import com.jpcode.service.CandidatoService

class MenuCandidato {
    final Scanner scanner = new Scanner(System.in)
    final DaoConfig daoConfig = new DaoConfig()
    final ServiceConfig serviceConfig = new ServiceConfig(daoConfig)
    final MenuCandidatoConfig menuCandidatoConfig = new MenuCandidatoConfig(scanner, serviceConfig)

    final CandidatoService candidatoService = serviceConfig.candidatoService
    final MenuCandidatoCrud menuCandidatoCrud = menuCandidatoConfig.menuCandidatoCrud
    final MenuCurtirVaga menuCurtirVaga = menuCandidatoConfig.menuCurtirVaga
    
    void inicio() {
        int opcao = capturarEscolha("""
        1 - Cadastre-se 
        2 - Fazer Login
        """)
        switch (opcao) {
            case 1:
                menuCandidatoCrud.cadastrarCandidato()
                break
            case 2:
                login()
                break
        }
    }

    private void login() {
        println("Digite o email do candidato: ")
        String email = scanner.nextLine()
        println("Digite a senha do candidato: ")
        String senha = scanner.nextLine()
        tentarLogarCandidato(email, senha)
    }

    private void tentarLogarCandidato(String email, String senha) {
        try {
            Candidato candidatoEncontrado = candidatoService.logar(email, senha)
            println("Candidato Logado com sucesso!")
            menuCandidato(candidatoEncontrado)
        } catch (CandidatoLoginException e) {
            println(e.getMessage())
        }
    }

    private int capturarEscolha(String mensagem) {
        while (true) {
            println(mensagem)
            String opcaoUsuario = scanner.nextLine()
            try {
                return Integer.parseInt(opcaoUsuario)
            } catch (NumberFormatException e) {
                println("Erro, Digite uma opção númerica inteira!")
            }
        }
    }

    private void menuCandidato(Candidato candidato) {
        while (true) {
            println(candidato)
            int escolha = capturarEscolha("""
                        1 - Ver vagas curtidas
                        2 - Ver vagas disponiveis
                        3 - Curtir Vaga
                        4 - Desativar Conta
                        5 - Atualizar competencias
                        6 - Atualizar conta
                        7 - Ver matches
                        8 - Remover Competencia
                        9 - Sair
                        """)
            executarOpcaoEscolhida(escolha, candidato)
            if (escolha == 4 || escolha == 9) {
                return
            }
        }
    }

    private void executarOpcaoEscolhida(int escolha, Candidato candidato) {
        switch (escolha) {
            case 1:
                println(menuCurtirVaga.vagasAnonimasCurtidas(candidato.id))
                break
            case 2:
                println(menuCurtirVaga.vagasDisponiveisParaCandidato(candidato.id))
                break
            case 3:
                menuCurtirVaga.curtirVaga(candidato.id)
                break
            case 4:
                menuCandidatoCrud.apagarCandidato(candidato.id, candidato.senha)
                break
            case 5:
                menuCandidatoCrud.atualizarCompetencias(candidato.id)
                break
            case 6:
                menuCandidatoCrud.atualizarCandidato(candidato)
                break
            case 7:
                menuCandidatoCrud.verMatches(candidato.id)
                break
            case 8:
                menuCandidatoCrud.removerCompetencia(candidato.id)
                break
            case 9:
                return
        }
    }
}
