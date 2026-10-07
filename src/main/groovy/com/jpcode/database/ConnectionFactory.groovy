package com.jpcode.database

import java.sql.Connection

interface ConnectionFactory {
    Connection getConnection()
}