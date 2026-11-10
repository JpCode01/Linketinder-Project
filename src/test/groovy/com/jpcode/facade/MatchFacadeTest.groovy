package com.jpcode.facade

import com.jpcode.dao.contratos.*
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Vaga
import com.jpcode.service.MatchService
import spock.lang.Specification
import java.sql.Connection

class MatchFacadeTest extends Specification {
    Connection conexao = Mock()
    ConnectionFactory factory = Mock()
    RetribuicaoRepository retribuicao = Mock()
    CandidatoCurtidasConsultaRepository consulta = Mock()
    MatchRepository matches = Mock()
    MatchFacade facade = new MatchFacade(factory, retribuicao, consulta, new MatchService(matches))
    boolean contaEmpresarialAtiva = true
    Long vinculoRecrutador = 1L
    boolean contaCandidatoAtiva = true
    Vaga vagaBloqueada = vaga(1L, 4L)
    boolean possuiInteresse = true

    def setup() {
        factory.getConnection() >> conexao
        retribuicao.buscarEmpresaDaVaga(conexao, 3L) >> 1L
        retribuicao.bloquearEmpresaAtiva(conexao, 1L) >> { contaEmpresarialAtiva }
        retribuicao.bloquearRecrutadorAtivo(conexao, 4L) >> { vinculoRecrutador }
        retribuicao.bloquearCandidatoAtivo(conexao, 2L) >> { contaCandidatoAtiva }
        retribuicao.bloquearVaga(conexao, 3L) >> { vagaBloqueada }
        retribuicao.bloquearInteresse(conexao, 2L, 3L) >> { possuiInteresse }
    }

    def 'os dois atores gravam na mesma conexao e confirmam depois dos INSERTs: #recrutador'() {
        when:
        if (recrutador) facade.retribuirInteresseRecrutador(4L, 2L, 3L)
        else facade.retribuirInteresseEmpresa(1L, 2L, 3L)

        then:
        1 * conexao.setAutoCommit(false)
        1 * retribuicao.salvar(conexao, 1L, 2L, 3L, recrutador ? 4L : null)
        then:
        1 * matches.salvar(conexao, 2L, 1L, 3L)
        then:
        1 * conexao.commit()
        0 * conexao.rollback()
        1 * conexao.close()
        where:
        recrutador << [false, true]
    }

    def 'rejeicao de #cenario nao escreve e reverte a transacao'() {
        given:
        contaEmpresarialAtiva = empresaAtiva
        vinculoRecrutador = empresaRecrutador
        contaCandidatoAtiva = candidatoAtivo
        vagaBloqueada = vaga(empresaVaga, autor)
        possuiInteresse = interesse

        when:
        if (recrutador) facade.retribuirInteresseRecrutador(4L, 2L, 3L)
        else facade.retribuirInteresseEmpresa(empresaOperante, 2L, 3L)

        then:
        thrown(RuntimeException)
        0 * retribuicao.salvar(*_)
        0 * matches._
        0 * conexao.commit()
        1 * conexao.rollback()
        1 * conexao.close()

        where:
        cenario              | recrutador | empresaOperante | empresaAtiva | empresaRecrutador | candidatoAtivo | empresaVaga | autor | interesse
        'empresa alheia'     | false      | 9L              | true         | 1L               | true           | 1L          | 4L    | true
        'empresa inativa'    | false      | 1L              | false        | 1L               | true           | 1L          | 4L    | true
        'recrutador inativo' | true       | null            | true         | null             | true           | 1L          | 4L    | true
        'vinculo divergente' | true       | null            | true         | 9L               | true           | 1L          | 4L    | true
        'colega da empresa'  | true       | null            | true         | 1L               | true           | 1L          | 5L    | true
        'vaga direta'        | true       | null            | true         | 1L               | true           | 1L          | null  | true
        'candidato inativo'  | false      | 1L              | true         | 1L               | false          | 1L          | 4L    | true
        'vaga alterada'      | false      | 1L              | true         | 1L               | true           | 9L          | 4L    | true
        'sem interesse'      | false      | 1L              | true         | 1L               | true           | 1L          | 4L    | false
    }

    def 'falha no segundo INSERT reverte e nao confirma'() {
        when:
        facade.retribuirInteresseEmpresa(1L, 2L, 3L)
        then:
        1 * retribuicao.salvar(conexao, 1L, 2L, 3L, null)
        1 * matches.salvar(conexao, 2L, 1L, 3L) >> { throw new IllegalStateException('Falha controlada') }
        thrown(IllegalStateException)
        1 * conexao.rollback()
        0 * conexao.commit()
        1 * conexao.close()
    }

    def 'consulta valida escopo antes de acessar somente o contrato anonimo'() {
        when:
        def resultado = facade.buscarInteressadosRecrutador(4L, 3L)
        then:
        1 * consulta.buscarCandidatosQueCurtiram(conexao, 3L) >> []
        resultado == []
        0 * retribuicao.salvar(*_)
        0 * matches._
        1 * conexao.commit()
    }

    def 'colega nao consulta interessados e nao acessa o repositorio de consulta'() {
        given:
        vagaBloqueada = vaga(1L, 5L)
        when:
        facade.buscarInteressadosRecrutador(4L, 3L)
        then:
        thrown(SecurityException)
        0 * consulta._
        0 * retribuicao.salvar(*_)
        1 * conexao.rollback()
    }

    def 'ator ausente nao abre conexao nem interpreta recrutador como empresa'() {
        when:
        facade.retribuirInteresseRecrutador(null, 2L, 3L)
        then:
        thrown(IllegalArgumentException)
        0 * factory._
        0 * retribuicao._
        0 * matches._
    }

    def 'estado de retribuicao valida contexto e retorna apenas IDs: #recrutador'() {
        when:
        def resultado = recrutador ? facade.buscarIdsCandidatosRetribuidosRecrutador(4L, 3L) :
                facade.buscarIdsCandidatosRetribuidosEmpresa(1L, 3L)
        then:
        1 * consulta.buscarIdsCandidatosRetribuidos(conexao, 3L) >> [2L]
        resultado == [2L]
        0 * retribuicao.salvar(*_)
        1 * conexao.commit()
        where:
        recrutador << [false, true]
    }

    def 'estado de retribuicao rejeita empresa alheia ou colega antes da consulta: #recrutador'() {
        given:
        vagaBloqueada = vaga(1L, 5L)
        when:
        if (recrutador) facade.buscarIdsCandidatosRetribuidosRecrutador(4L, 3L)
        else facade.buscarIdsCandidatosRetribuidosEmpresa(9L, 3L)
        then:
        thrown(SecurityException)
        0 * consulta._
        1 * conexao.rollback()
        where:
        recrutador << [false, true]
    }

    private static Vaga vaga(Long empresa, Long autor) {
        Vaga vaga = new Vaga(3L, 'Interna', 'Descrição', 'SP', empresa)
        vaga.idRecrutador = autor
        return vaga
    }
}
