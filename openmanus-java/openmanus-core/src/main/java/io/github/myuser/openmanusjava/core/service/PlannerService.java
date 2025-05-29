package io.github.myuser.openmanusjava.core.service;

import io.github.myuser.openmanusjava.core.exception.PlanningException;
import io.github.myuser.openmanusjava.core.model.Step;
import io.github.myuser.openmanusjava.core.model.Task;
import java.util.List;

public interface PlannerService {

    /**
     * Creates an execution plan (a list of steps) for a given task description.
     * This typically involves interacting with an LLM.
     * @param task The task for which to create a plan.
     * @param userGoal The original user goal or task description.
     * @return A list of Step objects representing the plan.
     * @throws PlanningException if plan creation fails.
     */
    List<Step> createPlan(Task task, String userGoal) throws PlanningException;
}
