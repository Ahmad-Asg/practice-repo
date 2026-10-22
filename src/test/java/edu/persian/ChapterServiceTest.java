package edu.persian;

import edu.persian.model.ComponentType;
import edu.persian.service.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ChapterServiceTest extends ServiceTestBase {
    @Test void createListViewRenameAndReopen() {
        long a=chapter(), b=chapter(); assertNotEquals(a,b); assertEquals(2,chapters.list().size());
        chapters.rename(a,"دوستی ۲"); reopen(); assertEquals("دوستی ۲",chapters.get(a).title());
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={" ","\t\n","\u00a0","\u2003"})
    void rejectsBlankWithoutOverwriting(String title) {
        long id=chapter(); String saved=chapters.get(id).title();
        assertThrows(ValidationException.class,() -> chapters.create(title));
        assertThrows(ValidationException.class,() -> chapters.rename(id,title));
        assertEquals(saved,chapters.get(id).title()); assertEquals(1,chapters.list().size());
    }
    @Test void confirmedDeletionCascadesAndCancelledDeletionPreserves() {
        long id=chapter(); structure.save(id,List.of(ComponentType.DIALOGUE)); content.save(id,ComponentType.DIALOGUE,"سلام"); glossary.add(id,"آب","پانی");
        assertFalse(chapters.delete(id,false)); assertEquals("سلام",content.get(id,ComponentType.DIALOGUE)); assertEquals(1,glossary.list(id).size());
        assertTrue(chapters.delete(id,true)); reopen(); assertTrue(chapters.list().isEmpty());
        db.read(c -> { for(String table:List.of("components","glossary")) try(var s=c.createStatement();var r=s.executeQuery("SELECT count(*) FROM "+table)) { r.next(); assertEquals(0,r.getInt(1)); } return null; });
    }
    @Test void missingRecordsAreClearErrors() {
        assertThrows(ValidationException.class,() -> chapters.get(42)); assertThrows(ValidationException.class,() -> chapters.rename(42,"x")); assertThrows(ValidationException.class,() -> chapters.delete(42,true));
    }
    @Test void titlesAcceptSingleCharacterAndSqlPunctuation() {
        long id=chapters.create("' ; DROP TABLE chapters; — ا").id(); assertTrue(chapters.get(id).title().contains("DROP")); assertNotNull(chapters.create("ا"));
    }
}
