package io.ppmtool.services;

import io.ppmtool.domain.Backlog;
import io.ppmtool.domain.ProjectTask;
import io.ppmtool.exceptions.BacklogNotFoundException;
import io.ppmtool.exceptions.ProjectIdException;
import io.ppmtool.repositories.BacklogRepository;
import io.ppmtool.repositories.ProjectRepository;
import io.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectTaskService {

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username){

        String userProjectId = projectIdentifier.toUpperCase();
        String savedProjectId = username + userProjectId;

        // PTs to be added to specific project, project != nul, backlog exists
        Backlog backlog = projectService.findProjectByIdentifier(userProjectId, username).getBacklog();

        // set the backlog to pt
        projectTask.setBacklog(backlog);

        // we want our project sequence to be like this: TESTT-1 TESTT-2
        Integer BacklogSequence = backlog.getPTSequence();
        BacklogSequence ++;
        backlog.setPTSequence(BacklogSequence);

        // add sequence to projectTask
        projectTask.setProjectSequence((savedProjectId + "-" + BacklogSequence));
        projectTask.setProjectIdentifier(savedProjectId);

        // initial priority when priority null
        if(projectTask.getPriority() == null || projectTask.getPriority() == 0){
            projectTask.setPriority(3);
        }
        // initial status when status is null
        if(projectTask.getStatus() == null || projectTask.getStatus() == ""){
            projectTask.setStatus("TO_DO");
        }

        ProjectTask returnProjectTask = projectTaskRepository.save(projectTask);
        returnProjectTask.setProjectSequence((username + userProjectId + "-" + BacklogSequence));
        returnProjectTask.setProjectIdentifier(userProjectId);
        return returnProjectTask;

    }

    public Iterable<ProjectTask> findBacklogById(String id, String username) {

        projectService.findProjectByIdentifier(id, username);
        Iterable<ProjectTask> list = projectTaskRepository.findByProjectIdentifierOrderByPriority(username + id);
        for(ProjectTask pt: list){
            pt.setProjectIdentifier(id);
            String savedps = pt.getProjectSequence();
            pt.setProjectSequence(savedps.substring(username.length()));
        }

        return list;
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username){

        // backlog_id and pt_id are user inputs, not same as records in the database

        // make sure we are searching on an existing backlog
        projectService.findProjectByIdentifier(backlog_id, username);

        // make sure that our task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(username + pt_id);
        if(projectTask == null){
            throw new BacklogNotFoundException("Project Task '" + pt_id + "' does not exist in project '" + backlog_id + "'");
        }
        // make sure that the backlog/project id in the path corresponds to the right project
        if(!projectTask.getProjectIdentifier().equals(username + backlog_id)){
            throw new BacklogNotFoundException("Project Task '" + pt_id + "' does not exist in project '" + backlog_id + "'");
        }

        projectTask.setProjectSequence(pt_id);
        projectTask.setProjectIdentifier(backlog_id);
        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username){
        // check if the backlog exists
        projectService.findProjectByIdentifier(backlog_id, username);

        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(username + pt_id);
        // check this pt_id corresponds to an existing project task in database
        if(projectTask == null){
            throw new BacklogNotFoundException("Project Task '" + pt_id + "' does not exist in project '" + backlog_id + "'");
        }
        // check the existing project task belongs to the user
        if(!projectTask.getProjectIdentifier().equals(username + backlog_id)){
            throw new BacklogNotFoundException("Project Task '" + pt_id + "' does not exist in project '" + backlog_id + "'");
        }

        // validate new project
        if(updatedTask.getId() != projectTask.getId()){
            throw new ProjectIdException("Update Failed: the id of the new project is wrong");
        }

        // change the project task to be an acceptable one and save it in database
        projectTask = updatedTask;
        projectTask.setProjectIdentifier(username + backlog_id);
        projectTask.setProjectSequence(username + pt_id);
        ProjectTask returnProjectTask = projectTaskRepository.save(projectTask);

        // return a user version project task
        returnProjectTask.setProjectIdentifier(backlog_id);
        returnProjectTask.setProjectSequence(pt_id);

        return returnProjectTask;
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);
        projectTaskRepository.delete(projectTask);
    }
}
