package com.gringosexy.service.impl;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.model.Device;
import com.gringosexy.repository.DeviceRepository;
import com.gringosexy.service.DeviceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public List<Device> getActiveDevices() {
        return deviceRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @Override
    public Device getDeviceByType(DeviceType type) {
        return deviceRepository.findByType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado para tipo: " + type));
    }

    @Override
    public Device getDeviceById(String id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado con ID: " + id));
    }

    @Override
    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    public Device createOrUpdateDevice(String id, String name, String codeName, String description, String iconClass, int sortOrder, boolean active) {
        Device device;
        DeviceType dType = DeviceType.OTHER;
        if (codeName != null && !codeName.trim().isEmpty()) {
            try {
                dType = DeviceType.valueOf(codeName.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                dType = DeviceType.OTHER;
            }
        }

        if (id != null && !id.trim().isEmpty()) {
            device = getDeviceById(id);
        } else {
            device = new Device();
            device.setCreatedAt(Instant.now());
        }

        device.setName(name.trim());
        device.setType(dType);
        device.setDescription(description != null ? description.trim() : "");
        device.setIconClass(iconClass != null && !iconClass.trim().isEmpty() ? iconClass.trim() : "fas fa-mobile-alt");
        device.setSortOrder(sortOrder);
        device.setActive(active);
        device.setUpdatedAt(Instant.now());

        return deviceRepository.save(device);
    }

    @Override
    public void deleteDevice(String id) {
        Device device = getDeviceById(id);
        deviceRepository.delete(device);
    }

    @Override
    public Device toggleActive(String id) {
        Device device = getDeviceById(id);
        device.setActive(!device.isActive());
        device.setUpdatedAt(Instant.now());
        return deviceRepository.save(device);
    }

    @Override
    public void initDefaultDevices() {
        DeviceType[] coreDevices = new DeviceType[] {
            DeviceType.SAMSUNG,
            DeviceType.MOTOROLA,
            DeviceType.XIAOMI,
            DeviceType.REALME,
            DeviceType.IPHONE
        };

        int order = 1;
        for (DeviceType type : coreDevices) {
            Device dev = deviceRepository.findByType(type).orElse(null);
            if (dev == null) {
                dev = new Device(type, type.getDisplayName(), type.getDescription(), type.getIconClass(), order);
            } else {
                dev.setName(type.getDisplayName());
                dev.setDescription(type.getDescription());
                dev.setIconClass(type.getIconClass());
                dev.setSortOrder(order);
                dev.setActive(true);
            }
            deviceRepository.save(dev);
            order++;
        }

        // Delete non-core devices from repository
        List<Device> all = deviceRepository.findAll();
        for (Device existing : all) {
            if (existing.getType() != DeviceType.SAMSUNG &&
                existing.getType() != DeviceType.MOTOROLA &&
                existing.getType() != DeviceType.XIAOMI &&
                existing.getType() != DeviceType.REALME &&
                existing.getType() != DeviceType.IPHONE) {
                deviceRepository.delete(existing);
            }
        }
    }
}
