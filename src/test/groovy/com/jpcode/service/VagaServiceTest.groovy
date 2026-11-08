package com.jpcode.service

import com.jpcode.dao.contratos.*
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.model.core.Empresa
import com.jpcode.model.core.Recrutador
import com.jpcode.model.referencia.Competencia
import spock.lang.Specification

class VagaServiceTest extends Specification {

    ReferenciaRepository<Competencia> referenciaRepository = Mock()
    ReferenciaCompetenciaRepository referenciaCompetencia = Mock()
    CompetenciaRepository competenciaRepository = Mock()
    VagaRepository vagaRepository = Mock()
    CurtidaRepository<VagaAnonimaDTO> curtidaRepository = Mock()
    UsuarioRepository empresaRepository = Mock()
    RecrutadorRepository recrutadorRepository = Mock()

    VagaService service

    def setup() {
        service = new VagaService(
                vagaRepository,
                referenciaRepository,
                referenciaCompetencia,
                competenciaRepository,
                curtidaRepository,
                empresaRepository,
                recrutadorRepository
        )
        empresaRepository.buscarPorId(10L) >> Optional.of(empresa(10L))
        empresaRepository.buscarPorId(1L) >> Optional.of(empresa(1L))
    }

    def "deve criar vaga pela empresa"() {
        given:
        CadastrarVagaDTO cadastrarVagaDTO = new CadastrarVagaDTO(
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L,
                []
        )

        Vaga vagaSalva = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        when:
        service.criarVagaEmpresa(10L, cadastrarVagaDTO)

        then:
        1 * vagaRepository.salvarComCompetencias({ it.idEmpresa == 10L && it.idRecrutador == null }, []) >> vagaSalva
        0 * competenciaRepository.salvar(_, _)
    }

    def "deve criar vaga e cadastrar competencias"() {
        given:
        CadastrarVagaDTO cadastrarVagaDTO = new CadastrarVagaDTO(
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L,
                ["JAVA", "SPRING"]
        )

        Vaga vagaSalva = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        referenciaRepository.buscarIdPorNome("JAVA") >> Optional.of(1L)
        referenciaRepository.buscarIdPorNome("SPRING") >> Optional.of(2L)

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaRepository.salvarComCompetencias(_, [1L, 2L]) >> vagaSalva
        0 * competenciaRepository.salvar(_, _)
    }

    def "deve lançar exceção quando competencia não existir"() {
        given:
        CadastrarVagaDTO cadastrarVagaDTO = new CadastrarVagaDTO(
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L,
                ["JAVA"]
        )

        Vaga vagaSalva = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        referenciaRepository.buscarIdPorNome("JAVA") >> Optional.empty()

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        0 * vagaRepository.salvarComCompetencias(_, _)
        thrown(CompetenciaNaoEncontradaException)
        0 * competenciaRepository.salvar(_, _)
    }

    def "deve curtir vaga"() {
        when:
        service.curtir(1L, 5L)

        then:
        1 * curtidaRepository.salvar(1L, 5L)
    }

    def "deve listar vagas da empresa"() {
        given:
        List<VagaEmpresaDTO> vagas = []

        vagaRepository.buscarVagasEmpresa(1L) >> vagas

        expect:
        service.listarVagas(1L) == vagas
    }

    def "deve buscar vaga pelo id"() {
        given:
        Vaga vaga = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        vagaRepository.buscarPorId(1L) >> Optional.of(vaga)

        expect:
        service.buscarVaga(1L) == vaga
    }

    def "deve lançar exceção quando vaga não for encontrada pelo id"() {
        given:
        vagaRepository.buscarPorId(1L) >> Optional.empty()

        when:
        service.buscarVaga(1L)

        then:
        thrown(VagaNaoEncontradaException)
    }

    def "deve listar vagas curtidas pelo candidato"() {
        given:
        List<VagaAnonimaDTO> vagas = []

        curtidaRepository.buscarCurtidas(1L) >> vagas

        expect:
        service.listarVagasCurtidas(1L) == vagas
    }

    def "deve buscar todas as vagas"() {
        given:
        List<VagaAnonimaDTO> vagas = []

        vagaRepository.buscarTodasAsVagas() >> vagas

        expect:
        service.buscarTodasAsVagas() == vagas
    }

    def "deve buscar competencias da vaga"() {
        given:
        List<Competencia> competencias = [
                new Competencia(1L, "JAVA"),
                new Competencia(2L, "SPRING")
        ]

        competenciaRepository.buscarPorId(1L) >> competencias

        expect:
        service.buscarCompetenciasDeVaga(1L) == competencias
    }

    def "deve deletar vaga"() {
        when:
        service.deletarVaga(1L)

        then:
        1 * vagaRepository.deletar(1L)
    }

    def "deve atualizar vaga"() {
        given:
        AtualizarVagaDTO atualizarVagaDTO = new AtualizarVagaDTO(
                1L,
                "Desenvolvedor Java Senior",
                "Desenvolvimento de APIs REST",
                "São Paulo",
                10L
        )
        vagaRepository.buscarPorId(1L) >> Optional.of(new Vaga(1L, 'Antes', 'Descrição', 'SP', 10L))

        when:
        service.atualizarVaga(atualizarVagaDTO)

        then:
        1 * vagaRepository.atualizarDados(_)
    }

    def "deve buscar competencias da vaga em String"() {
        given:
        List<String> competencias = ["JAVA", "SPRING"]

        competenciaRepository.buscarEmString(1L) >> competencias

        expect:
        service.competenciasEmString(1L) == competencias
    }

    def "deve adicionar competencias a vaga"() {
        given:
        Competencia java = new Competencia(1L, "JAVA")
        Competencia spring = new Competencia(2L, "SPRING")

        referenciaCompetencia.buscarPorNome("JAVA") >> java
        referenciaCompetencia.buscarPorNome("SPRING") >> spring

        when:
        service.adicionarCompetencias(
                1L,
                ["JAVA", "SPRING"]
        )

        then:
        1 * competenciaRepository.atualizar(
                1L,
                [java, spring]
        )
    }

    def 'criação do recrutador deriva empresa e autoria e elimina competências repetidas'() {
        given:
        recrutadorRepository.buscarPorId(4L) >> Optional.of(new Recrutador(id: 4L, idEmpresa: 10L, ativo: true))
        referenciaRepository.buscarIdPorNome('RH') >> Optional.of(7L)

        when:
        service.criarVagaRecrutador(4L, new CadastrarVagaDTO('Vaga', 'Descrição', 'SP', null, ['RH', 'RH']))

        then:
        1 * vagaRepository.salvarComCompetencias({ it.idEmpresa == 10L && it.idRecrutador == 4L }, [7L])
    }

    def 'não aceita empresa injetada na criação do recrutador'() {
        given:
        recrutadorRepository.buscarPorId(4L) >> Optional.of(new Recrutador(id: 4L, idEmpresa: 10L, ativo: true))

        when:
        service.criarVagaRecrutador(4L, new CadastrarVagaDTO('Vaga', 'Descrição', 'SP', 99L, []))

        then:
        thrown(IllegalArgumentException)
        0 * vagaRepository.salvarComCompetencias(_, _)
    }

    def 'consulta rejeita outro titular antes de acessar vagas'() {
        when:
        if (perfil == 'empresa') service.listarVagasEmpresa(1L, 2L)
        else service.listarVagasRecrutador(1L, 2L)

        then:
        thrown(IllegalArgumentException)
        0 * vagaRepository._

        where:
        perfil << ['empresa', 'recrutador']
    }

    def 'recrutador inativo não cria nem consulta'() {
        given:
        recrutadorRepository.buscarPorId(4L) >> Optional.of(new Recrutador(id: 4L, idEmpresa: 10L, ativo: false))

        when:
        if (criar) service.criarVagaRecrutador(4L, new CadastrarVagaDTO('Vaga', 'Descrição', 'SP', null, []))
        else service.listarVagasRecrutador(4L, 4L)

        then:
        thrown(IllegalStateException)
        0 * vagaRepository._

        where:
        criar << [true, false]
    }

    def 'atualização não permite usar empresa alheia'() {
        given:
        vagaRepository.buscarPorId(3L) >> Optional.of(new Vaga(3L, 'Vaga', 'Descrição', 'SP', 99L))

        when:
        service.atualizarVaga(new AtualizarVagaDTO(3L, 'Outro', 'Descrição', 'SP', 10L))

        then:
        thrown(IllegalArgumentException)
        0 * vagaRepository.atualizarDados(_)
    }

    private static Empresa empresa(Long id) {
        return new Empresa(id, 'Empresa', 'empresa@teste', 'hash', 'cnpj', 1L, 1L, 'cep', 'Descrição', true)
    }
}
