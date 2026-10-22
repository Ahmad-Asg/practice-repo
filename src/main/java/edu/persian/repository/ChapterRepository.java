package edu.persian.repository;

import edu.persian.model.Chapter;
import java.util.*;
import java.sql.*;

public final class ChapterRepository {
    private final Database db;
    public ChapterRepository(Database db) { this.db = db; }
    public List<Chapter> list() {
        return db.read(c -> {
            List<Chapter> result = new ArrayList<>();
            try (var s = c.prepareStatement("SELECT id,title FROM chapters ORDER BY id"); var r = s.executeQuery()) {
                while (r.next()) result.add(new Chapter(r.getLong(1), r.getString(2)));
            }
            return result;
        });
    }
    public Optional<Chapter> find(long id) {
        return db.read(c -> {
            try (var s = c.prepareStatement("SELECT id,title FROM chapters WHERE id=?")) {
                s.setLong(1,id);
                try (var r = s.executeQuery()) { return r.next() ? Optional.of(new Chapter(r.getLong(1),r.getString(2))) : Optional.empty(); }
            }
        });
    }
    public Chapter create(String title) {
        return db.transaction(c -> {
            try (var s = c.prepareStatement("INSERT INTO chapters(title) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                s.setString(1,title); s.executeUpdate();
                try (var r = s.getGeneratedKeys()) { if (!r.next()) throw new SQLException("Missing generated chapter ID"); return new Chapter(r.getLong(1),title); }
            }
        });
    }
    public boolean rename(long id, String title) {
        return db.read(c -> {
            try (var s = c.prepareStatement("UPDATE chapters SET title=? WHERE id=?")) { s.setString(1,title); s.setLong(2,id); return s.executeUpdate() == 1; }
        });
    }
    public boolean delete(long id) {
        return db.read(c -> {
            try (var s = c.prepareStatement("DELETE FROM chapters WHERE id=?")) { s.setLong(1,id); return s.executeUpdate() == 1; }
        });
    }
}
