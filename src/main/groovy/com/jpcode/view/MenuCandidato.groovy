package com.jpcode.view

import com.jpcode.config.DaoConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Vaga
import com.jpcode.service.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MenuCandidato {
    final Scanner scanner = new Scanner(System.in)
    final DaoConfig daoConfig = new DaoConfig()
    final ServiceConfig serviceConfig = new ServiceConfig(daoConfig)

    final CandidatoService candidatoService = serviceConfig.candidatoService
    final VagaService vagaService = serviceConfig.vagaService
    final CompetenciaService competenciaService = serviceConfig.competenciaService
    final ReferenciaService referenciaService = serviceConfig.referenciaService
    final MatchService matchService = serviceConfig.matchService

    void inicio() {
        println("""
        1 - Cadastre-se 
        2 - Fazer Login
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
            List<String> competencias = candidatoService.competenciasEmString(candidato.id)
            println(candidato)
            println("""
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
                    if(apagarCandidato(candidato)) {
                        return
                    }
                    break
                case 5:
                    atualizarCompetencias(candidato, competencias)
                    break
                case 6:
                    atualizarCandidato(candidato)
                    break
                case 7:
                    verMatches(candidato)
                    break
                case 8:
                    removerCompetencia(candidato)
                    break
                case 9:
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
        Vaga vagaBuscada = vagaService.buscarVaga(idVaga)

        if (vagaBuscada != null) {

            println("Competencias exigidas: " + vagaService.buscarCompetenciasDeVaga(idVaga))
            println("Desja Curtir a vaga: (s/n)? ")

            if (scanner.nextLine().toLowerCase() == "s") {
                vagaService.curtir(candidato.id, idVaga)
            }
            
        } else {
            println("Vaga não encontrada, talvez voce tenha digitado o ID incorretamente")
        }
    }

    private void cadastrarCandidato() {
        scanner.nextLine()

        println("Nome:")
        String nome = scanner.nextLine()

        println("Sobrenome:")
        String sobrenome = scanner.nextLine()

        println("Email:")
        String email = scanner.nextLine()

        println("Senha:")
        String senha = scanner.nextLine()

        println("CPF:")
        String cpf = scanner.nextLine()

        println("Data de nascimento (dd/MM/yyyy): ")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        LocalDate data = LocalDate.parse(scanner.nextLine(), formatter)

        println("Idade:")
        int idade = scanner.nextInt()
        scanner.nextLine()

        println("Pais:")
        String pais = scanner.nextLine()

        println("Estado em sigla (SP/RS/RJ):")
        String estado = scanner.nextLine()

        println("CEP:")
        String cep = scanner.nextLine()

        println("Descrição:")
        String descricao = scanner.nextLine()

        List<String> competencias = capturarCompetencias([])

        candidatoService.cadastrarCandidato(
                new CadastrarCandidatoDTO(
                        nome,
                        sobrenome,
                        email,
                        senha,
                        cpf,
                        data,
                        pais,
                        idade,
                        estado,
                        cep,
                        descricao,
                        competencias
                )
        )
    }

    private void atualizarCandidato(Candidato candidato) {
        scanner.nextLine()


        println("Nome:")
        String nome = scanner.nextLine()
        if (!nome.isEmpty()) {
            candidato.nome = nome
        }

        println("Sobrenome:")
        String sobrenome = scanner.nextLine()
        if (!sobrenome.isEmpty()) {
            candidato.sobrenome = sobrenome
        }

        println("Email:")
        String email = scanner.nextLine()
        if (!email.isEmpty()) {
            candidato.email = email
        }

        println("Senha:")
        String senha = scanner.nextLine()
        if (!senha.isEmpty()) {
            candidato.senha = senha
        }

        println("CPF:")
        String cpf = scanner.nextLine()
        if (!cpf.isEmpty()) {
            candidato.cpf = cpf
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        println("Data de nascimento (dd/MM/yyyy):")
        String dataInput = scanner.nextLine()

        if (!dataInput.isEmpty()) {
            candidato.dataNascimento = LocalDate.parse(dataInput, formatter)
        }

        println("Pais:")
        String pais = scanner.nextLine()


        if (pais.isEmpty()) {
            pais = referenciaService.converterIdPaisParaString(candidato.idPais)
        }

        println("Estado em sigla (SP/RS/RJ):")
        String estado = scanner.nextLine()
        
        if (estado.isEmpty()) {
            estado = referenciaService.converterIdEstadoParaString(candidato.idEstado)
        }

        println("CEP:")
        String cep = scanner.nextLine()
        if (!cep.isEmpty()) {
            candidato.cep = cep
        }

        println("Descrição:")
        String descricao = scanner.nextLine()
        if (!descricao.isEmpty()) {
            candidato.descricao = descricao
        }
        
        candidatoService.atualizarCandidato(
                new AtualizarCandidatoDTO(
                        candidato.id,
                        candidato.nome,
                        candidato.sobrenome,
                        candidato.email,
                        candidato.senha,
                        candidato.cpf,
                        candidato.dataNascimento,
                        pais,
                        candidato.idade,
                        estado,
                        candidato.cep,
                        candidato.descricao,
                        candidato.ativo
                )
        )
    }


    private List<String> capturarCompetencias(List<String> competencias) {
        List<String> todasCompetencias = competenciaService.listarCompetencias()

        while (true) {
            if (todasCompetencias - competencias == []) {
                break
            }

            println("""
        Competencias atuais: ${competencias}

        1 - Digitar nova competencia
        2 - Parar
        """)

            if (scanner.nextInt() == 2) {
                break
            }

            scanner.nextLine()
            
            println("""
        Competencias disponiveis: ${todasCompetencias - competencias}

        Digite uma competencia:
        """)

            String competencia = scanner.nextLine().trim().toUpperCase()

            if (!competencias.contains(competencia)) {
                competencias.add(competencia)
            } else {
                println("Competencia ja existente!")
            }
        }

        return competencias
    }

    boolean apagarCandidato(Candidato candidato) {
        scanner.nextLine()
        println("Digite sua senha para confirmar (Caso queira desistir, aperte enter): ")
        if (scanner.nextLine() == candidato.senha) {
            candidatoService.desativarCandidato(candidato.id)
            println("Conta deletada com sucesso")
            return true
        }
        return false
    }

    void atualizarCompetencias(Candidato candidato, List<String> competenciasCandidato) {
        List<String> novasCompetencias = capturarCompetencias(competenciasCandidato)
        candidatoService.adicionarCompetencias(candidato.id, novasCompetencias)
    }

    void verMatches(Candidato candidato) {
        println(matchService.verMatchesPorCandidato(candidato.id))
    }

    void removerCompetencia(Candidato candidato) {
        println(candidatoService.listaParaRemover(candidato.id))
        println("Digite o ID da competencia: ")
        Long idCompetencia = scanner.nextLong()
        if (competenciaService.buscarCompetencia(idCompetencia) != null) {
            candidatoService.removerCompetencia(candidato.id, idCompetencia)
        }
    }
}
