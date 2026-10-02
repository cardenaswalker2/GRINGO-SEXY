package com.gringosexy.repository;

import com.gringosexy.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {

    List<ActivityLog> findTop10ByOrderByTimestampDesc();

    Page<ActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<ActivityLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    Page<ActivityLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
}
