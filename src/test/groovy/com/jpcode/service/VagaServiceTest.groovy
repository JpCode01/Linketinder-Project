package com.jpcode.service

import com.jpcode.model.Candidato
import com.jpcode.model.Empresa
import com.jpcode.model.Vaga
import spock.lang.Specification

class VagaServiceTest extends Specification {
    def "Deve criar vaga"() {
        given:
        VagaService vagaService = new VagaService()
        def empresa = new Empresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB"
        )
        empresa.competencias = ["PYTHON"]
        when:
        def vaga = vagaService.criarVaga(
                empresa,
                "Dev",
                "Desenvolvedor"
        )
        then:
        vaga.nome == "Dev"
        vaga.descricao == "Desenvolvedor"
        vaga.competencias == ["PYTHON"]
    }

    def "Deve retornar uma vaga null ao tentar passar uma empresa null para criar vaga"() {
        given:
        VagaService vagaService = new VagaService()
        def empresa = null
        when:
        def vaga = vagaService.criarVaga(
                empresa,
                "Dev",
                "Desenvolvedor"
        )
        then:
        vaga == null
    }

    def "Deve adicionar candidato que curtiu na lista e candidato deve adicionar vaga curtida na sua lista"() {
        given:
        VagaService vagaService = new VagaService()
        def candidato = new Candidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend"
        )
        candidato.competencias = ["JAVA"]
        def vaga = new Vaga("Dev Java", "Dev")
        when:
        vagaService.curtir(candidato, vaga)
        then:
        candidato.vagasCurtidas == [vaga]
        vaga.candidatosQueCurtiram == [candidato]
    }

    def "Não deve adicionar candidato null nas lista de candidatos que curtiram"() {
        given:
        VagaService vagaService = new VagaService()
        def candidato = null
        def vaga = new Vaga("Dev Java", "Dev")
        when:
        vagaService.curtir(candidato, vaga)
        then:
        vaga.candidatosQueCurtiram == []
    }

    def "Não deve adicionar vaga null nas lista de vagas curtidas"() {
        given:
        VagaService vagaService = new VagaService()
        def candidato = new Candidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend"
        )
        def vaga = null
        when:
        vagaService.curtir(candidato, vaga)
        then:
        candidato.vagasCurtidas == []
    }

    def "Deve listar vagas"() {
        given:
        VagaService vagaService = new VagaService()
        def vaga1 = new Vaga("Dev Java", "Dev")
        def vaga2 = new Vaga("Dev Python", "Dev")
        when:
            def vagas = vagaService.listarVagas([vaga1, vaga2])
        then:
            vagas == [vaga1, vaga2]
    }

    def "Deve retornar lista de vagas vázia"() {
        given:
        VagaService vagaService = new VagaService()
        when:
        def vagas = vagaService.listarVagas([])
        then:
        vagas == []
    }


    
}
