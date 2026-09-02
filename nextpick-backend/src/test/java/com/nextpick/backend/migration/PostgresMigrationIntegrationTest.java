package com.nextpick.backend.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PostgresMigrationIntegrationTest {
    @Test
    void upgradesV2DataAndEnforcesCompositeFavoriteIdentity() {
        assumeTrue(dockerCommandAvailable(), "Docker no disponible");
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker daemon no disponible");
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
            postgres.start();
            Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .target("2").load().migrate();

            JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                    postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
            jdbc.update("INSERT INTO users(name,email,password) VALUES (?,?,?)", "Legacy", "legacy@test", "hash");
            jdbc.update("INSERT INTO favorites(user_id,movie_id) VALUES (1,1)");

            Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .load().migrate();

            assertThat(jdbc.queryForObject("SELECT count(*) FROM favorites WHERE legacy_movie_id=1 AND tmdb_id IS NULL",
                    Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT to_regclass('public.legacy_movies') IS NOT NULL", Boolean.class))
                    .isTrue();

            jdbc.update("INSERT INTO users(name,email,password) VALUES (?,?,?)", "Other", "other@test", "hash");
            jdbc.update("INSERT INTO favorites(user_id,tmdb_id,media_type,snapshot_title) VALUES (1,603,'MOVIE','Matrix')");
            jdbc.update("INSERT INTO favorites(user_id,tmdb_id,media_type,snapshot_title) VALUES (2,603,'MOVIE','Matrix')");
            assertThatThrownBy(() -> jdbc.update(
                    "INSERT INTO favorites(user_id,tmdb_id,media_type,snapshot_title) VALUES (1,603,'MOVIE','Duplicate')"))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }

    private boolean dockerCommandAvailable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "docker.exe" : "docker";
        return Arrays.stream(System.getenv().getOrDefault("PATH", "").split(File.pathSeparator))
                .map(Path::of)
                .map(path -> path.resolve(executable))
                .anyMatch(Files::isRegularFile);
    }
}
