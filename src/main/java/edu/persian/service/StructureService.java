package edu.persian.service;

import edu.persian.model.ComponentType;
import edu.persian.repository.StructureRepository;
import java.util.*;

public final class StructureService {
    private final ChapterService chapters;
    private final StructureRepository repository;
    public StructureService(ChapterService chapters, StructureRepository repository) { this.chapters=chapters; this.repository=repository; }
    public List<ComponentType> get(long chapter) { chapters.get(chapter); return repository.selected(chapter); }
    public void save(long chapter, List<ComponentType> types) {
        chapters.get(chapter);
        if (types==null || types.stream().anyMatch(Objects::isNull) || new HashSet<>(types).size()!=types.size())
            throw new ValidationException("Choose each component at most once. · ہر جزو صرف ایک بار منتخب کریں۔");
        repository.configure(chapter,List.copyOf(types));
    }
}
