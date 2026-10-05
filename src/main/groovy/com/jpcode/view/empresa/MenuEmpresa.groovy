package com.jpcode.view.empresa

import com.jpcode.config.DaoConfig
import com.jpcode.config.MenuEmpresaConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.exception.empresa.EmpresaLoginException
import com.jpcode.model.core.Empresa
import com.jpcode.service.*
import com.jpcode.view.vaga.MenuVagaCrud

class MenuEmpresa {
    final Scanner scanner = new Scanner(System.in)
    final DaoConfig daoConfig = new DaoConfig()
    final ServiceConfig serviceConfig = new ServiceConfig(daoConfig)
    final MenuEmpresaConfig menuConfig = new MenuEmpresaConfig(scanner, serviceConfig)

    final EmpresaService empresaService = serviceConfig.empresaService

    final MenuEmpresaCrud menuEmpresaCrud = menuConfig.menuEmpresaCrud
    final MenuVagaCrud menuVagaCrud = menuConfig.menuVagaCrud
    final MenuCurtirCandidato menuCurtirCandidato = menuConfig.menuCurtirCandidato

    void inicio() {
        int opcao = capturarEscolha("""
            1 - Cadastrar Empresa
            2 - Fazer Login
            """)
        switch (opcao) {
            case 1:
                menuEmpresaCrud.cadastrarEmpresa()
                break
            case 2:
                login()
                break

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

    private void login() {
        println("Digite o email da empresa: ")
        String email = scanner.nextLine()
        println("Digite a senha da empresa: ")
        String senha = scanner.nextLine()
        tentarLogarEmpresa(email, senha)
    }

    private void tentarLogarEmpresa(String email, String senha) {
        try {
            Empresa empresaEncontrada = empresaService.logar(email, senha)
            println("Empresa Logada com sucesso!")
            menuEmpresa(empresaEncontrada)
        } catch (EmpresaLoginException e) {
            println(e.getMessage())
        }

    }

    private void menuEmpresa(Empresa empresa) {
        while (true) {
            menuEmpresaCrud.exibirEmpresaInicio(empresa)
            int escolha = capturarEscolha("""
                1 - Ver Vagas
                2 - Curtir Candidatos em Vaga
                3 - Criar Vaga
                4 - Ver Candidatos Curtidos
                5 - Desativar Conta
                6 - Apagar vaga
                7 - Atualizar conta 
                8 - Atualizar vaga
                9 - Adicionar competencias em Vaga
                10 - Ver Matches
                11 - Remover competencia
                12 - Sair
                """)
            executarOpcaoEscolhida(escolha, empresa)
            if (escolha == 5 || escolha == 12) {
                return
            }
        }
    }

    private void executarOpcaoEscolhida(int escolha, Empresa empresa) {
            switch (escolha) {
                case 1:
                    println(menuVagaCrud.verVagasEmpresa(empresa.id))
                    break
                case 2:
                    menuCurtirCandidato.escolherVagaParaCurtirCandidato(empresa.id)
                    break
                case 3:
                    menuVagaCrud.criarVaga(empresa.id)
                    break
                case 4:
                    menuCurtirCandidato.verCandidatosCurtidos(empresa.id)
                    break
                case 5:
                    menuEmpresaCrud.apagarEmpresa(empresa.id, empresa.senha)
                    break
                case 6:
                    menuVagaCrud.apagarVaga(empresa.id)
                    break
                case 7:
                    menuEmpresaCrud.atualizarEmpresa(empresa)
                    break
                case 8:
                    menuVagaCrud.atualizarVaga(empresa.id)
                    break
                case 9:
                    menuVagaCrud.atualizarCompetencias(empresa.id)
                    break
                case 10:
                    menuEmpresaCrud.verMatches(empresa.id)
                    break
                case 11:
                    menuVagaCrud.escolherVagaParaRemoverCompetencia(empresa.id)
                    break
                case 12:
                    return
            }
    }
}
