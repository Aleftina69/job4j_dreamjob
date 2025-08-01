package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryCandidateRepository implements CandidateRepository {

    private static final MemoryCandidateRepository INSTANCE = new MemoryCandidateRepository();

    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();
    private int nextId = 1;

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Иван Иванов", "Описание 1", LocalDateTime.now()));
        save(new Candidate(0, "Петр Петров", "Описание 2", LocalDateTime.now()));
        save(new Candidate(0, "Дарья Дашина", "Описание 3", LocalDateTime.now()));
        save(new Candidate(0, "Егор Егоров", "Описание 4", LocalDateTime.now()));
        save(new Candidate(0, "Екатерина Попова", "Описание 5", LocalDateTime.now()));
        save(new Candidate(0, "Евгений Морозов", "Описание 6", LocalDateTime.now()));
    }

    public static MemoryCandidateRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public Candidate save(Candidate candidate) {
        if (candidate.getId() == 0) { // если id не задан, то присваиваем новый
            candidate.setId(nextId++);
        }
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id) != null;
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(oldCandidate.getId(), candidate.getName(),
                        candidate.getDescription(), candidate.getCreationDate())) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}
