package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileControllerTest {

    private FileController fileController;
    private FileService fileService;

    @BeforeEach
    public void initServices() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
    }

    @Test
    public void whenGetByIdWithNonExistingFileThenReturnNotFound() {
        int fileId = 2;
        when(fileService.getFileById(fileId)).thenReturn(Optional.empty());
        ResponseEntity<?> response = fileController.getById(fileId);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }
}
