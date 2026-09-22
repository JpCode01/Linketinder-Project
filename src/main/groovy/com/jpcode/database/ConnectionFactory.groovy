package com.jpcode.database

import java.sql.Connection
import java.sql.DriverManager

class ConnectionFactory {

    private static final String URL = "jdbc:postgresql://localhost:5432/linketinder"
    private static final String USER = "jp"
    private static final String PASSWORD = "2506"

    static Connection getConnection() {
        DriverManager.getConnection(URL, USER, PASSWORD)
    }
}
