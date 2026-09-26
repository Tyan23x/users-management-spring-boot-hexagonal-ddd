package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import java.util.Objects;

public record DatabaseConfig(
    String host,
    int port,
    String databaseName,
    String username,
    String password,
    boolean ssl,
    String directUrl) {

  private static final String URL_TEMPLATE =
      "jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  private static final String MYSQL_PREFIX = "mysql://";
  private static final String JDBC_PREFIX = "jdbc:";
  private static final String STRING_TRUE = "true";
  private static final String STRING_FALSE = "false";

  public DatabaseConfig(
      final String host,
      final int port,
      final String databaseName,
      final String username,
      final String password) {
    this(host, port, databaseName, username, password, false, null);
  }

  public DatabaseConfig(
      final String host,
      final int port,
      final String databaseName,
      final String username,
      final String password,
      final boolean ssl) {
    this(host, port, databaseName, username, password, ssl, null);
  }

  public String buildJdbcUrl() {
    if (Objects.nonNull(directUrl) && !directUrl.isBlank()) {
      if (directUrl.startsWith(MYSQL_PREFIX)) {
        return JDBC_PREFIX + directUrl;
      }
      return directUrl;
    }
    return String.format(URL_TEMPLATE, host, port, databaseName, ssl ? STRING_TRUE : STRING_FALSE);
  }
}
