package com.jpcode.view.empresa

import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.candidato.SemCandidatosDisponiveisException
import com.jpcode.exception.vaga.SemVagasDisponiveisException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.service.EmpresaService
import com.jpcode.service.MatchService
import com.jpcode.view.vaga.MenuVagaCrud

class MenuCurtirCandidato {

    final Scanner scanner
    final MenuVagaCrud menuVagaCrud
    final EmpresaService empresaService
    final MatchService matchService

    MenuCurtirCandidato(Scanner scanner, MenuVagaCrud menuVagaCrud, EmpresaService empresaService, MatchService matchService) {
        this.scanner = scanner
        this.menuVagaCrud = menuVagaCrud
        this.empresaService = empresaService
        this.matchService = matchService
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
        println(empresaService.buscarCandidatosCurtidos(idEmpresa))
    }

    private void escolherCandidatoParaCurtir(Long idEmpresa, Long idVaga) {
        List<CandidatoAnonimoDTO> candidatosQueCurtiram = empresaService.buscarCandidatosQueCurtiram(idVaga)
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

        empresaService.curtirCandidato(idEmpresa, idCandidato)
        matchService.salvar(idCandidato, idEmpresa, idVaga)

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
