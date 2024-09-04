package pucmm.eict.proyectofinal.examguard.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.Student;
import pucmm.eict.proyectofinal.examguard.model.User;

import java.util.List;

@Repository
public interface StudentRepository extends MongoRepository<Student, ObjectId>{
    Page<Student> findAllByUser(Pageable pageable, User user);

    List<Student> findAllByUser(User loggedUser);
}
