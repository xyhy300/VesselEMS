package vesselems.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import vesselems.model.Datasource;

@Component
public class DSManager {

    private final Map<Long, DataSource> cache = new ConcurrentHashMap<>();

    public DataSource get(Datasource ds) {
        return cache.computeIfAbsent(ds.getId(), id -> create(ds));
    }

    public void evict(Long id) {
        DataSource old = cache.remove(id);
        if (old instanceof HikariDataSource hds) {
            hds.close();
        }
    }

    public boolean test(Datasource ds) {
        try (Connection conn = DriverManager.getConnection(ds.getUrl(), ds.getUsername(), ds.getPassword())) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    private DataSource create(Datasource ds) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ds.getUrl());
        config.setUsername(ds.getUsername());
        config.setPassword(ds.getPassword());
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5000);
        return new HikariDataSource(config);
    }
}