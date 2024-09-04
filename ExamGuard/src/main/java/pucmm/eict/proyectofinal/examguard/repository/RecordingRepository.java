package pucmm.eict.proyectofinal.examguard.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.User;

import java.util.List;

@Repository
public interface RecordingRepository extends MongoRepository<Recording, ObjectId> {
    Page<Recording> findAllByFolderId(ObjectId objectId, Pageable pageable);

    List<Recording> findAllByUser(User loggedUser);

    Page<Recording> findAllByUser(User loggedUser, Pageable pageable);

    long countByUser(User user);

    long countByUserAndProcessed(User user, boolean b);
}
