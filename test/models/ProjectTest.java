package models;
import static org.hamcrest.Matchers.*;
import static matchers.Matchers.recentDate;

import java.util.List;

import org.junit.Test;

/**
 * Work out the basic functionality of the Project Model
 * <p>
 * Note that there are other tests that exercise the Project.
 * For example, the StateStatistics test checks that project
 * level statistics are kept correctly.
 * 
 * @author mgjv
 */
// TODO add multiple project tests.
public class ProjectTest extends BasicModelTest {
	
    /**
     * Test that new projects can be created, and have the right defaults
     */
    @Test
    public void newProjectTest() {   
    	Project project = new Project("New project", getDefaultUser());
    	assertThat(project, notNullValue());
    	project.save();
    	
    	List<Project> projects = Project.findAll();
    	assertThat(projects.size(), is(1));
    	project = projects.get(0);
    	assertThat(project, notNullValue());
    	assertThat(project.createdOn, recentDate());
    	assertThat(project.createdUser, is(getDefaultUser()));
    	
    	project = new Project("Another project", getDefaultUser());
    	project.save();
    	projects = Project.findAll();
    	assertThat(projects.size(), is(2));
    	project = new Project("Yet another project", getDefaultUser());
    	project.save();
    	projects = Project.findAll();
    	assertThat(projects.size(), is(3));
    	for (int i = 0; i < 3; i++) {
    		project = projects.get(i);
    		assertThat(project, notNullValue());
    	}
    }
    
    /**
     * Test that adding and removing states works
     */
    @Test
    public void addAndRemoveStatesTest() {
    	Project project = new Project("New project", getDefaultUser());
    	assertThat(project, notNullValue());
    	assertThat(project.states.size(), is(5));
    	project.save();
    	
    	project = Project.all().first();
    	
    	// Ensure that the order of the default states is preserved
    	assertThat(project.states.get(0).name, is("Sandbox"));
    	assertThat(project.states.get(1).name, is("Backlog"));
    	assertThat(project.states.get(2).name, is("In Progress"));
    	assertThat(project.states.get(3).name, is("Completed"));
    	assertThat(project.states.get(4).name, is("Archive"));
    	
    	// Try to insert a new state, and remove it
    	project.addState(1, "Test state", "This is a test State", null);
    	project.save();
    	project = Project.all().first();
    	assertThat(project.states.get(1).name, is("Test state"));
    	assertThat(project.states.get(1).limit, is(nullValue()));
    	assertThat(project.states.get(1).description, is("This is a test State"));
       	assertThat(project.states.size(), is(6));
       	project.removeState(project.states.get(1));
    	project.save();
    	project = Project.all().first();
       	assertThat(project.states.get(1).name, is(not("Test State")));
       	assertThat(project.states.size(), is(5));
       	
       	// Try to insert a new state at the start and observe it being at position 1
    	project.addState(0, "Test state", "This is a test State", null);
    	assertThat(project.states.get(0).name, is("Sandbox"));
    	assertThat(project.states.get(1).name, is("Test state"));
       	project.removeState(project.states.get(1));

       	// Try to insert a new state at the end and observe it being at position size() - 2
    	project.addState(5, "Test state", "This is a test State", null);
    	assertThat(project.states.get(5).name, is("Archive"));
    	assertThat(project.states.get(4).name, is("Test state"));
       	project.removeState(project.states.get(3));

       	// Try to remove the first and the last element
    	project.removeState(project.states.get(0));
       	assertThat(project.states.size(), is(5));
    	project.removeState(project.states.get(4));
       	assertThat(project.states.size(), is(5));
    }
    
    /**
     * Test that stories are migrated correctly on state removal
     * <p>
     * Remove a project state that has stories in it.
     * Ensure the stories end up with another state
     */
    @Test
    public void removeStateWithStory() {
    	Project project = getDefaultProject();
    	Story story = project.defaultState().newStory("Story 1", getDefaultUser());
    	assertThat(project.defaultState().stories.size(), is(1));
    	project.addState(3, "Temp state", "Just a temporary state", 12);
    	project.save();
    	assertThat(project.states.get(3).name, is("Temp state"));
    	assertThat(project.states.get(3).limit, is(12));
    	State state = project.states.get(3);
    	state.addStory(story);
    	state.save();
    	
    	project.removeState(state);
    	assertThat(story.getState(), is(not(state)));
    	assertThat(story.getState(), is(project.states.get(2)));
    	project.save();
    }
}
