package com.jpcode.view.referencia

import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.service.CompetenciaService

class MenuCompetencia {

    final Scanner scanner
    final CompetenciaService competenciaService

    MenuCompetencia(Scanner scanner, CompetenciaService competenciaService) {
        this.scanner = scanner
        this.competenciaService = competenciaService
    }

    List<String> capturarCompetencias(List<String> competenciasAtuais) {
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

    private boolean verificaSeHaCompetenciasDisponiveis(List<String> competenciasDisponiveis) {
        return !competenciasDisponiveis.isEmpty()
    }

    private List<String> competenciasDisponiveis(List<String> competenciasAtuais) {
        return competenciaService.listarCompetencias() - competenciasAtuais
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
