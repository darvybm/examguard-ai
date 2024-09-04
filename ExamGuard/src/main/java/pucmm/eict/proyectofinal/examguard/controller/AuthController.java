package pucmm.eict.proyectofinal.examguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pucmm.eict.proyectofinal.examguard.dto.ChangePasswordDTO;
import pucmm.eict.proyectofinal.examguard.dto.UserDTO;
import pucmm.eict.proyectofinal.examguard.model.User;
import pucmm.eict.proyectofinal.examguard.model.enums.UserRole;
import pucmm.eict.proyectofinal.examguard.service.FolderService;
import pucmm.eict.proyectofinal.examguard.service.MailService;
import pucmm.eict.proyectofinal.examguard.service.RecordingService;
import pucmm.eict.proyectofinal.examguard.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RecordingService recordingService;
    private final FolderService folderService;
    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO,
                               BindingResult result,
                               Model model) {

        // Verifica si el email ya está registrado
        if (userService.existByEmail(userDTO.getEmail())) {
            result.rejectValue("email", "error.UserDTO", "Email already taken");
        }

        // Verifica si hay errores de validación
        if (result.hasErrors()) {
            return "auth/register"; // Muestra la página de registro con errores
        }

        User user = objectMapper.convertValue(userDTO, User.class);
        user.setId(new ObjectId());
        user.setUserRole(UserRole.USER);
        userService.createUser(user);

        // Enviar correo de confirmación

        mailService.sendEmailRegistration(user);

        return "redirect:/login"; // Redirige al inicio de sesión
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO changePasswordDTO,
                                            BindingResult result) {

        User user = userService.getLoggedUser();

        // Verificar la contraseña actual
        if (result.hasErrors()) {
            if (!changePasswordDTO.getCurrentPassword().isEmpty() && !userService.checkPassword(user, changePasswordDTO.getCurrentPassword())){
                result.rejectValue("currentPassword", "error.ChangePasswordDTO", "Current password is incorrect");
            }
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        if (!userService.checkPassword(user, changePasswordDTO.getCurrentPassword())) {
            result.rejectValue("currentPassword", "error.ChangePasswordDTO", "Current password is incorrect");
        }

        // Verificar que las nuevas contraseñas coincidan
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
            result.rejectValue("confirmNewPassword", "error.ChangePasswordDTO", "New password and confirmation do not match");
        }

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        // Actualizar la contraseña
        userService.changePassword(user, changePasswordDTO.getNewPassword());

        return ResponseEntity.ok().body("Password updated successfully");
    }

}
