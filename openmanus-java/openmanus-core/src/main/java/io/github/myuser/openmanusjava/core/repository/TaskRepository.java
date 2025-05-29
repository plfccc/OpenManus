package io.github.myuser.openmanusjava.core.repository;

import io.github.myuser.openmanusjava.core.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Custom query methods can be added here if needed
}
