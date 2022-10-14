package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.models.checkableTypes.ScienceKit;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    //TODO: Implement behavior described by the unit tests in tst.com.bloomtech.library.services.LibraryService

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CheckableService checkableService;

    public List<Library> getLibraries() {
        return libraryRepository.findAll();
    }

    public Library getLibraryByName(String name) throws LibraryNotFoundException{
        Optional<Library> library = libraryRepository.findByName(name);
        if (library.isEmpty()) {
            throw new LibraryNotFoundException(String.format("%s not found!", name));
        }
        return library.get();
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {
        // Get checkAble
        Checkable checkableFromSerive = getCheckableByIsbn(checkableIsbn);
        Library library = getLibraryByName(libraryName);

        // In the library, find the matching checkable
        List<CheckableAmount> checkables = library.getCheckables();
        for (CheckableAmount checkableAmount: checkables) {
            Checkable checkable = checkableAmount.getCheckable();
            if (checkableIsbn.equals(checkable.getIsbn())) {
                return new CheckableAmount(checkable, checkableAmount.getAmount());
            }
        }

        // if checkable is not found
        return new CheckableAmount(checkableFromSerive, 0);
    }

    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        List<LibraryAvailableCheckouts> available = new ArrayList<>();

        // Get all libraries
        List<Library> libraries = getLibraries();

        Checkable checkableFromSerive = getCheckableByIsbn(isbn);
        // For each library - check if the isbn exists
        for (Library library : libraries) {
            List<CheckableAmount> checkables = library.getCheckables();
            for (CheckableAmount checkableAmount: checkables) {
                Checkable checkable = checkableAmount.getCheckable();
                if (isbn.equals(checkable.getIsbn())) {
                    available.add(new LibraryAvailableCheckouts(checkableAmount.getAmount(), library.getName()));
                }
            }
        }
        return available;
    }

    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        List<OverdueCheckout> overdueCheckouts = new ArrayList<>();
        Library library = getLibraryByName(libraryName);

        // In the library, for each library card, check all checkouts
        // For each checkout, check the due date
        // If the due date is before the current time, then it is overdue
        // To create overdue - we get the current Patron from the current Card and the current Checkout
        Set<LibraryCard> libraryCards = library.getLibraryCards();
        for (LibraryCard libraryCard : libraryCards) {
            for (Checkout checkout : libraryCard.getCheckouts()) {
                LocalDateTime currentTime = LocalDateTime.now();
                if (checkout.getDueDate().isBefore(currentTime)) {
                    overdueCheckouts.add(new OverdueCheckout(libraryCard.getPatron(), checkout));
                }
            }
        }

        return overdueCheckouts;
    }

    private Checkable getCheckableByIsbn (String isbn) {
        return checkableService.getByIsbn(isbn);
    }
}
