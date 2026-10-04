package com.jpcode.view

import com.jpcode.config.DaoConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.exception.candidato.CandidatoLoginException
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.exception.vaga.SemVagasDisponiveisException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
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
        int opcao = capturarEscolha("""
        1 - Cadastre-se 
        2 - Fazer Login
        """)
        switch (opcao) {
            case 1:
                cadastrarCandidato()
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
    }
    
    private void executarOpcaoEscolhida(int escolha, Candidato candidato) {
        while (true) {
            switch (escolha) {
                case 1:
                    println(vagasAnonimasCurtidas(candidato.id))
                    break
                case 2:
                    println(vagasDisponiveisParaCandidato(candidato.id))
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

    private List<String> competenciasDisponiveis(List<String> competenciasAtuais) {
        return competenciaService.listarCompetencias() - competenciasAtuais
    }

    private List<VagaAnonimaDTO> vagasDisponiveisParaCandidato(Long idCandidato) {
        return vagaService.buscarTodasAsVagas() - vagasAnonimasCurtidas(idCandidato)
    }
    
    private List<VagaAnonimaDTO> vagasAnonimasCurtidas(Long idCandidato) {
        return vagaService.listarVagasCurtidas(idCandidato)
    }

    private Long solicitarId(String mensagem) {
        while (true) {
            println(mensagem)
            String idEscolhido = scanner.nextLine()
            try {
                return Long.parseLong(idEscolhido)
            } catch (NumberFormatException e) {
                println("Erro, digite um ID númerico!")
            }
        }
    }

    private boolean verificaSeExisteVaga(Long idVaga, List<VagaAnonimaDTO> vagasDisponiveis) {
        return vagasDisponiveis.any {
            VagaAnonimaDTO vaga -> vaga.id == idVaga
        }
    }

    private void verificaSeHaVagasDisponiveis(List<VagaAnonimaDTO> vagasDisponiveis) {
        if (vagasDisponiveis.isEmpty()) {
            throw new SemVagasDisponiveisException()
        }
    }
    

    void curtirVaga(Long idCandidato) {
        List<VagaAnonimaDTO> vagasDisponiveis = vagasDisponiveisParaCandidato(idCandidato)
        println(vagasDisponiveis)

        try {
            verificaSeHaVagasDisponiveis(vagasDisponiveis)
            Long idVaga = solicitarId("Digite o ID da vaga: ")

            tentarCurtirVaga(idVaga, idCandidato, vagasDisponiveis)
        } catch (InputMismatchException e) {
            println("Entrada inválida! Digite um ID numérico." + e.getMessage())
        } catch (VagaNaoEncontradaException e) {
            e.getMessage()
        } catch (SemVagasDisponiveisException e) {
            e.getMessage()
        }
    }


    private void tentarCurtirVaga(Long idVaga, Long idCandidato, List<VagaAnonimaDTO> vagasDisponiveis) {
        if (!verificaSeExisteVaga(idVaga, vagasDisponiveis)) {
            throw new VagaNaoEncontradaException(idVaga)
        }

        vagaService.curtir(idCandidato, idVaga)
        println("Vaga Curtida com sucesso!")
    }

    private CadastrarCandidatoDTO capturarDadosCadastrar() {
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
        AtualizarCandidatoDTO candidatoAtualizado = captuarDadosAtualizar(candidato)
        tentarAtualizarCandidato(candidatoAtualizado)
    }

    private AtualizarCandidatoDTO captuarDadosAtualizar(Candidato candidato) {
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

        return new AtualizarCandidatoDTO(
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

    private boolean verificaSeHaCompetenciasDisponiveis(List<String> competenciasDisponiveis) {
        return !competenciasDisponiveis.isEmpty()
    }

    private List<String> capturarCompetencias(List<String> competenciasAtuais) {
        List<String> competenciasDisponiveis = competenciasDisponiveis(competenciasAtuais)

        while (verificaSeHaCompetenciasDisponiveis(competenciasDisponiveis)) {

            int opcao = capturarEscolha("""
        Competencias atuais: ${competenciasAtuais}

        1 - Digitar nova competencia
        2 - Parar
        """)

            if (opcao == 2) {
                break
            }

            String competenciaEscolhida = escolherCompetencia(competenciasDisponiveis)

            try {
                tentarAdicionarCompetencia(competenciaEscolhida, competenciasAtuais ,competenciasDisponiveis)
            } catch (CompetenciaNaoEncontradaException e) {
                println(e.getMessage())
            }
        }

        return competenciasAtuais
    }

    private void tentarAdicionarCompetencia(String competencia,
                                   List<String> competenciasAtuais,
                                   List<String> competenciasDisponiveis) {
        if (competenciasDisponiveis.contains(competencia)) {
            competenciasDisponiveis.remove(competencia)
            competenciasAtuais.add(competencia)
        } else {
            throw new CompetenciaNaoEncontradaException(competencia)
        }
    }


    private String escolherCompetencia(List<String> competenciasDisponiveis) {
        println("""
        Competencias disponiveis: ${competenciasDisponiveis}

        Digite uma competencia:
        """)

        return scanner.nextLine().trim().toUpperCase()
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
        List<String> competenciasAtuais = competenciasCandidato(idCandidato)
        List<String> novasCompetencias = capturarCompetencias(competenciasAtuais)
        candidatoService.adicionarCompetencias(idCandidato, novasCompetencias)
    }

    void verMatches(Long idCandidato) {
        println(matchService.verMatchesPorCandidato(idCandidato))
    }
    
    private void tentarRemoverCompetencia(Long idCandidato, Long idCompetencia) {
        try {
            candidatoService.removerCompetencia(idCandidato, idCompetencia)
            println("Competencia removida com sucesso!")
        } catch (CandidatoNaoEncontradoPorIdException e) {
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
