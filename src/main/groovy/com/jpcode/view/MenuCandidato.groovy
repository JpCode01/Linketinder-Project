package com.jpcode.view

import com.jpcode.config.DaoConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.exception.CandidatoNaoEncontradoException
import com.jpcode.exception.CompetenciaNaoEncontradaException
import com.jpcode.exception.EstadoNaoEncontradoException
import com.jpcode.exception.PaisNaoEncontradoException
import com.jpcode.model.core.Candidato
import com.jpcode.service.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
                    println(vagasAnonimasCurtidas(candidato.id))
                    break
                case 2:
                    List<VagaAnonimaDTO> vagasCurtidasEncontradas = vagasAnonimasCurtidas(candidato.id)
                    println(vagasDisponiveis(vagasCurtidasEncontradas))
                    break
                case 3:
                    curtirVaga(candidato.id)
                    break
                case 4:
                    if(apagarCandidato(candidato.id, candidato.senha)) {
                        return
                    }
                    break
                case 5:
                    atualizarCompetencias(candidato.id)
                    break
                case 6:
                    atualizarCandidato(candidato)
                    break
                case 7:
                    verMatches(candidato.id)
                    break
                case 8:
                    removerCompetencia(candidato.id)
                    break
                case 9:
                    return
            }
        }
    }

    private List<String> competenciasCandidato(Long idCandidato) {
        return candidatoService.competenciasEmString(idCandidato)
    }

    private List<VagaAnonimaDTO> vagasDisponiveis(List<VagaAnonimaDTO> vagasAnonimasCurtidas) {
        return vagaService.buscarTodasAsVagas() - vagasAnonimasCurtidas
    }

    private List<VagaAnonimaDTO> vagasAnonimasCurtidas(Long idCandidato) {
        return vagaService.listarVagasCurtidas(idCandidato)
    }

    private Long solicitarId(String mensagem) {
        println(mensagem)

        if (!scanner.hasNextLong()) {
            scanner.nextLine()
            throw new InputMismatchException("Digite um ID numérico.")
        }

        return scanner.nextLong()
    }

    private boolean verificaSeExisteVaga(Long idVaga, List<VagaAnonimaDTO> vagasDisponiveis) {
        return vagasDisponiveis.any {
            VagaAnonimaDTO vaga -> vaga.id == idVaga
        }
    }

    void curtirVaga(Long idCandidato) {
        List<VagaAnonimaDTO> vagasCurtidasCandidato = vagasAnonimasCurtidas(idCandidato)
        List<VagaAnonimaDTO> vagasDisponiveis = vagasDisponiveis(vagasCurtidasCandidato)

        if (vagasDisponiveis.isEmpty()) {
            println("Nao ha vagas disponiveis no momento!")
            return
        }

        println(vagasDisponiveis)

        try {
            Long idVaga = solicitarId("Digite o ID da vaga: ")

            if (!verificaSeExisteVaga(idVaga, vagasDisponiveis)) {
                println("Vaga de ID ${idVaga} não encontrada!")
                return
            }

            vagaService.curtir(idCandidato, idVaga)
            println("Vaga Curtida com sucesso!")


        } catch (InputMismatchException e) {
            println("Entrada inválida! Digite um ID numérico." + e.getMessage())
        }
    }

    private CadastrarCandidatoDTO capturarDadosCadastrar() {
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
        LocalDate data = validarDataNascimento(scanner.nextLine())

        println("Idade:")
        int idade = validarIdade(scanner.nextLine())

        println("Pais:")
        String pais = scanner.nextLine()

        println("Estado em sigla (SP/RS/RJ):")
        String estado = scanner.nextLine()

        println("CEP:")
        String cep = scanner.nextLine()

        println("Descrição:")
        String descricao = scanner.nextLine()

        List<String> competencias = capturarCompetencias([])

        return new CadastrarCandidatoDTO(
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
    }

    private void cadastrarCandidato() {
        CadastrarCandidatoDTO candidato = capturarDadosCadastrar()
        tentarCandastrarCandidato(candidato)
    }

    private void tentarCandastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        try {
            candidatoService.cadastrarCandidato(cadastrarCandidatoDTO)
            println("Candidato cadastrado com sucesso!")
        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException | CompetenciaNaoEncontradaException e) {
            println(e.getMessage())
        }
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

        println("Data de nascimento (dd/MM/yyyy):")
        String dataInput = scanner.nextLine()

        if (!dataInput.isEmpty()) {
            candidato.dataNascimento = validarDataNascimento(dataInput)
        }

        println("Idade: ")
        String idade = scanner.nextLine()

        if (!idade.isEmpty()) {
            candidato.idade = validarIdade(idade)
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
        
        tentarAtualizarCandidato(
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

    private LocalDate validarDataNascimento(String dataInput) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        while (true) {
            try {
                return LocalDate.parse(dataInput, formatter)
            } catch (DateTimeParseException e) {
                println("Data inválida! Digite uma data no formato dd/MM/yyyy.")
                dataInput = scanner.nextLine()
            }
        }
    }

    private int validarIdade(String idadeInput) {
        while (true) {
            try {
                int idade = Integer.parseInt(idadeInput)
                return idade
            } catch (NumberFormatException e) {
                println("Idade inválida! Digite um número inteiro.")
                idadeInput = scanner.nextLine()
            }
        }
    }

    private void tentarAtualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        try {
            candidatoService.atualizarCandidato(atualizarCandidatoDTO)
            println("Candidato atualizado com sucesso!")
        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
            println(e.getMessage())
        }
    }

    private List<String> capturarCompetencias(List<String> competencias) {
        List<String> todasCompetencias = competenciaService.listarCompetencias()

        while (true) {
            if ((todasCompetencias - competencias).isEmpty()) {
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

    boolean apagarCandidato(Long idCandidato, String senha) {
        scanner.nextLine()
        println("Digite sua senha para confirmar (Caso queira desistir, aperte enter): ")
        if (scanner.nextLine() == senha) {
            candidatoService.desativarCandidato(idCandidato)
            println("Conta deletada com sucesso")
            return true
        }
        return false
    }

    void atualizarCompetencias(Long idCandidato) {
        List<String> competenciasCandidato = competenciasCandidato(idCandidato)
        List<String> novasCompetencias = capturarCompetencias(competenciasCandidato)
        candidatoService.adicionarCompetencias(idCandidato, novasCompetencias)
    }

    void verMatches(Long idCandidato) {
        println(matchService.verMatchesPorCandidato(idCandidato))
    }
    
    private void tentarRemoverCompetencia(Long idCandidato, Long idCompetencia) {
        try {
            candidatoService.removerCompetencia(idCandidato, idCompetencia)
            println("Competencia removida com sucesso!")
        } catch (CandidatoNaoEncontradoException e) {
            e.getMessage()
        }
    }

    void removerCompetencia(Long idCandidato) {
        println(candidatoService.listaParaRemover(idCandidato))
        try {
            Long idCompetencia = solicitarId("Digite o ID do candidato: ")
            tentarRemoverCompetencia(idCandidato, idCompetencia)
        } catch (InputMismatchException e) {
            println("Entrada inválida! Digite um ID numérico." + e.getMessage())
        }
    }
}
