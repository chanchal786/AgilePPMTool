package com.csk.ppmtool.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csk.ppmtool.domain.Backlog;
import com.csk.ppmtool.domain.Project;
import com.csk.ppmtool.domain.User;
import com.csk.ppmtool.exceptions.ProjectIdException;
import com.csk.ppmtool.exceptions.ProjectNotFoundException;
import com.csk.ppmtool.repositories.BacklogRepoitory;
import com.csk.ppmtool.repositories.ProjectRepository;
import com.csk.ppmtool.repositories.UserRepository;

@Service
public class ProjectService {
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private BacklogRepoitory backlogRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	public Project saveOrUpdateProject(Project project, String username) {
		
		if(project.getId()!=null) {
			Project existingProject=projectRepository.findByProjectIdentifier(project.getProjectIdentifier());
			if(existingProject!=null&&!existingProject.getProjectLeader().equals(username)) {
				throw new ProjectNotFoundException("Project not found in your account");
			}else if(existingProject==null) {
				throw new ProjectNotFoundException("Project ID : \""+project.getProjectIdentifier()+"\" can't be updated because it doesn't exits");
			}
		}
		
		try {
			User user=userRepository.findByUsername(username);
			project.setUser(user);
			project.setUpdatedAt(new Date());
			project.setProjectLeader(user.getUsername());
			project.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());
			if(project.getId()==null) {
				Backlog backlog=new Backlog();
				project.setBacklog(backlog);
				backlog.setProject(project);
				backlog.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());
			}
//			if(project.getId()!=null) {
			else {
				project.setBacklog(backlogRepository.findByProjectIdentifier(project.getProjectIdentifier().toUpperCase()));
			}
			return projectRepository.save(project);
		}
		catch(Exception e) {
			throw new ProjectIdException("Project Id : \""+project.getProjectIdentifier().toUpperCase()+"\" already exits");
		}
	}
	
	public Project findProjectByIdentifier(String projectId, String username) {
		Project project=projectRepository.findByProjectIdentifier(projectId);
		if(project==null) {
			throw new ProjectIdException("Project Id : \""+projectId+"\" does not exits");
		}
		if(!project.getProjectLeader().equals(username)) {
			throw new ProjectNotFoundException("Project not found in your account");
		}
		
		return project;
	}
	public Iterable<Project> findAllProject(String username){
		return projectRepository.findAllByProjectLeader(username);
	}
	
	public void deleteProjectByIdentifier(String projectId, String username) {
		/*
		 * Project project=projectRepository.findByProjectIdentifier(projectId);
		 * if(project==null) { throw new
		 * ProjectIdException("Cannot Project with Id : \""
		 * +projectId+"\" This project does not exits"); }
		 */		projectRepository.delete(findProjectByIdentifier(projectId,username));
	}
	
}
