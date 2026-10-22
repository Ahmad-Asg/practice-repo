package edu.persian.repository;

import edu.persian.model.ComponentType;
import java.util.Optional;

/** UC-03 data access: exact text belonging to a selected chapter section. */
public final class ContentRepository {
    private final Database db;
    public ContentRepository(Database db) { this.db = db; }

    public Optional<String> content(long chapter, ComponentType type) {
        return db.read(c -> {
            try (var s = c.prepareStatement("SELECT content FROM components WHERE chapter_id=? AND type=? AND position IS NOT NULL")) {
                s.setLong(1,chapter); s.setString(2,type.name());
                try (var r = s.executeQuery()) { return r.next() ? Optional.of(r.getString(1)) : Optional.empty(); }
            }
        });
    }

    public boolean saveContent(long chapter, ComponentType type, String text) {
        return db.read(c -> {
            try (var s = c.prepareStatement("UPDATE components SET content=? WHERE chapter_id=? AND type=? AND position IS NOT NULL")) {
                s.setString(1,text); s.setLong(2,chapter); s.setString(3,type.name()); return s.executeUpdate()==1;
            }
        });
    }
}
