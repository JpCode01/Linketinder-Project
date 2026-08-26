package com.jpcode.service

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Candidato
import com.jpcode.model.Vaga
import com.jpcode.validation.CompetenciaValidation
import spock.lang.Specification

class EmpresaServiceTest extends Specification {
    def "Deve cadastrar uma empresa"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> true
        }
        def service = new EmpresaService(validation)

        when:
        def empresa = service.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["PYTHON"]
        )

        then:
        empresa.nome == "Google"
        empresa.email == "google@email.com"
        empresa.cnpj == "17.593.554/0001-42"
        empresa.pais == "Estados Unidos"
        empresa.estado == "Washington"
        empresa.cep == "12900000"
        empresa.descricao == "Empresa WEB"
        empresa.competencias == [CompetenciasEnum.PYTHON]
    }

    def "Deve cadastrar uma empresa sem competencia"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> true
        }
        def service = new EmpresaService(validation)

        when:
        def empresa = service.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                []
        )

        then:
        empresa.nome == "Google"
        empresa.email == "google@email.com"
        empresa.cnpj == "17.593.554/0001-42"
        empresa.pais == "Estados Unidos"
        empresa.estado == "Washington"
        empresa.cep == "12900000"
        empresa.descricao == "Empresa WEB"
        empresa.competencias.isEmpty()
    }

    def "Não deve adicionar competencia invalida"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> false
        }
        def service = new EmpresaService(validation)

        when:
        def empresa = service.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["PHP"]
        )

        then:
        empresa.nome == "Google"
        empresa.email == "google@email.com"
        empresa.cnpj == "17.593.554/0001-42"
        empresa.pais == "Estados Unidos"
        empresa.estado == "Washington"
        empresa.cep == "12900000"
        empresa.descricao == "Empresa WEB"
        empresa.competencias.isEmpty()
    }

    def "Deve normalizar competência para maiúsculo"() {
        given:
        def validation = Mock(CompetenciaValidation)
        def service = new EmpresaService(validation)

        when:
        def empresa = service.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["python"]
        )

        then:
        1 * validation.validarCompetencia("PYTHON", _)
    }

    def "Deve cadastrar varias competencias validas"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia(_, _) >> true
        }
        def service = new EmpresaService(validation)

        when:
        def empresa = service.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA", "PYTHON"]
        )

        then:
        empresa.competencias == [
                CompetenciasEnum.JAVA,
                CompetenciasEnum.PYTHON
        ]
    }

    def "Deve cadastrar apenas as competencias validas"() {
        given:
        def validation = Stub(CompetenciaValidation) {
            validarCompetencia("JAVA", _) >> true
            validarCompetencia("PYTHON", _) >> true
            validarCompetencia("PHP", _) >> false
        }
        def service = new EmpresaService(validation)

        when:
        def empresa = service.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA", "PYTHON", "PHP"]
        )

        then:
        empresa.competencias == [
                CompetenciasEnum.JAVA,
                CompetenciasEnum.PYTHON
        ]
    }

    def "Deve curtir candidato adicionando em candidatosCurtidos"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
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
        
        when:
            empresaService.curtirCandidato(candidato, empresa)
        then:
            empresa.candidatosCurtidos == [candidato]
    }

    def "Não deve adicionar candidato inexistente"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        def candidato = null

        when:
        empresaService.curtirCandidato(candidato, empresa)
        then:
        empresa.candidatosCurtidos == []
    }

    def "Deve adicionar candidatos na lista"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        def candidato1 = new Candidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend"
        )
        candidato1.competencias = ["JAVA"]
        def candidato2 = new Candidato(
                "Maria",
                "Maria@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend"
        )
        candidato2.competencias = ["JAVA"]

        when:
        empresaService.curtirCandidato(candidato1, empresa)
        empresaService.curtirCandidato(candidato2, empresa)
        then:
        empresa.candidatosCurtidos == [candidato1, candidato2]
    }

    def "Não deve adicionar candidatos iguais na lista"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        def candidato1 = new Candidato(
                "João",
                "joao@email.com",
                "123456789",
                20,
                "SP",
                "12900000",
                "Desenvolvedor backend"
        )
        candidato1.competencias = ["JAVA"]

        when:
        empresaService.curtirCandidato(candidato1, empresa)
        empresaService.curtirCandidato(candidato1, empresa)
        then:
        empresa.candidatosCurtidos == [candidato1]
    }

    def "Deve listar vagas da empresa"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        Vaga vaga = new Vaga("Dev Java", "Dev")
        when:
        empresa.adicionarVaga(vaga)
        then:
        empresaService.ListarVagasPorEmpresa(empresa) == [vaga]
    }

    def "Deve exibir a lista de vagas vázia"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        def vaga = null
        when:
        empresa.adicionarVaga(vaga)
        then:
        empresaService.ListarVagasPorEmpresa(empresa) == []
    }

    def "Deve adicionar duas vagas iguais e exibir apenas a primeira"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        def vaga = new Vaga("Dev Java", "Dev")
        when:
        empresa.adicionarVaga(vaga)
        empresa.adicionarVaga(vaga)
        then:
        empresaService.ListarVagasPorEmpresa(empresa) == [vaga]
    }

    def "Deve adicionar duas vagas e exibir as duas"() {
        given:
        EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        def empresa = empresaService.cadastrarEmpresa(
                "Google",
                "google@email.com",
                "17.593.554/0001-42",
                "Estados Unidos",
                "Washington",
                "12900000",
                "Empresa WEB",
                ["JAVA"]
        )
        def vaga1 = new Vaga("Dev Java", "Dev")
        def vaga2 = new Vaga("Dev Python", "Dev")
        when:
        empresa.adicionarVaga(vaga1)
        empresa.adicionarVaga(vaga2)
        then:
        empresaService.ListarVagasPorEmpresa(empresa) == [vaga1, vaga2]
    }







}
