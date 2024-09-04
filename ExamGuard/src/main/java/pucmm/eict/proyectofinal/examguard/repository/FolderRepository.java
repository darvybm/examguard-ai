package pucmm.eict.proyectofinal.examguard.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pucmm.eict.proyectofinal.examguard.model.Folder;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends MongoRepository<Folder, ObjectId> {
    List<Folder> findAllByUser(User loggedUser);

    long countAllByUser(User loggedUser);
}
