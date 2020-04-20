package io.ppmtool.services;

import io.ppmtool.domain.Backlog;
import io.ppmtool.domain.Project;
import io.ppmtool.domain.User;
import io.ppmtool.exceptions.BacklogNotFoundException;
import io.ppmtool.exceptions.ProjectIdException;
import io.ppmtool.repositories.BacklogRepository;
import io.ppmtool.repositories.ProjectRepository;
import io.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private BacklogRepository backlogRepository;
    @Autowired
    private UserRepository userRepository;

    public Project saveOrUpdateProject(Project project, String username){

        if(project.getProjectIdentifier().length() > 5){
            throw new ProjectIdException("Project Identifier should be less than 6 characters");
        }

        String userProjectId = project.getProjectIdentifier().toUpperCase();
        String savedProjectId = username + userProjectId;

        // 如果传入的 project id 不为空，说明用户正在尝试更新一个 project
        if(project.getId() != null){
            Project existingProject = projectRepository.findByProjectIdentifier(savedProjectId);

            if(existingProject != null && (!existingProject.getProjectLeader().equals(username))){
                throw new BacklogNotFoundException(("Update Failed: Project not found in your account"));
            }else if(existingProject != null && existingProject.getId() != project.getId()){
                throw new BacklogNotFoundException(("Update Failed: Project ID and Project Identifier does not match"));
            }else if(existingProject == null){
                throw new BacklogNotFoundException("Updated Failed: Project with ID '" + userProjectId + "' cannot be updated because it doesn't exist");
            }
        }

        // Create Project or Overwrite existing project
        try{
            User user = userRepository.findByUsername(username);
            project.setUser(user);
            project.setProjectLeader(username);
            project.setProjectIdentifier(savedProjectId);

            if(project.getId() == null){
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(savedProjectId);
            }

            if(project.getId() != null){
                project.setBacklog(backlogRepository.findByProjectIdentifier(savedProjectId));
            }

            Project returnProject = projectRepository.save(project);
            returnProject.setProjectIdentifier(userProjectId);
            return returnProject;
        }catch (Exception e){
            throw new ProjectIdException("Project ID '" + userProjectId + "' already exists");
        }
    }

    /*
    projectId: user version
    check project exists, check the existing project belongs to the user
    return project in user version
     */
    public Project findProjectByIdentifier(String projectId, String username){
        String userProjectId = projectId.toUpperCase();
        String savedProjectId = username + userProjectId;

        Project project = projectRepository.findByProjectIdentifier(savedProjectId);
        if(project == null){
            throw new ProjectIdException("Project ID '" + userProjectId + "' does not exist");
        }
        if(!project.getProjectLeader().equals(username)){
            throw new ProjectIdException("Project ID '" + userProjectId + "' does not exist");
        }
        project.setProjectIdentifier(userProjectId);
        return project;
    }

    public Iterable<Project> findAllProjects(String username){
        Iterable<Project> projectList = projectRepository.findAllByProjectLeader(username);
        for(Project project: projectList){
            String savedProjectId = project.getProjectIdentifier();
            String userProjectId = savedProjectId.substring(username.length());
            project.setProjectIdentifier(userProjectId);
        }
        return projectList;
    }

    public void deleteProjectByIdentifier(String projectId, String username){
        projectRepository.delete(findProjectByIdentifier(projectId, username));
    }
}
