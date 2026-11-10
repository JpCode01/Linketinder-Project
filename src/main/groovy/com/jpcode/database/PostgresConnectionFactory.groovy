package com.jpcode.database

import java.sql.Connection
import java.sql.DriverManager
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import groovy.transform.PackageScope

class PostgresConnectionFactory implements ConnectionFactory {
    private static final List<String> CAMPOS = ['DB_URL', 'DB_USER', 'DB_PASSWORD']
    private final Map<String, String> configuracao

    PostgresConnectionFactory() {
        this(System.getenv(), Path.of('.env'))
    }

    @PackageScope
    PostgresConnectionFactory(Map<String, String> ambiente, Path arquivo) {
        Map<String, String> valores = [:]
        if (!CAMPOS.every { ambiente.containsKey(it) } && Files.exists(arquivo)) {
            List<String> linhas
            try {
                linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8)
            } catch (IOException ignored) {
                throw new IllegalStateException('Não foi possível ler o arquivo .env.')
            }
            linhas.each { linha ->
                String texto = linha.trim()
                if (!texto || texto.startsWith('#')) return
                int separador = texto.indexOf('=')
                String campo = separador < 0 ? texto : texto.substring(0, separador).trim()
                if (!(campo in CAMPOS)) return
                if (separador < 0) throw new IllegalStateException('Configuração de banco inválida no .env.')
                String valor = texto.substring(separador + 1).trim()
                if (valor.startsWith('"') || valor.startsWith("'")) {
                    if (valor.length() < 2 || !valor.endsWith(valor.substring(0, 1))) {
                        throw new IllegalStateException('Configuração de banco inválida no .env.')
                    }
                    valor = valor.substring(1, valor.length() - 1)
                }
                valores[campo] = valor
            }
        }
        CAMPOS.each { campo ->
            if (ambiente.containsKey(campo)) valores[campo] = ambiente[campo]
        }
        if (CAMPOS.any { !valores[it]?.trim() }) {
            throw new IllegalStateException('Configure DB_URL, DB_USER e DB_PASSWORD no ambiente ou no .env da raiz do projeto.')
        }
        configuracao = valores.asImmutable()
    }

    @Override
    Connection getConnection() {
        DriverManager.getConnection(
                configuracao.DB_URL,
                configuracao.DB_USER,
                configuracao.DB_PASSWORD
        )
    }
}
