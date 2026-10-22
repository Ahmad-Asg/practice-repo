package edu.persian;

import edu.persian.model.ComponentType;
import edu.persian.service.ValidationException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StructureServiceTest extends ServiceTestBase {
    @Test void selectionOrderingAndChapterIndependenceSurviveReopen() {
        long a=chapter(),b=chapter(); var all=new ArrayList<>(List.of(ComponentType.values()));
        structure.save(a,all); Collections.reverse(all); structure.save(a,all); structure.save(b,List.of(ComponentType.GRAMMAR));
        reopen(); assertEquals(all,structure.get(a)); assertEquals(List.of(ComponentType.GRAMMAR),structure.get(b));
    }
    @Test void deselectionRetainsContentAndGlossary() {
        long id=chapter(); structure.save(id,List.of(ComponentType.READING)); content.save(id,ComponentType.READING,"کتاب"); glossary.add(id,"کتاب","کتاب");
        structure.save(id,List.of()); reopen(); assertTrue(structure.get(id).isEmpty());
        structure.save(id,List.of(ComponentType.READING)); assertEquals("کتاب",content.get(id,ComponentType.READING)); assertEquals(1,glossary.list(id).size());
    }
    @Test void invalidConfigurationsPreservePreviousOrder() {
        long id=chapter(); var valid=List.of(ComponentType.READING); structure.save(id,valid);
        assertThrows(ValidationException.class,() -> structure.save(id,null));
        assertThrows(ValidationException.class,() -> structure.save(id,Arrays.asList(ComponentType.READING,null)));
        assertThrows(ValidationException.class,() -> structure.save(id,List.of(ComponentType.READING,ComponentType.READING)));
        assertEquals(valid,structure.get(id));
    }
    @Test void missingChapterIsRejectedEvenForEmptyStructure() {
        assertThrows(ValidationException.class,() -> structure.save(88,List.of())); assertThrows(ValidationException.class,() -> structure.get(88));
    }
    @Test void failedDatabaseConfigurationRollsBack() {
        long id=chapter(); structure.save(id,List.of(ComponentType.READING));
        assertThrows(RuntimeException.class,() -> new edu.persian.repository.StructureRepository(db).configure(id,Arrays.asList(ComponentType.GRAMMAR,null)));
        assertEquals(List.of(ComponentType.READING),structure.get(id));
    }
}
