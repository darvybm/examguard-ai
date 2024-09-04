package pucmm.eict.proyectofinal.examguard.service;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import pucmm.eict.proyectofinal.examguard.exception.FolderAccessDeniedException;
import pucmm.eict.proyectofinal.examguard.model.Folder;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.User;
import pucmm.eict.proyectofinal.examguard.repository.FolderRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FolderService {

    private final RecordingService recordingService;
    private final FolderRepository folderRepository;

    private void checkUserAccess(Folder folder, User loggedUser) {
        if (!folder.getUser().equals(loggedUser)) {
            throw new FolderAccessDeniedException("Access denied: User does not own this folder.");
        }
    }

    public void createFolder(Folder folder, User loggedUser) {
        // Verificar que el folder pertenece al usuario logueado
        folder.setUser(loggedUser);
        folderRepository.save(folder);
    }

    public Optional<Folder> getFolderById(ObjectId id, User loggedUser) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid folder Id:" + id));
        // Verificar que el usuario logueado es el propietario del folder
        checkUserAccess(folder, loggedUser);
        return Optional.of(folder);
    }

    public void deleteFolder(ObjectId folderId, User loggedUser) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid folder Id:" + folderId));

        // Verificar que el usuario logueado es el propietario del folder
        checkUserAccess(folder, loggedUser);

        // Eliminar todos los recordings asociados al folder
        for (Recording recording : folder.getRecordings()) {
            recordingService.deleteRecording(recording.getId(), loggedUser);
        }

        // Eliminar el folder
        folderRepository.delete(folder);
    }

    public void updateFolder(Folder folder, User loggedUser) {
        // Verificar que el folder pertenece al usuario logueado
        Folder existingFolder = folderRepository.findById(folder.getId())
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        checkUserAccess(existingFolder, loggedUser);

        folderRepository.save(folder);
    }

    public List<Folder> getFoldersByUser(User loggedUser) {
        return folderRepository.findAllByUser(loggedUser);
    }

    public long countFoldersByUser(User loggedUser) {
        return folderRepository.countAllByUser(loggedUser);
    }
}
