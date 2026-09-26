package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseConfig")
class DatabaseConfigTest {

  private static final String HOST = "db.example.com";
  private static final int PORT = 3306;
  private static final String DB_NAME = "users_db";
  private static final String USERNAME = "db_user";
  private static final String PASSWORD = "db_password";

  @Test
  @DisplayName("buildJdbcUrl() genera URL con useSSL=false por defecto")
  void shouldBuildJdbcUrlWithSslFalseByDefault() {
    // Arrange
    final DatabaseConfig config = new DatabaseConfig(HOST, PORT, DB_NAME, USERNAME, PASSWORD);

    // Act
    final String url = config.buildJdbcUrl();

    // Assert
    assertEquals(
        "jdbc:mysql://db.example.com:3306/users_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        url);
  }

  @Test
  @DisplayName("buildJdbcUrl() genera URL con useSSL=true cuando ssl es true")
  void shouldBuildJdbcUrlWithSslTrueWhenConfigured() {
    // Arrange
    final DatabaseConfig config = new DatabaseConfig(HOST, PORT, DB_NAME, USERNAME, PASSWORD, true);

    // Act
    final String url = config.buildJdbcUrl();

    // Assert
    assertEquals(
        "jdbc:mysql://db.example.com:3306/users_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        url);
  }

  @Test
  @DisplayName("buildJdbcUrl() retorna directUrl cuando está presente")
  void shouldReturnDirectUrlWhenProvided() {
    // Arrange
    final String directUrl = "jdbc:mysql://custom-host:4000/cloud_db?sslMode=REQUIRED";
    final DatabaseConfig config =
        new DatabaseConfig(HOST, PORT, DB_NAME, USERNAME, PASSWORD, true, directUrl);

    // Act
    final String url = config.buildJdbcUrl();

    // Assert
    assertEquals(directUrl, url);
  }

  @Test
  @DisplayName("buildJdbcUrl() antepone jdbc: si directUrl comienza con mysql://")
  void shouldPrependJdbcPrefixWhenDirectUrlStartsWithMysql() {
    // Arrange
    final String rawUrl = "mysql://custom-host:4000/cloud_db?sslMode=REQUIRED";
    final DatabaseConfig config =
        new DatabaseConfig(HOST, PORT, DB_NAME, USERNAME, PASSWORD, true, rawUrl);

    // Act
    final String url = config.buildJdbcUrl();

    // Assert
    assertEquals("jdbc:" + rawUrl, url);
  }

  @Test
  @DisplayName("DatabaseConfig conserva todos sus campos")
  void shouldRetainConfigFields() {
    // Arrange
    final DatabaseConfig config = new DatabaseConfig(HOST, PORT, DB_NAME, USERNAME, PASSWORD, true);

    // Act & Assert
    assertAll(
        "Campos de DatabaseConfig",
        () -> assertEquals(HOST, config.host()),
        () -> assertEquals(PORT, config.port()),
        () -> assertEquals(DB_NAME, config.databaseName()),
        () -> assertEquals(USERNAME, config.username()),
        () -> assertEquals(PASSWORD, config.password()),
        () -> assertEquals(true, config.ssl()));
  }
}
