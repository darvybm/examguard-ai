package pucmm.eict.proyectofinal.examguard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pucmm.eict.proyectofinal.examguard.dto.StudentAssociationDTO;
import pucmm.eict.proyectofinal.examguard.dto.StudentDTO;
import pucmm.eict.proyectofinal.examguard.exception.StudentNotFoundException;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.Student;
import pucmm.eict.proyectofinal.examguard.service.*;
import pucmm.eict.proyectofinal.examguard.utils.DataTableResponse;

import java.util.*;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final UserService userService;
    private final RecordingService recordingService;

    @GetMapping("")
    public String recordingList(Model model) {
        model.addAttribute("students", studentService.getAllStudents(userService.getLoggedUser()));
        return "student/list";
    }

    @GetMapping("/api/students")
    @ResponseBody
    public DataTableResponse<Student> getStudents(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam(value = "search[value]", required = false) String searchValue,
            @RequestParam(value = "order[0][column]", required = false) int orderColumn,
            @RequestParam(value = "order[0][dir]", required = false) String orderDirection) {

        System.out.println("ENTRANDO A API");

        try {
            Pageable pageable = PageRequest.of(start / length, length);
            Page<Student> studentPage = studentService.getAllStudentsPageable(pageable, userService.getLoggedUser());

            List<Student> students = studentService.searchValueAndSorting(studentPage.getContent(), searchValue, orderColumn, orderDirection);

            System.out.println("ESTUDIANTES");
            System.out.println(students);

            return new DataTableResponse<>(
                    draw,
                    studentPage.getTotalElements(),
                    studentPage.getTotalElements(),
                    students
            );
        } catch (Exception e) {
            return new DataTableResponse<>(draw, 0, 0, Collections.emptyList());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentDTO studentDTO, BindingResult result) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Student student = new Student();
            student.setId(new ObjectId());
            student.setName(studentDTO.getName());
            student.setEmail(studentDTO.getEmail());
            student.setUser(userService.getLoggedUser()); // Asumiendo que obtienes al usuario logueado

            studentService.createStudent(student, userService.getLoggedUser());

            return ResponseEntity.status(HttpStatus.CREATED).body("Estudiante creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el Estudiante");
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editStudent(@PathVariable ObjectId id, @Valid @RequestBody StudentDTO studentDTO, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Student> optionalStudent = studentService.getStudentById(id, userService.getLoggedUser());

        if (optionalStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder no encontrado");
        }

        try {
            Student student = optionalStudent.get();
            student.setName(studentDTO.getName());
            student.setEmail(studentDTO.getEmail());
            student.setUser(userService.getLoggedUser());

            studentService.updateStudent(student,userService.getLoggedUser());

            return ResponseEntity.ok("Folder actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el folder");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable ObjectId id) {
        try {
            studentService.deleteStudent(id, userService.getLoggedUser());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/associate-student")
    public ResponseEntity<?> associateStudent(@RequestBody StudentAssociationDTO associationDTO) {
        try {
            Student student = studentService.getStudentById(associationDTO.getStudentId(), userService.getLoggedUser())
                    .orElseThrow(() -> new StudentNotFoundException("Estudiante no encontrado"));

            Recording recording = recordingService.getRecordingById(associationDTO.getRecordingId(), userService.getLoggedUser())
                    .orElseThrow(() -> new StudentNotFoundException("Grabación no encontrada"));

            recording.setStudent(student);
            recordingService.updateRecording(recording, userService.getLoggedUser());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Manejar excepciones según sea necesario
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al asociar el estudiante con la grabación");
        }
    }

    @DeleteMapping("/disassociate-student")
    public ResponseEntity<?> disassociateStudent(@RequestBody StudentAssociationDTO associationDTO) {
        try {

            Recording recording = recordingService.getRecordingById(associationDTO.getRecordingId(), userService.getLoggedUser())
                    .orElseThrow(() -> new StudentNotFoundException("Grabación no encontrada"));

            recording.setStudent(null);
            recordingService.updateRecording(recording, userService.getLoggedUser());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Manejar excepciones según sea necesario
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al asociar el estudiante con la grabación");
        }
    }

}
