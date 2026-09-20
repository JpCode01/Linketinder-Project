package com.jpcode.view


import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.core.Candidato
import com.jpcode.service.CandidatoService
import com.jpcode.service.VagaService

class MenuCandidato {
    final Scanner scanner = new Scanner(System.in)
    final CandidatoService candidatoService = new CandidatoService()
    final VagaService vagaService = new VagaService()
    
    void inicio() {
        println("""
        1 - Cadastre-se 
        2 - Login por nome
        """)
        switch (scanner.nextInt()) {
            case 1:
                cadastrarCandidato()
                break
            case 2:
                login()
                break
        }
    }

    private void login() {
        while (true) {
            scanner.nextLine()
            println("Digite o email do candidato: ")
            String email = scanner.nextLine()
            println("Digite a senha do candidato: ")
            String senha = scanner.nextLine()
            Candidato candidatoEncontrado = candidatoService.logar(email, senha)
            if (candidatoEncontrado) {
                menuCandidato(candidatoEncontrado)
                break
            } else {
                println("""
                Email ou Senha incorretos
                
                1 - Tente Novamente
                Qualquer Tecla - Sair
                """)
                if (scanner.nextLine() != "1") {
                    break
                }
            }
        }
    }

    private void menuCandidato(Candidato candidato) {
        while (true) {
            List<VagaAnonimaDTO> vagasCurtidas = vagaService.listarVagasCurtidas(candidato.id)
            List<VagaAnonimaDTO> vagasDisponiveis = vagaService.buscarTodasAsVagas() - vagasCurtidas
            println(candidato)
            println("""
                        1 - Ver vagas curtidas
                        2 - Ver vagas disponíveis
                        3 - Curtir Vaga
                        4 - Sair
                        """)
            switch (scanner.nextInt()) {
                case 1:
                    println(vagasCurtidas)
                    break
                case 2:
                    println(vagasDisponiveis)
                    break
                case 3:
                    curtirVaga(candidato, vagasDisponiveis)
                    break
                case 4:
                    return
            }
        }
    }

    void curtirVaga(Candidato candidato, List<VagaAnonimaDTO> vagasDisponiveis) {
        if (vagasDisponiveis.isEmpty()) {
            println("Nao ha vagas disponiveis no momento!")
            return
        }
        println(vagasDisponiveis)
        println("Escolha uma vaga por ID: ")
        int idVaga = scanner.nextInt()
        scanner.nextLine()
        if (vagaService.buscarVaga(idVaga) != null) {

            println("Competencias exigidas: " + vagaService.buscarVaga(idVaga).competencias)
            println("Desja Curtir a vaga: (s/n)? ")

            if (scanner.nextLine().toLowerCase() == "s") {
                vagaService.curtir(candidato.id, idVaga)
            } else {
                println("Opção inválida!")
            }
            
        } else {
            println("Vaga não encontrada, talvez voce tenha digitado o ID incorretamente")
        }
    }

    private void cadastrarCandidato() {
        scanner.nextLine()

        println("Nome:")
        String nome = scanner.nextLine()

        println("Email:")
        String email = scanner.nextLine()

        println("CPF:")
        String cpf = scanner.nextLine()

        println("Idade:")
        int idade = scanner.nextInt()
        scanner.nextLine()

        println("Estado:")
        String estado = scanner.nextLine()

        println("CEP:")
        String cep = scanner.nextLine()

        println("Descrição:")
        String descricao = scanner.nextLine()

        List<String> competencias = capturarCompetencias()

        Candidato candidato = candidatoService.cadastrarCandidato(
                nome,
                email,
                cpf,
                idade,
                estado,
                cep,
                descricao,
                competencias
        )

        if (candidato != null) {
            Menu.candidatos.add(candidato)
        }

    }
    private List<String> capturarCompetencias() {
        List<String> competencias = []

        while (true) {
            println("""
                Competências atuais do Candidato: ${competencias}
        
                1 - Digitar nova competência
                2 - Parar
                """)

            if (scanner.nextInt() == 2) {
                break
            }

            scanner.nextLine()

            println("""
        Competências disponíveis: ${CompetenciasEnum.values() - competencias}
        
        Digite uma competência:
        """)

            competencias.add(scanner.nextLine())
        }

        return competencias
    }
}
