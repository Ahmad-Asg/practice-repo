package edu.persian.service;

import edu.persian.model.ComponentType;
import edu.persian.repository.ContentRepository;
import java.util.List;

public final class ContentService {
    private final ContentRepository repository;
    private final StructureService structure;
    private final GlossaryService glossary;
    public ContentService(ContentRepository repository, StructureService structure, GlossaryService glossary) {
        this.repository=repository; this.structure=structure; this.glossary=glossary;
    }
    private void editable(long chapter, ComponentType type) {
        if (type==null || type==ComponentType.GLOSSARY || !structure.get(chapter).contains(type))
            throw new ValidationException("Select a configured text component; edit glossary entries in Glossary. · متن کا منتخب جزو کھولیں، فرہنگ الگ صفحے پر ہے۔");
    }
    public String get(long chapter, ComponentType type) { editable(chapter,type); return repository.content(chapter,type).orElseThrow(); }
    public void save(long chapter, ComponentType type, String text) {
        editable(chapter,type);
        Rules.found(repository.saveContent(chapter,type,Rules.required(text,"Content / متن")));
    }
    public void remove(long chapter, ComponentType type) { editable(chapter,type); Rules.found(repository.saveContent(chapter,type,"")); }
    public boolean complete(long chapter) {
        List<ComponentType> selected = structure.get(chapter);
        if (selected.isEmpty()) return false;
        return selected.stream().allMatch(t -> t==ComponentType.GLOSSARY ? !glossary.list(chapter).isEmpty() : !Rules.blank(repository.content(chapter,t).orElse("")));
    }
}
