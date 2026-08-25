package com.jpcode.view

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Candidato
import com.jpcode.model.Vaga
import com.jpcode.service.CandidatoService
import com.jpcode.service.VagaService
import com.jpcode.validation.CompetenciaValidation

class MenuCandidato {
    final Scanner scanner = new Scanner(System.in)
    final CandidatoService candidatoService = new CandidatoService(new CompetenciaValidation())
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
        scanner.nextLine()
        println("Digite o nome do candidato desejado: ")
        String nome = scanner.nextLine()
        Candidato candidato = Menu.candidatos.find {it.nome == nome}
        if (candidato) {
            menuCandidato(candidato)
        }
    }

    private void menuCandidato(Candidato candidato) {
        while (true) {
            println(candidato)
            println("""
                        1 - Ver vagas curtidas
                        2 - Ver vagas disponíveis
                        3 - Curtir Vaga
                        4 - Sair
                        """)
            switch (scanner.nextInt()) {
                case 1:
                    verVagasCurtidas(candidato)
                    break
                case 2:
                    verVagasDisponiveis(candidato)
                    break
                case 3:
                    curtirVaga(candidato)
                    break
                case 4:
                    return
            }
        }
    }

    void verVagasCurtidas(Candidato candidato){
        if (!candidato.vagasCurtidas.isEmpty()) {
            println(candidato.vagasCurtidas)
        } else {
            println("Não há nenhuma vaga curtida")
        }
    }

    void verVagasDisponiveis(Candidato candidato) {
        if (candidato.vagasCurtidas.isEmpty()) {
            println(Menu.vagasGerais)
        } else {
            println(Menu.vagasGerais - candidato.vagasCurtidas)
        }
    }

    void curtirVaga(Candidato candidato) {
        println(Menu.vagasGerais)
        List vagas
        if (candidato.vagasCurtidas.isEmpty()) {
            vagas = vagaService.listarVagas(Menu.vagasGerais)
        } else {
            vagas = vagaService.listarVagas(Menu.vagasGerais - candidato.vagasCurtidas)
        }
        println("Escolha uma vaga por ID: ")
        int idVaga = scanner.nextInt()
        scanner.nextLine()
        try {
            Vaga vaga = vagas.get(idVaga)
            println(vaga.competencias)
            println("Deseja curtir a vaga(s/n)? ")

            if(scanner.nextLine().toLowerCase() == "s") {
                vagaService.curtir(candidato, vaga)
            } else {
                println("Opção invalida")
            }
        } catch (IndexOutOfBoundsException ex) {
            println("ID invalido")
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
