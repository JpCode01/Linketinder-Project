package com.jpcode.view.candidato

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.match.MatchController
import com.jpcode.controller.referencia.ReferenciaController
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Candidato
import com.jpcode.view.referencia.MenuCompetencia

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MenuCandidatoCrud {

    final Scanner scanner
    final CandidatoController candidatoController
    final MenuCompetencia menuCompetencia
    final ReferenciaController referenciaController
    final MatchController matchController

    MenuCandidatoCrud(Scanner scanner, CandidatoController candidatoController, MenuCompetencia menuCompetencia, ReferenciaController referenciaController, MatchController matchController) {
        this.scanner = scanner
        this.candidatoController = candidatoController
        this.menuCompetencia = menuCompetencia
        this.referenciaController = referenciaController
        this.matchController = matchController
    }

    void cadastrarCandidato() {
        CadastrarCandidatoDTO candidato = capturarDadosCadastrar()
        tentarCandastrarCandidato(candidato)
    }

    boolean apagarCandidato(Long idCandidato, String senha) {
        scanner.nextLine()
        println("Digite sua senha para confirmar (Caso queira desistir, aperte enter): ")
        if (scanner.nextLine() == senha) {
            candidatoController.desativarCandidato(idCandidato)
            println("Conta deletada com sucesso")
            return true
        }
        return false
    }

    void atualizarCandidato(Candidato candidato) {
        AtualizarCandidatoDTO candidatoAtualizado = captuarDadosAtualizar(candidato)
        tentarAtualizarCandidato(candidatoAtualizado)
    }

    void atualizarCompetencias(Long idCandidato) {
        List<String> competenciasAtuais = competenciasCandidato(idCandidato)
        List<String> novasCompetencias = menuCompetencia.capturarCompetencias(competenciasAtuais)
        candidatoController.adicionarCompetencias(idCandidato, novasCompetencias)
    }

    void verMatches(Long idCandidato) {
        println(matchController.verMatchesPorCandidato(idCandidato))
    }

    void removerCompetencia(Long idCandidato) {
        println(candidatoController.listaParaRemover(idCandidato))
        
        Long idCompetencia = solicitarId("Digite o ID da competencia: ")
        tentarRemoverCompetencia(idCandidato, idCompetencia)
    }

    private void tentarRemoverCompetencia(Long idCandidato, Long idCompetencia) {

        try {
            candidatoController.removerCompetencia(idCandidato, idCompetencia)
            println("Competencia removida com sucesso!")
        } catch (CandidatoNaoEncontradoPorIdException e) {
            e.getMessage()
        }
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

    private List<String> competenciasCandidato(Long idCandidato) {
        return candidatoController.competenciasEmString(idCandidato)
    }

    private void tentarAtualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        try {
            candidatoController.atualizarCandidato(atualizarCandidatoDTO)
            println("Candidato atualizado com sucesso!")
        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
            println(e.getMessage())
        }
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
            pais = referenciaController.converterIdPaisParaString(candidato.idPais)
        }

        println("Estado em sigla (SP/RS/RJ):")
        String estado = scanner.nextLine()

        if (estado.isEmpty()) {
            estado = referenciaController.converterIdEstadoParaString(candidato.idEstado)
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

    private void tentarCandastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        try {
            candidatoController.cadastrarCandidato(cadastrarCandidatoDTO)
            println("Candidato cadastrado com sucesso!")
        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException | CompetenciaNaoEncontradaException e) {
            println(e.getMessage())
        }
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

        List<String> competencias = menuCompetencia.capturarCompetencias([])

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

}
