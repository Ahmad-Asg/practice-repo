package edu.persian.service;

import edu.persian.model.Chapter;
import edu.persian.repository.ChapterRepository;
import java.util.List;

public final class ChapterService {
    private final ChapterRepository repository;
    public ChapterService(ChapterRepository repository) { this.repository=repository; }
    public List<Chapter> list() { return repository.list(); }
    public Chapter get(long id) { return repository.find(id).orElseThrow(() -> new ValidationException("Chapter not found. · باب نہیں ملا۔")); }
    public Chapter create(String title) { return repository.create(Rules.required(title,"Chapter title / باب کا عنوان")); }
    public void rename(long id, String title) { Rules.found(repository.rename(id,Rules.required(title,"Chapter title / باب کا عنوان"))); }
    public boolean delete(long id, boolean confirmed) {
        get(id);
        if (!confirmed) return false;
        Rules.found(repository.delete(id)); return true;
    }
}
