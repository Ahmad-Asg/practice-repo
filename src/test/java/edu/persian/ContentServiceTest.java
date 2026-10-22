package edu.persian;

import edu.persian.model.ComponentType;
import edu.persian.service.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ContentServiceTest extends ServiceTestBase {
    @Test void unicodeEditAndReopenPreserveExactText() {
        long id=chapter(); structure.save(id,List.of(ComponentType.DIALOGUE,ComponentType.TRANSLATION));
        String text="سلام! می‌روم — یہ اُردو ہے۔\nLesson 2: ۱۲۳ (45)؟ 🙂  ";
        content.save(id,ComponentType.DIALOGUE,text); content.save(id,ComponentType.TRANSLATION,"پہلا متن"); content.save(id,ComponentType.TRANSLATION,"نیا متن");
        reopen(); assertEquals(text,content.get(id,ComponentType.DIALOGUE)); assertEquals("نیا متن",content.get(id,ComponentType.TRANSLATION)); assertTrue(content.complete(id));
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={" ","\n\t","\u00a0"})
    void emptyRequiredContentDoesNotOverwrite(String text) {
        long id=chapter(); structure.save(id,List.of(ComponentType.READING)); content.save(id,ComponentType.READING,"متن");
        assertThrows(ValidationException.class,() -> content.save(id,ComponentType.READING,text)); assertEquals("متن",content.get(id,ComponentType.READING));
    }
    @Test void contentRemovalAndCompleteness() {
        long id=chapter(); assertFalse(content.complete(id)); structure.save(id,List.of(ComponentType.READING,ComponentType.GLOSSARY)); assertFalse(content.complete(id));
        content.save(id,ComponentType.READING,"متن"); assertFalse(content.complete(id)); glossary.add(id,"آب","پانی"); assertTrue(content.complete(id));
        content.remove(id,ComponentType.READING); reopen(); assertEquals("",content.get(id,ComponentType.READING)); assertFalse(content.complete(id));
    }
    @Test void associationsAndUnconfiguredComponentsAreProtected() {
        long a=chapter(),b=chapter(); structure.save(a,List.of(ComponentType.EXAMPLES));
        assertThrows(ValidationException.class,() -> content.save(b,ComponentType.EXAMPLES,"x"));
        assertThrows(ValidationException.class,() -> content.save(a,ComponentType.GLOSSARY,"x"));
        assertThrows(ValidationException.class,() -> content.get(a,null));
        assertThrows(ValidationException.class,() -> content.remove(99,ComponentType.EXAMPLES));
        content.save(a,ComponentType.EXAMPLES,"الف"); structure.save(b,List.of(ComponentType.EXAMPLES)); assertEquals("",content.get(b,ComponentType.EXAMPLES));
    }
}
