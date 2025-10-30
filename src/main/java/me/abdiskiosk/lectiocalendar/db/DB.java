package me.abdiskiosk.lectiocalendar.db;

import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DB {

    private final SQLiteDataSource dataSource;

    public DB() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:./cache.db");
    }

    public void init() {
        try (Connection connection = dataSource.getConnection()) {
            InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql");
            if (in == null) throw new RuntimeException("schema.sql not found in resources");
            String sql = new BufferedReader(new InputStreamReader(in))
                    .lines().collect(Collectors.joining("\n"));
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    public <R> List<R> query(@NotNull String query, @NotNull SQLThrowingFunction<R, ResultSet> mapper,
                                          @NotNull Object... params) throws SQLException {
        return withConnection(connection -> query(connection, query, mapper, params));
    }

    public long update(@NotNull String update, @NotNull Object... params) throws SQLException {
        return withConnection(connection -> update(connection, update, params));
    }

    public long insertIncremental(@NotNull String insert, @NotNull Object... params) throws SQLException {
        return withConnection(connection -> insertIncremental(connection, insert, params));
    }

    public <R> R withConnection(@NotNull SQLThrowingFunction<R, Connection> func) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            return func.run(connection);
        }
    }

    public <R> List<R> query(@NotNull Connection connection, @NotNull String query,
                                          @NotNull SQLThrowingFunction<R, ResultSet> mapper,
                                          @NotNull Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for(int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }

        ResultSet result = statement.executeQuery();
        List<R> mapped = new ArrayList<>();
        while(result.next()) {
            mapped.add(mapper.run(result));
        }

        return mapped;
    }

    public long update(@NotNull Connection connection, @NotNull String update,
                                   @NotNull Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(update);
        for(int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }

        return statement.executeLargeUpdate();
    }

    public long insertIncremental(@NotNull Connection connection, @NotNull String insert,
                                               @NotNull Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(insert);
        for(int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }


        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            generatedKeys.next();
            return generatedKeys.getLong(1);
        }
    }


    public void close() {
    }

    @FunctionalInterface
    public interface SQLThrowingFunction<R, P> {

        R run(@NotNull P param) throws SQLException;

    }

}
