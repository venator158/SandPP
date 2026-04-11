package com.example.pathsandbox.app.repository;

import com.example.pathsandbox.app.model.PathExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathExecutionLogRepository extends JpaRepository<PathExecutionLog, Long> {
}
