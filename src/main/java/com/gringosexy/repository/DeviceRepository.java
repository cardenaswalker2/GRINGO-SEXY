package com.gringosexy.repository;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    Optional<Device> findByType(DeviceType type);

    boolean existsByType(DeviceType type);

    List<Device> findAllByOrderBySortOrderAsc();

    List<Device> findByActiveTrueOrderBySortOrderAsc();
}
