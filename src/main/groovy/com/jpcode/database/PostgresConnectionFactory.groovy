package com.jpcode.database

import java.sql.Connection
import java.sql.DriverManager

class PostgresConnectionFactory implements ConnectionFactory {

    @Override
    Connection getConnection() {
        DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/linketinder",
                "jp",
                "2506"
        )
    }
}
