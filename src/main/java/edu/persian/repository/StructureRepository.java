package edu.persian.repository;

import edu.persian.model.ComponentType;
import java.util.ArrayList;
import java.util.List;

/** UC-02 data access: which sections belong to a chapter, and in what order. */
public final class StructureRepository {
    private final Database db;
    public StructureRepository(Database db) { this.db = db; }

    public List<ComponentType> selected(long chapter) {
        return db.read(c -> {
            List<ComponentType> result = new ArrayList<>();
            try (var s = c.prepareStatement("SELECT type FROM components WHERE chapter_id=? AND position IS NOT NULL ORDER BY position")) {
                s.setLong(1,chapter);
                try (var r = s.executeQuery()) { while(r.next()) result.add(ComponentType.valueOf(r.getString(1))); }
            }
            return result;
        });
    }

    public void configure(long chapter, List<ComponentType> types) {
        db.transaction(c -> {
            // NULL positions retain deselected content and avoid unique-position conflicts during reorder.
            try (var s = c.prepareStatement("UPDATE components SET position=NULL WHERE chapter_id=?")) { s.setLong(1,chapter); s.executeUpdate(); }
            try (var s = c.prepareStatement("INSERT INTO components(chapter_id,type,position,content) VALUES (?,?,?,'') ON DUPLICATE KEY UPDATE position=VALUES(position)")) {
                for (int i=0;i<types.size();i++) { s.setLong(1,chapter); s.setString(2,types.get(i).name()); s.setInt(3,i); s.addBatch(); }
                s.executeBatch();
            }
            return null;
        });
    }
}
