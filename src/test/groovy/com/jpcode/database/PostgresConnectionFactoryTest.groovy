package com.jpcode.database

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager

class PostgresConnectionFactoryTest extends Specification {
    @TempDir Path diretorio
    Path arquivo
    Driver driver
    Connection conexao = Mock()
    String urlRecebida
    Properties propriedadesRecebidas

    def setup() {
        arquivo = diretorio.resolve('.env')
        driver = Stub(Driver) {
            connect(_, _) >> { String url, Properties propriedades ->
                if (!url.startsWith('jdbc:configuracao-teste:')) return null
                urlRecebida = url
                propriedadesRecebidas = propriedades
                conexao
            }
        }
        DriverManager.registerDriver(driver)
    }

    def cleanup() {
        DriverManager.deregisterDriver(driver)
    }

    def 'usa URL usuario e senha do arquivo sem interpretar caracteres da senha'() {
        given:
        Files.writeString(arquivo, '''# Configuração local
DB_URL = "jdbc:configuracao-teste:linketinder-recrutador"
DB_USER = 'usuario_teste'
DB_PASSWORD = " senha=#com=caracteres "
LINKETINDER_TEST_JDBC_URL=ignorada
''')

        expect:
        new PostgresConnectionFactory([:], arquivo).getConnection().is(conexao)
        urlRecebida == 'jdbc:configuracao-teste:linketinder-recrutador'
        propriedadesRecebidas.getProperty('user') == 'usuario_teste'
        propriedadesRecebidas.getProperty('password') == ' senha=#com=caracteres '
    }

    def 'variaveis de ambiente prevalecem sobre arquivo e completam configuracao parcial'() {
        given:
        Files.writeString(arquivo, 'DB_URL=jdbc:configuracao-teste:antigo\nDB_USER=local\nDB_PASSWORD=local')

        expect:
        new PostgresConnectionFactory([DB_URL: 'jdbc:configuracao-teste:correto', DB_PASSWORD: 'ambiente'], arquivo)
                .getConnection().is(conexao)
        urlRecebida == 'jdbc:configuracao-teste:correto'
        propriedadesRecebidas.getProperty('user') == 'local'
        propriedadesRecebidas.getProperty('password') == 'ambiente'
    }

    def 'ambiente completo funciona sem arquivo ou com arquivo invalido'() {
        given:
        if (arquivoExiste) Files.writeString(arquivo, 'DB_PASSWORD="sem fechamento')

        expect:
        new PostgresConnectionFactory([DB_URL: 'jdbc:configuracao-teste:ambiente', DB_USER: 'usuario', DB_PASSWORD: 'senha'], arquivo)
                .getConnection().is(conexao)
        urlRecebida == 'jdbc:configuracao-teste:ambiente'

        where:
        arquivoExiste << [false, true]
    }

    def 'configuracao incompleta ou vazia falha antes de tentar conectar sem revelar valores'() {
        given:
        Files.writeString(arquivo, conteudo)

        when:
        new PostgresConnectionFactory(ambiente, arquivo)

        then:
        def erro = thrown(IllegalStateException)
        erro.message == 'Configure DB_URL, DB_USER e DB_PASSWORD no ambiente ou no .env da raiz do projeto.'
        urlRecebida == null

        where:
        conteudo                                                                         | ambiente
        'DB_URL=jdbc:configuracao-teste:segredo\nDB_USER=usuario'                           | [:]
        'DB_URL=jdbc:configuracao-teste:segredo\nDB_USER=usuario\nDB_PASSWORD=segredo'       | [DB_PASSWORD: '']
        'LINKETINDER_TEST_JDBC_URL=jdbc:configuracao-teste:segredo'                          | [:]
    }

    def 'nao usa banco padrao quando nao existe configuracao'() {
        when:
        new PostgresConnectionFactory([:], arquivo)

        then:
        thrown(IllegalStateException)
        urlRecebida == null
    }

    def 'arquivo com aspas incompletas e rejeitado sem revelar segredo'() {
        given:
        Files.writeString(arquivo, 'DB_PASSWORD="segredo')

        when:
        new PostgresConnectionFactory([:], arquivo)

        then:
        def erro = thrown(IllegalStateException)
        erro.message == 'Configuração de banco inválida no .env.'
        urlRecebida == null
    }
}
