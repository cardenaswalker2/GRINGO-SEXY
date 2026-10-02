package com.gringosexy.controller.admin;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.model.Device;
import com.gringosexy.service.DeviceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/devices")
public class AdminDeviceController {

    private final DeviceService deviceService;

    public AdminDeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public String listDevices(Model model) {
        model.addAttribute("devices", deviceService.getAllDevices());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("pageTitle", "Gestión de Dispositivos — GRINGO SEXY");
        return "admin/devices";
    }

    @PostMapping("/toggle/{id}")
    public String toggleDevice(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        Device device = deviceService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Dispositivo '" + device.getName() + "' " + (device.isActive() ? "activado" : "desactivado") + " con éxito.");
        return "redirect:/admin/devices";
    }

    @PostMapping("/save")
    public String saveDevice(@RequestParam(value = "id", required = false) String id,
                             @RequestParam("name") String name,
                             @RequestParam(value = "codeName", defaultValue = "OTHER") String codeName,
                             @RequestParam(value = "description", defaultValue = "") String description,
                             @RequestParam(value = "iconClass", defaultValue = "fas fa-mobile-alt") String iconClass,
                             @RequestParam(value = "sortOrder", defaultValue = "0") int sortOrder,
                             @RequestParam(value = "active", defaultValue = "true") boolean active,
                             RedirectAttributes redirectAttributes) {
        deviceService.createOrUpdateDevice(id, name, codeName, description, iconClass, sortOrder, active);
        redirectAttributes.addFlashAttribute("successMessage", "¡Dispositivo guardado exitosamente!");
        return "redirect:/admin/devices";
    }

    @PostMapping("/delete/{id}")
    public String deleteDevice(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        deviceService.deleteDevice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Dispositivo eliminado correctamente.");
        return "redirect:/admin/devices";
    }
}
