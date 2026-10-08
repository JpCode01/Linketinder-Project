package com.jpcode.view.empresa

import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.match.MatchController
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.candidato.SemCandidatosDisponiveisException
import com.jpcode.exception.vaga.SemVagasDisponiveisException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.view.vaga.MenuVagaCrud

class MenuCurtirCandidato {

    final Scanner scanner
    final MenuVagaCrud menuVagaCrud
    final EmpresaController empresaController
    final MatchController matchController

    MenuCurtirCandidato(Scanner scanner, MenuVagaCrud menuVagaCrud, EmpresaController empresaController, MatchController matchController) {
        this.scanner = scanner
        this.menuVagaCrud = menuVagaCrud
        this.empresaController = empresaController
        this.matchController = matchController
    }

    void escolherVagaParaCurtirCandidato(Long idEmpresa) {
        List<VagaEmpresaDTO> vagasEmpresa = menuVagaCrud.verVagasEmpresa(idEmpresa)
        println(vagasEmpresa)

        try {
            verificaSeHaVagas(vagasEmpresa)
            Long idVaga = solicitarId("Digite o ID da vaga: ")

            escolherCandidatoParaCurtir(idEmpresa, idVaga)
        } catch (VagaNaoEncontradaException e) {
            println(e.getMessage())
        }
    }

    void verCandidatosCurtidos(Long idEmpresa) {
        println(empresaController.buscarCandidatosCurtidos(idEmpresa))
    }

    private void escolherCandidatoParaCurtir(Long idEmpresa, Long idVaga) {
        List<CandidatoAnonimoDTO> candidatosQueCurtiram = empresaController.buscarCandidatosQueCurtiram(idVaga)
        println(candidatosQueCurtiram)

        try {
            verificaSeHaCandidatos(candidatosQueCurtiram)
            Long idCandidato = solicitarId("Digite o ID do candidato:")

            tentarCurtirCandidato(idEmpresa, idVaga, idCandidato, candidatosQueCurtiram)
        } catch (CandidatoNaoEncontradoPorIdException e) {
            println(e.getMessage())
        }
    }

    private void tentarCurtirCandidato(Long idEmpresa, Long idVaga, Long idCandidato, List<CandidatoAnonimoDTO> candidatosQueCurtiram) {
        if (!tentarProcurarCandidato(idCandidato, candidatosQueCurtiram)) {
            throw new CandidatoNaoEncontradoPorIdException(idCandidato)
        }

        if (confirmarCurtida()) {
            curtirCandidato(idCandidato, idEmpresa, idVaga)
        }
    }

    private void curtirCandidato(
            Long idCandidato,
            Long idEmpresa,
            Long idVaga) {

        matchController.curtirCandidato(idEmpresa, idCandidato, idVaga)

        println("Curtida registrada com sucesso!")
    }

    private boolean confirmarCurtida() {
        println("Deseja curtir o candidato? (s/n)")
        String resposta = scanner.nextLine().trim().toLowerCase()

        return resposta == "s"
    }

    private boolean tentarProcurarCandidato(Long idCandidato, List<CandidatoAnonimoDTO> candidatosEncontrados) {
        return candidatosEncontrados.any {
            CandidatoAnonimoDTO candidatoAnonimoDTO -> candidatoAnonimoDTO.id == idCandidato
        }
    }

    private void verificaSeHaCandidatos(List<CandidatoAnonimoDTO> candidatosDisponiveis) {
        if (candidatosDisponiveis.isEmpty()) {
            throw new SemCandidatosDisponiveisException()
        }
    }

    private void verificaSeHaVagas(List<VagaEmpresaDTO> vagasDisponiveis) {
        if (vagasDisponiveis.isEmpty()) {
            throw new SemVagasDisponiveisException()
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
}
