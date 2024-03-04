package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.checkableTypes.*;
import com.bloomtech.library.repositories.CheckableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@SpringBootTest
public class CheckableServiceTest {

    //TODO: Inject dependencies and mocks
    @Autowired
    private CheckableService checkableService;

    @MockBean
    private CheckableRepository checkableRepository;

    private List<Checkable> checkables;

    @BeforeEach
    void init() {
        //Initialize test data
        checkables = new ArrayList<>();

        checkables.addAll(
                Arrays.asList(
                        new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                        new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                        new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC),
                        new Media("1-3", "Nature Around the World", "DocuSpecialists", MediaType.VIDEO),
                        new ScienceKit("2-0", "Anatomy Model"),
                        new ScienceKit("2-1", "Robotics Kit"),
                        new Ticket("3-0", "Science Museum Tickets"),
                        new Ticket("3-1", "National Park Day Pass")
                )
        );
    }

    //TODO: Write Unit Tests for all CheckableService methods and possible Exceptions

    @Test
    void getCheckables() {
        Mockito.when(checkableRepository.findAll()).thenReturn(checkables);
        List<Checkable> checkables = checkableService.getAll();
        assertEquals(8, checkables.size());
    }

    @Test
    void getByIsbn_findsExistingIsbn_returnsCheckable() {
        Mockito.when(checkableRepository.findByIsbn(any(String.class))).thenReturn(Optional.of(checkables.get(0)));
        Checkable checkable = checkableService.getByIsbn("1-0");
        assertEquals("1-0", checkable.getIsbn());
    }

    @Test
    void getByIsbn_nonExistingIsbn_throwsCheckableNotFoundException() {
        Mockito.when(checkableRepository.findByIsbn(any(String.class))).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, ()->{
            checkableService.getByIsbn("XXX");
        });
    }

    @Test
    void getByType_findsExistingType_returnsCheckable() {
        Mockito.when(checkableRepository.findByType(any())).thenReturn(Optional.of(checkables.get(0)));
        Checkable checkable = checkableService.getByType(Media.class);
        assertEquals(Media.class, checkable.getClass());
    }

    @Test
    void getByType_nonExistingIsbn_throwsCheckableNotFoundException() {
        Mockito.when(checkableRepository.findByIsbn(any())).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, ()->{
            checkableService.getByType(List.class);
        });
    }

    @Test
    void save_newCheckable_verifiesSave() {
        when(checkableRepository.findAll()).thenReturn(checkables);
        Checkable checkable = new Media("1-4", "New Book", "Some Person", MediaType.BOOK);
        checkableService.save(checkable);
        Mockito.verify(checkableRepository).save(checkable);
    }

    @Test
    void save_existingName_throwsResourceExistsException() {
        when(checkableRepository.findAll()).thenReturn(checkables);
        Checkable checkable = new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK);
        assertThrows(ResourceExistsException.class, ()->{
            checkableService.save(checkable);
        });
        verify(checkableRepository, never()).save(checkable);
    }

}