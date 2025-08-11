package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CandidateControllerTest {

    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "John Doe", "Desc", now(), 1, 1);
        var candidate2 = new Candidate(2, "Jane Doe", "Description", now(), 2, 2);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var session = mock(HttpSession.class);
        var view = candidateController.getAll(model, session);

        assertThat(view).isEqualTo("candidates/list");
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPage() {
        var model = new ConcurrentModel();
        var session = mock(HttpSession.class);
        var view = candidateController.getCreationPage(model, session);

        assertThat(view).isEqualTo("candidates/create");
    }

    @Test
    public void whenPostCandidateWithFileThenRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "John Doe", "Descr", now(), 1, 1);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenCreateCandidateThrowsExceptionThenReturnErrorPage() {
        var expectedException = new RuntimeException("Failed to save candidate");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenGetByIdAndCandidateExistsThenReturnCandidatePage() {
        var candidate = new Candidate(1, "John Doe", "Desc", now(), 1, 1);
        when(candidateService.findById(1)).thenReturn(java.util.Optional.of(candidate));
        when(cityService.findAll()).thenReturn(List.of(new City(1, "Москва"), new City(2, "Санкт-Петербург")));

        var model = new ConcurrentModel();
        var session = mock(HttpSession.class);
        var view = candidateController.getById(model, 1, session);

        assertThat(view).isEqualTo("candidates/one");
        assertThat(model.getAttribute("candidate")).isEqualTo(candidate);
        assertThat(model.getAttribute("cities")).isNotNull(); // cities добавлены в модель
    }

    @Test
    public void whenGetByIdAndCandidateNotFoundThenReturnErrorPage() {
        when(candidateService.findById(99)).thenReturn(java.util.Optional.empty());

        var model = new ConcurrentModel();
        var session = mock(HttpSession.class);
        var view = candidateController.getById(model, 99, session);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    public void whenUpdateCandidateSuccessfulThenRedirectToCandidates() throws Exception {
        var candidate = new Candidate(1, "John Doe", "Desc", now(), 1, 1);
        when(candidateService.update(any(), any())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenUpdateCandidateNotFoundThenReturnErrorPage() throws Exception {
        var candidate = new Candidate(99, "Not Found", "Desc", now(), 1, 1);
        when(candidateService.update(any(), any())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    public void whenUpdateCandidateThrowsExceptionThenReturnErrorPage() throws Exception {
        var candidate = new Candidate(1, "John Doe", "Desc", now(), 1, 1);
        var exception = new RuntimeException("Error during update");
        when(candidateService.update(any(), any())).thenThrow(exception);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo(exception.getMessage());
    }

    @Test
    public void whenDeleteCandidateSuccessfulThenRedirectToCandidates() {
        when(candidateService.deleteById(1)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, 1);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenDeleteCandidateNotFoundThenReturnErrorPage() {
        when(candidateService.deleteById(99)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, 99);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
    }
}
