package com.jpcode.service

import com.jpcode.validation.CompetenciaValidation
import spock.lang.Specification
import com.jpcode.enums.CompetenciasEnum

class CandidatoServiceTest extends Specification {
    def "Deve cadastrar um candidato"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> true
        }
        def service = new CandidatoService(validation)

        when:
        def candidato = service.cadastrarCandidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend",
                ["JAVA"]
        )

        then:
        candidato.nome == "João"
        candidato.email == "joao@email.com"
        candidato.cpf == "123456789"
        candidato.idade == 20
        candidato.estado == "SP"
        candidato.cep == "12900000"
        candidato.descricao == "Desenvolvedor backend"
        candidato.competencias == [CompetenciasEnum.JAVA]
    }

    def "Deve cadastrar um candidato sem competências"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> true
        }
        def service = new CandidatoService(validation)

        when:
        def candidato = service.cadastrarCandidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend",
                []
        )

        then:
        candidato.nome == "João"
        candidato.email == "joao@email.com"
        candidato.cpf == "123456789"
        candidato.idade == 20
        candidato.estado == "SP"
        candidato.cep == "12900000"
        candidato.descricao == "Desenvolvedor backend"
        candidato.competencias.isEmpty()
    }

    def "Não deve adicionar competência inválida"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> false
        }
        def service = new CandidatoService(validation)

        when:
        def candidato = service.cadastrarCandidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend",
                ["FIGMA"]
        )

        then:
        candidato.nome == "João"
        candidato.email == "joao@email.com"
        candidato.cpf == "123456789"
        candidato.idade == 20
        candidato.estado == "SP"
        candidato.cep == "12900000"
        candidato.descricao == "Desenvolvedor backend"
        candidato.competencias.isEmpty()
    }

    def "Deve normalizar competência para maiúsculo"() {
        given:
        def validation = Mock(CompetenciaValidation)
        def service = new CandidatoService(validation)

        when:
        service.cadastrarCandidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend",
                ["java"]
        )

        then:
        1 * validation.validarCompetencia("JAVA", _)
    }

    def "Deve cadastrar varias competencias validas"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> true
        }
        def service = new CandidatoService(validation)

        when:
        def candidato = service.cadastrarCandidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend",
                ["JAVA", "GROOVY", "PYTHON", "SPRING"]
        )

        then:
        candidato.competencias == [
                CompetenciasEnum.JAVA,
                CompetenciasEnum.GROOVY,
                CompetenciasEnum.PYTHON,
                CompetenciasEnum.SPRING
        ]
    }

    def "Deve cadastrar apenas as cometencias validas"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia("JAVA", _) >> true
            validarCompetencia("PHP", _) >> false
        }
        def service = new CandidatoService(validation)

        when:
        def candidato = service.cadastrarCandidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend",
                ["JAVA", "PHP"]
        )

        then:
        candidato.competencias == [CompetenciasEnum.JAVA]
    }





}
