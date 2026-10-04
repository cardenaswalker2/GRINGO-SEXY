package com.gringosexy.dto;

import com.gringosexy.enums.DeviceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String fullName;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String username;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Ingresa un correo electrónico válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Debes confirmar tu contraseña")
    private String confirmPassword;

    @NotNull(message = "Debes seleccionar tu dispositivo principal")
    private DeviceType deviceType;

    private String requestedModule = "ALL";

    public RegisterRequest() {
    }

    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getRequestedModule() {
        return requestedModule != null ? requestedModule : "ALL";
    }

    public void setRequestedModule(String requestedModule) {
        this.requestedModule = requestedModule;
    }
}
