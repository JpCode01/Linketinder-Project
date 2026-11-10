package com.jpcode.integracao

import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.recrutador.RecrutadorDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.service.VagaService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

import java.sql.SQLException

import static org.junit.jupiter.api.Assumptions.assumeTrue

class VagaAutoriaTest extends Specification {
    SuporteContasPostgres banco
    VagaDAO dao
    CompetenciasVagaDAO competencias
    VagaService service

    def setup() {
        assumeTrue(SuportePostgres.configurado(), 'Integração NÃO EXECUTADA: configure o banco dedicado de testes.')
        banco = new SuporteContasPostgres()
        executar("""
            INSERT INTO empresas (nome, cnpj, email_corporativo, id_pais, cep)
            VALUES ('Empresa A', 'cnpj-a', 'a@teste', 1, 'cep'),
                   ('Empresa B', 'cnpj-b', 'b@teste', 1, 'cep');
            INSERT INTO areas_recrutamento (nome) VALUES ('Administração');
            INSERT INTO recrutadores (nome, id_area_recrutamento, email, cpf, data_nascimento, senha, id_empresa)
            VALUES ('Autor A', 1, 'r1@teste', 'cpf-1', '1990-01-01', 'fixture-sem-login', 1),
                   ('Autor B', 2, 'r2@teste', 'cpf-2', '1990-01-01', 'fixture-sem-login', 1),
                   ('Autor C', 1, 'r3@teste', 'cpf-3', '1990-01-01', 'fixture-sem-login', 2);
            INSERT INTO competencias (nome_competencia) VALUES ('RH'), ('ADMINISTRAÇÃO');
        """)
        competencias = new CompetenciasVagaDAO(banco.factory)
        dao = new VagaDAO(banco.factory, competencias)
        def referencia = new CompetenciaDAO(banco.factory)
        service = new VagaService(dao, referencia, referencia, competencias, null,
                new EmpresaDAO(banco.factory), new RecrutadorDAO(banco.factory))
    }

    def cleanup() { banco?.close() }

    def 'empresa vê vagas diretas e autorais e cada recrutador vê somente as próprias'() {
        when:
        service.criarVagaEmpresa(1L, dados('Direta', 1L))
        service.criarVagaRecrutador(1L, dados('Autor A'))
        service.criarVagaRecrutador(2L, dados('Autor B'))
        service.criarVagaRecrutador(3L, dados('Outra empresa'))
        def vagas = service.listarVagasEmpresa(1L, 1L)

        then:
        vagas*.nome == ['Direta', 'Autor A', 'Autor B']
        vagas*.idRecrutador == [null, 1L, 2L]
        vagas*.nomeRecrutador == [null, 'Autor A', 'Autor B']
        vagas*.areaRecrutamento == [null, 'Recursos Humanos', 'Administração']
        vagas.every { it.competencias*.nome as Set == ['RH', 'ADMINISTRAÇÃO'] as Set }
        vagas.every { competencias.buscarPorId(it.id)*.id as Set == [1L, 2L] as Set }
        service.listarVagasRecrutador(1L, 1L)*.nome == ['Autor A']
        service.listarVagasRecrutador(2L, 2L)*.nome == ['Autor B']
        service.listarVagasRecrutador(3L, 3L)*.nome == ['Outra empresa']
        service.listarVagasEmpresa(2L, 2L)*.nome == ['Outra empresa']
        dao.buscarPorId(vagas[0].id).orElseThrow().idRecrutador == null
        dao.buscarPorId(vagas[1].id).orElseThrow().with { idEmpresa == 1L && idRecrutador == 1L }
    }

    def 'empresa mantém autoria histórica após inatividade e recrutador deixa de operar'() {
        given:
        service.criarVagaRecrutador(1L, dados('Histórica'))
        executar('UPDATE recrutadores SET ativo = false WHERE id = 1')

        expect:
        service.listarVagasEmpresa(1L, 1L)[0].with {
            idRecrutador == 1L && nomeRecrutador == 'Autor A' && areaRecrutamento == 'Recursos Humanos'
        }
        dao.buscarVagasRecrutador(1L).empty

        when:
        service.listarVagasRecrutador(1L, 1L)

        then:
        thrown(IllegalStateException)

        when:
        service.criarVagaRecrutador(1L, dados('Proibida'))

        then:
        thrown(IllegalStateException)
        contar('vagas') == 1
    }

    def 'competência inexistente é rejeitada antes da criação'() {
        when:
        service.criarVagaEmpresa(1L, new CadastrarVagaDTO('Inválida', 'Descrição', 'SP', 1L, ['RH', 'Inexistente']))

        then:
        thrown(CompetenciaNaoEncontradaException)
        contar('vagas') == 0
        contar('vagas_competencias') == 0
    }

    def 'falha na segunda associação reverte vaga e primeira associação e permite nova transação'() {
        given:
        Vaga vaga = new Vaga('Rollback', 'Descrição', 'SP', 1L)
        vaga.idRecrutador = 1L

        when:
        dao.salvarComCompetencias(vaga, [1L, -1L])

        then:
        SQLException erro = thrown()
        erro.SQLState == '23503'
        vaga.id == null
        contar('vagas') == 0
        contar('vagas_competencias') == 0

        when:
        dao.salvarComCompetencias(vaga, [1L, 2L])

        then:
        vaga.id != null
        contar('vagas') == 1
        contar('vagas_competencias') == 2
    }

    def 'DAO revalida empresa e autoria antes de gravar: #cenario'() {
        given:
        if (inativar) executar(inativar)
        Vaga vaga = new Vaga('Inválida', 'Descrição', 'SP', idEmpresa)
        vaga.idRecrutador = idRecrutador

        when:
        dao.salvarComCompetencias(vaga, [1L])

        then:
        thrown(IllegalArgumentException)
        contar('vagas') == 0
        contar('vagas_competencias') == 0

        where:
        cenario              | idEmpresa | idRecrutador | inativar
        'vínculo divergente' | 2L        | 1L           | null
        'autor inexistente'  | 1L        | -1L          | null
        'empresa inexistente'| -1L       | null         | null
        'autor inativo'      | 1L        | 1L           | 'UPDATE recrutadores SET ativo = false WHERE id = 1'
        'empresa inativa'    | 1L        | null         | 'UPDATE empresas SET ativo = false WHERE id = 1'
    }

    def 'atualização preserva propriedade e autoria histórica e rejeita empresa divergente'() {
        given:
        service.criarVagaRecrutador(1L, dados('Original'))
        Long id = service.listarVagasEmpresa(1L, 1L)[0].id
        executar('UPDATE recrutadores SET ativo = false WHERE id = 1')

        when:
        service.atualizarVaga(new AtualizarVagaDTO(id, 'Atualizada', 'Descrição nova', 'RJ', 1L))

        then:
        dao.buscarPorId(id).orElseThrow().with {
            nome == 'Atualizada' && idEmpresa == 1L && idRecrutador == 1L
        }

        when:
        dao.atualizarDados(new Vaga(id, 'Ataque', 'Descrição', 'SP', 2L))

        then:
        thrown(IllegalArgumentException)
        dao.buscarPorId(id).orElseThrow().nome == 'Atualizada'
    }

    def 'descoberta continua com chaves exatas anônimas apesar da autoria persistida'() {
        given:
        service.criarVagaRecrutador(1L, dados('Identificação interna'))

        when:
        def json = new JsonSlurper().parseText(JsonOutput.toJson(service.buscarTodasAsVagas()))

        then:
        json.size() == 1
        json[0].keySet() == ['id', 'descricao', 'competencias'] as Set
        json[0].competencias*.nome as Set == ['RH', 'ADMINISTRAÇÃO'] as Set
    }

    private static CadastrarVagaDTO dados(String nome, Long empresa = null) {
        return new CadastrarVagaDTO(nome, 'Descrição', 'SP', empresa, ['RH', 'ADMINISTRAÇÃO', 'RH'])
    }

    private void executar(String sql) {
        try (def conexao = banco.abrirConexao(); def comando = conexao.createStatement()) {
            comando.execute(sql)
        }
    }

    private long contar(String tabela) {
        try (def conexao = banco.abrirConexao(); def comando = conexao.createStatement();
             def resultado = comando.executeQuery("SELECT count(*) FROM ${tabela}".toString())) {
            resultado.next()
            return resultado.getLong(1)
        }
    }
}
