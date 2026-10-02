package com.gringosexy.controller;

import com.gringosexy.dto.RegisterRequest;
import com.gringosexy.exception.ValidationException;
import com.gringosexy.service.DeviceService;
import com.gringosexy.service.UserService;
import com.gringosexy.service.VerificationService;
import com.gringosexy.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final DeviceService deviceService;
    private final VerificationService verificationService;

    public AuthController(UserService userService,
                          DeviceService deviceService,
                          VerificationService verificationService) {
        this.userService = userService;
        this.deviceService = deviceService;
        this.verificationService = verificationService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "verified", required = false) String verified,
                            @RequestParam(value = "reset", required = false) String reset,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Credenciales incorrectas o tu cuenta no está habilitada.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión correctamente.");
        }
        if (verified != null) {
            model.addAttribute("successMessage", "¡Tu correo ha sido verificado exitosamente! Ya puedes iniciar sesión.");
        }
        if (reset != null) {
            model.addAttribute("successMessage", "Tu contraseña ha sido restablecida. Inicia sesión con tus nuevas credenciales.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("devices", deviceService.getActiveDevices());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("devices", deviceService.getActiveDevices());
            return "auth/register";
        }

        try {
            String clientIp = SecurityUtils.getClientIp(request);
            userService.registerUser(registerRequest, clientIp);
            return "redirect:/verify-email?email=" + registerRequest.getEmail();
        } catch (ValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("devices", deviceService.getActiveDevices());
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ocurrió un error al procesar el registro. Inténtalo nuevamente.");
            model.addAttribute("devices", deviceService.getActiveDevices());
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmailNoticeOrToken(@RequestParam(value = "token", required = false) String token,
                                           @RequestParam(value = "email", required = false) String email,
                                           Model model) {
        if (token != null && !token.trim().isEmpty()) {
            try {
                verificationService.verifyEmailToken(token);
                model.addAttribute("success", true);
                model.addAttribute("title", "¡Correo Verificado con Éxito!");
                model.addAttribute("message", "Tu cuenta ha sido confirmada y activada en GRINGO SEXY.");
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("title", "Error de Verificación");
                model.addAttribute("message", e.getMessage());
            }
            return "auth/verification-result";
        }

        model.addAttribute("userEmail", email != null ? email : "tu correo electrónico");
        return "auth/verify-email";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        String clientIp = SecurityUtils.getClientIp(request);
        userService.initiatePasswordReset(email, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Si el correo coincide con una cuenta activa, te hemos enviado un enlace para restablecer tu contraseña.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "token", required = false) String token, Model model) {
        if (token == null || token.trim().isEmpty()) {
            return "redirect:/login";
        }
        try {
            verificationService.validatePasswordResetToken(token);
            model.addAttribute("token", token);
            return "auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("title", "Enlace Inválido o Expirado");
            model.addAttribute("message", e.getMessage());
            return "auth/verification-result";
        }
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      HttpServletRequest request,
                                      Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
            return "auth/reset-password";
        }

        try {
            String clientIp = SecurityUtils.getClientIp(request);
            userService.resetPassword(token, password, clientIp);
            return "redirect:/login?reset=true";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }
}
