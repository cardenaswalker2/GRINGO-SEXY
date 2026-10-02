package com.gringosexy.service;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.model.Device;

import java.util.List;

public interface DeviceService {

    List<Device> getAllDevices();

    List<Device> getActiveDevices();

    Device getDeviceByType(DeviceType type);

    Device getDeviceById(String id);

    Device saveDevice(Device device);

    Device createOrUpdateDevice(String id, String name, String codeName, String description, String iconClass, int sortOrder, boolean active);

    void deleteDevice(String id);

    Device toggleActive(String id);

    void initDefaultDevices();
}
