package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
@Repository
public class MemoryCandidateRepository implements CandidateRepository {

    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Иван Иванов", "Описание 1", LocalDateTime.now(), 1));
        save(new Candidate(0, "Петр Петров", "Описание 2", LocalDateTime.now(), 2));
        save(new Candidate(0, "Дарья Дашина", "Описание 3", LocalDateTime.now(), 3));
        save(new Candidate(0, "Егор Егоров", "Описание 4", LocalDateTime.now(), 3));
        save(new Candidate(0, "Екатерина Попова", "Описание 5", LocalDateTime.now(), 2));
        save(new Candidate(0, "Евгений Морозов", "Описание 6", LocalDateTime.now(), 1));
    }

    @Override
    public Candidate save(Candidate candidate) {
        if (candidate.getId() == 0) {
            candidate.setId(nextId.incrementAndGet());
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
                        candidate.getDescription(), candidate.getCreationDate(), candidate.getCityId())) != null;
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
