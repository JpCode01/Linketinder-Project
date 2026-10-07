package com.jpcode.view

import com.jpcode.factory.MenuFactory
import com.jpcode.view.candidato.MenuCandidato
import com.jpcode.view.empresa.MenuEmpresa

class Menu {
    private final Scanner scanner = new Scanner(System.in)
    MenuFactory menuFactory = new MenuFactory(scanner)

    MenuEmpresa menuEmpresa = menuFactory.criarMenuEmpresa()
    MenuCandidato menuCandidato = menuFactory.criarMenuCandidato()

    void inicio() {
        println("""
    SEJA BEM VINDO AO LINKETINDER, AQUI EMPRESAS PODEM
    ENCONTRAR CANDIDATOS POR MATCH
    """)

        while (true) {
            int escolha = capturarEscolha("""
        1 - ENTRAR COMO EMPRESA
        2 - ENTRAR COMO CANDIDATO

        3 - SAIR
        
        ESCOLHA A OPCAO DESEJADA:""")
            switch (escolha) {
                case 1:
                    menuEmpresa.inicio()
                    break
                case 2:
                    menuCandidato.inicio()
                    break
                case 3:
                    return
            }
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


}
