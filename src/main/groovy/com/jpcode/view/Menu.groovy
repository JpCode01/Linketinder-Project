package com.jpcode.view

class Menu {
    private final Scanner scanner = new Scanner(System.in)
    private final MenuEmpresa menuEmpresa = new MenuEmpresa()
    private final MenuCandidato menuCandidato = new MenuCandidato()

    void inicio() {
        println("""
    SEJA BEM VINDO AO LINKETINDER, AQUI EMPRESAS PODEM
    ENCONTRAR CANDIDATOS POR MATCH
    """)

        while (true) {
            println("""
        1 - ENTRAR COMO EMPRESA
        2 - ENTRAR COMO CANDIDATO

        3 - SAIR
        
        ESCOLHA A OPCAO DESEJADA:""")
            switch (scanner.nextInt()) {
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


}
