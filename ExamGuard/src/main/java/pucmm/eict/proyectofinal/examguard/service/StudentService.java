package pucmm.eict.proyectofinal.examguard.service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pucmm.eict.proyectofinal.examguard.exception.FolderAccessDeniedException;
import pucmm.eict.proyectofinal.examguard.model.*;
import pucmm.eict.proyectofinal.examguard.repository.StudentRepository;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserService userService;

    private void checkUserAccess(Student student, User loggedUser) {
        if (!student.getUser().equals(loggedUser)) {
            throw new FolderAccessDeniedException("Access denied: User does not own this folder.");
        }
    }

    public Page<Student> getAllStudentsPageable(Pageable pageable, User loggedUser) {
        return studentRepository.findAllByUser(pageable, loggedUser);
    }

    public List<Student> searchValueAndSorting(List<Student> t, String searchValue, int orderColumn, String orderDirection) {

        return t.stream()
                .filter(containsText(normalize(searchValue.toLowerCase())))
                .toList()
                .stream()
                .sorted(buildSorter(orderColumn, orderDirection))
                .toList();
    }

    public Comparator<Student> buildSorter(int orderColumn, String orderDirection) {
        List<Comparator<Student>> sortChain = new ArrayList<>();
        sortChain.add(buildComparatorClause(orderColumn, orderDirection));

        return (student1, student2) -> {
            for (Comparator<Student> comparator : sortChain) {
                int result = comparator.compare(student1, student2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }

    private Comparator<Student> buildComparatorClause(int orderColumn, String orderDirection) {
        // Map column indices to comparators for conciseness
        Map<Integer, Comparator<Student>> comparators = Map.of(
                1, Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER),
                2, Comparator.comparing(Student::getEmail, String.CASE_INSENSITIVE_ORDER));

        Comparator<Student> comp = comparators.getOrDefault(orderColumn, Comparator.comparing(Student::getId));

        return "asc".equals(orderDirection) ? comp : comp.reversed();
    }

    public Predicate<Student> containsText(String searchTerm) {
        return student -> {
            // Normaliza el término de búsqueda
            String normalizedSearchTerm = normalize(searchTerm.toLowerCase());

            // Verifica si el campo name del estudiante contiene el término de búsqueda
            boolean nameMatches = student.getName() != null && normalize(student.getName().toLowerCase()).contains(normalizedSearchTerm);

            // Verifica si el campo email del estudiante contiene el término de búsqueda
            boolean emailMatches = student.getEmail() != null && normalize(student.getEmail().toLowerCase()).contains(normalizedSearchTerm);

            // Si hay alguna coincidencia, retorna true
            return nameMatches || emailMatches;
        };
    }

    private boolean eventContainsText(Event event, String searchTerm) {
        // Implement logic to check if event contains search term
        return event != null && normalize(event.toString().toLowerCase()).contains(searchTerm);
    }

    private String normalize(String input) {
        if (input == null) {
            return null;
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{Mn}", "");
    }

    public List<Student> getAllStudents(User loggedUser) {
        return studentRepository.findAllByUser(loggedUser);
    }

    public void createStudent(Student student, User loggedUser) {
        checkUserAccess(student, loggedUser);
        studentRepository.save(student);
    }

    public Optional<Student> getStudentById(ObjectId id, User loggedUser) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        optionalStudent.ifPresent(student -> checkUserAccess(student, loggedUser));
        return optionalStudent;
    }

    public void updateStudent(Student student, User loggedUser) {
        checkUserAccess(student, loggedUser);
        studentRepository.save(student);
    }

    public void deleteStudent(ObjectId studentId, User loggedUser) {
        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        optionalStudent.ifPresent(student -> checkUserAccess(student, loggedUser));
        optionalStudent.ifPresent(studentRepository::delete);
    }
}
