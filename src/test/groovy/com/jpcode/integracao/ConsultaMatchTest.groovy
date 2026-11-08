package com.jpcode.integracao

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.match.MatchDAO
import com.jpcode.dao.recrutador.RecrutadorDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.relacionamento.EmpresaCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.facade.MatchFacade
import com.jpcode.service.MatchService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import java.sql.Connection
import static org.junit.jupiter.api.Assumptions.assumeTrue

/** T10: DAOs reais; todas as conexões e a limpeza usam o banco dedicado validado. */
class ConsultaMatchTest extends Specification {
    SuporteContasPostgres banco
    MatchDAO matches
    MatchService service
    MatchFacade facade
    CandidatoCurtirDAO interesses

    def setup() {
        assumeTrue(SuportePostgres.configurado(), 'Integração NÃO EXECUTADA: configure o banco dedicado de testes.')
        banco = new SuporteContasPostgres()
        executar('''
            INSERT INTO areas_recrutamento (nome) VALUES ('Administração');
            INSERT INTO empresas (nome, cnpj, email_corporativo, descricao, id_pais, id_estado, cep, senha)
            VALUES ('Empresa A', 'cnpj-a', 'a@teste', 'Descrição empresarial A', 1, 1, 'cep', 'segredo-empresa'),
                   ('Empresa B', 'cnpj-b', 'b@teste', 'Descrição empresarial B', 1, 1, 'cep', 'segredo-empresa-b');
            INSERT INTO recrutadores (nome, id_area_recrutamento, email, cpf, data_nascimento, descricao, senha, id_empresa)
            VALUES ('Autor A', 1, 'r1@teste', 'cpf-r1', '1990-01-01', 'Privada A', 'segredo-r1', 1),
                   ('Colega A', 2, 'r2@teste', 'cpf-r2', '1991-01-01', 'Privada B', 'segredo-r2', 1),
                   ('Autor B', 2, 'r3@teste', 'cpf-r3', '1992-01-01', 'Privada C', 'segredo-r3', 2);
            INSERT INTO candidatos (nome, sobrenome, email, senha, data_nascimento, cpf, id_pais, id_estado, idade, cep, descricao)
            VALUES ('Pessoa', 'Candidata', 'c1@teste', 'segredo-c1', '1990-01-01', 'cpf-c1', 1, 1, 36, 'cep', 'Descrição anônima'),
                   ('Outra', 'Pessoa', 'c2@teste', 'segredo-c2', '1992-01-01', 'cpf-c2', 1, 1, 34, 'cep', 'Outra descrição');
            INSERT INTO vagas (nome, descricao, local, id_empresa, id_recrutador)
            VALUES ('Autoral A', 'Descrição A', 'Local privado', 1, 1),
                   ('Colega A', 'Descrição B', 'Local privado', 1, 2),
                   ('Direta', 'Descrição C', 'Local privado', 1, NULL),
                   ('Outra empresa', 'Descrição D', 'Local privado', 2, 3);
            INSERT INTO competencias (nome_competencia) VALUES ('RH'), ('Administração');
            INSERT INTO candidatos_competencias (id_candidato, id_competencia) VALUES (1, 1), (2, 2);
            INSERT INTO vagas_competencias (id_vaga, id_competencia) VALUES (1, 1), (2, 2), (3, 2), (4, 1);
        ''')
        matches = new MatchDAO(banco.factory, new CompetenciasCandidatoDAO(banco.factory), new CompetenciasVagaDAO(banco.factory))
        service = new MatchService(matches, new CandidatoDAO(banco.factory), new EmpresaDAO(banco.factory), new RecrutadorDAO(banco.factory))
        interesses = new CandidatoCurtirDAO(banco.factory)
        facade = new MatchFacade(banco.factory, new EmpresaCurtirDAO(banco.factory), interesses, service)
        interesses.salvar(1L, 1L)
        interesses.salvar(1L, 2L)
        interesses.salvar(1L, 3L)
        interesses.salvar(1L, 4L)
        interesses.salvar(2L, 2L)
    }

    def cleanup() { banco?.close() }

    def 'sem retribuicao nenhum observador recebe identificacao pelo contrato de match'() {
        expect:
        service.consultarMatchesCandidato(1L, 1L).empty
        service.consultarMatchesEmpresa(1L, 1L).empty
        service.consultarMatchesRecrutador(1L, 1L).empty
        anonimo(interesses.buscarCurtidas(1L)).size() == 4
        anonimo(facade.buscarInteressadosEmpresa(1L, 1L)).size() == 1
    }

    def 'match empresarial preserva todos os campos e somente nome e area do autor: inativo=#inativo'() {
        given:
        if (inativo) executar('UPDATE recrutadores SET ativo = false WHERE id = 1')
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        when:
        def candidato = json(service.consultarMatchesCandidato(1L, 1L))[0]
        def empresa = json(service.consultarMatchesEmpresa(1L, 1L))[0]
        then:
        candidato == empresa
        candidato == [id: 1, nomeCandidato: 'Pessoa', sobrenomeCandidato: 'Candidata', emailCandidato: 'c1@teste',
                      cpfCandidato: 'cpf-c1', descricaoCandidato: 'Descrição anônima', idadeCandidato: 36,
                      estadoCandidato: 'SP', paisCandidato: 'BRASIL', competenciasCandidato: [[id: 1, nome: 'RH']],
                      nomeEmpresa: 'Empresa A', cnpjEmpresa: 'cnpj-a', emailCorporativo: 'a@teste',
                      descricaoEmpresa: 'Descrição empresarial A', paisEmpresa: 'BRASIL', estadoEmpresa: 'SP',
                      nome: 'Autoral A', descricao: 'Descrição A', competencias: ['RH'],
                      nomeRecrutador: 'Autor A', areaRecrutamento: 'Recursos Humanos']
        matches.buscarMatchesPorRecrutador(1L).size() == (inativo ? 0 : 1)
        anonimo(facade.buscarInteressadosEmpresa(1L, 1L))[0].id == 1
        where:
        inativo << [false, true]
    }

    def 'match direto tem os mesmos campos com os dois campos de recrutador nulos'() {
        given:
        facade.retribuirInteresseEmpresa(1L, 1L, 3L)
        when:
        def dto = json(service.consultarMatchesCandidato(1L, 1L))[0]
        then:
        dto.keySet() == chavesMatch()
        dto.nomeRecrutador == null
        dto.areaRecrutamento == null
        dto.nome == 'Direta'
        dto.competencias == ['Administração']
        service.consultarMatchesRecrutador(1L, 1L).empty
        service.consultarMatchesRecrutador(2L, 2L).empty
    }

    def 'cada titular consulta seu conjunto e match A nao identifica candidato em B'() {
        given:
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        facade.retribuirInteresseEmpresa(1L, 1L, 3L)
        facade.retribuirInteresseRecrutador(3L, 1L, 4L)
        facade.retribuirInteresseRecrutador(2L, 2L, 2L)
        expect:
        service.consultarMatchesEmpresa(1L, 1L)*.nome == ['Autoral A', 'Direta', 'Colega A']
        service.consultarMatchesEmpresa(2L, 2L)*.nome == ['Outra empresa']
        service.consultarMatchesCandidato(1L, 1L)*.nome == ['Autoral A', 'Direta', 'Outra empresa']
        service.consultarMatchesCandidato(2L, 2L)*.nome == ['Colega A']
        service.consultarMatchesRecrutador(1L, 1L)*.nome == ['Autoral A']
        service.consultarMatchesRecrutador(2L, 2L)*.emailCandidato == ['c2@teste']
        service.consultarMatchesRecrutador(3L, 3L)*.nome == ['Outra empresa']
        anonimo(facade.buscarInteressadosRecrutador(2L, 2L))*.id == [1, 2]
        anonimo(facade.buscarInteressadosEmpresa(1L, 1L))*.id == [1]
        anonimo(interesses.buscarCurtidas(1L)).size() == 4
        anonimo(new VagaDAO(banco.factory).buscarTodasAsVagas()).size() == 4
        json(service.consultarMatchesRecrutador(2L, 2L)).every { it.keySet() == chavesMatch() }

        when:
        facade.buscarInteressadosRecrutador(2L, 1L)
        then:
        thrown(SecurityException)
    }

    def 'ID alternativo e titular inativo nao consultam matches: #tipo'() {
        given:
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        when:
        service."consultarMatches${tipo}"(1L, 2L)
        then:
        thrown(SecurityException)

        when:
        executar("UPDATE ${tabela} SET ativo = false WHERE id = 1")
        service."consultarMatches${tipo}"(1L, 1L)
        then:
        thrown(SecurityException)
        matches."buscarMatchesPor${tipo}"(1L).empty
        where:
        tipo         | tabela
        'Candidato'  | 'candidatos'
        'Empresa'    | 'empresas'
        'Recrutador' | 'recrutadores'
    }

    def 'historico permanece para os demais titulares ativos'() {
        given:
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        executar('UPDATE candidatos SET ativo = false WHERE id = 1; UPDATE recrutadores SET ativo = false WHERE id = 1')
        expect:
        service.consultarMatchesEmpresa(1L, 1L)*.nomeRecrutador == ['Autor A']
        service.consultarMatchesEmpresa(1L, 1L)*.cpfCandidato == ['cpf-c1']
        when:
        executar('UPDATE empresas SET ativo = false WHERE id = 1; UPDATE candidatos SET ativo = true WHERE id = 1')
        then:
        service.consultarMatchesCandidato(1L, 1L)*.nomeEmpresa == ['Empresa A']
    }

    private static Set chavesMatch() {
        ['id', 'nomeCandidato', 'sobrenomeCandidato', 'emailCandidato', 'cpfCandidato', 'descricaoCandidato',
         'idadeCandidato', 'estadoCandidato', 'paisCandidato', 'competenciasCandidato', 'nomeEmpresa', 'cnpjEmpresa',
         'emailCorporativo', 'descricaoEmpresa', 'paisEmpresa', 'estadoEmpresa', 'nome', 'descricao', 'competencias',
         'nomeRecrutador', 'areaRecrutamento'] as Set
    }

    private static def json(Object dados) { new JsonSlurper().parseText(JsonOutput.toJson(dados)) }

    private static List anonimo(Object dados) {
        List itens = json(dados)
        assert itens.every { it.keySet() == ['id', 'descricao', 'competencias'] as Set }
        return itens
    }

    private void executar(String sql) {
        try (Connection conexao = banco.abrirConexao(); def comando = conexao.createStatement()) { comando.execute(sql) }
    }
}
