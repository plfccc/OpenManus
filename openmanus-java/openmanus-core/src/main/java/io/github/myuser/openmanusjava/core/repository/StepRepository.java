package io.github.myuser.openmanusjava.core.repository;

import io.github.myuser.openmanusjava.core.model.Step;
// import io.github.myuser.openmanusjava.core.model.Task; // Not needed for JpaRepository<Step, Long>
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // Not used in this basic version

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    // Custom query methods can be added here if needed
    // Example:
    // List<Step> findByTaskIdOrderBySequenceOrderAsc(Long taskId); // If Step had taskId directly
    // Since Step has a Task object, more complex queries might involve joins or specific Task object:
    // List<Step> findByTaskOrderBySequenceOrderAsc(Task task);
}
