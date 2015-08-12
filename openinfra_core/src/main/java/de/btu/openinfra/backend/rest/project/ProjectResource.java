package de.btu.openinfra.backend.rest.project;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.btu.openinfra.backend.db.daos.OpenInfraSchemas;
import de.btu.openinfra.backend.db.daos.ProjectDao;
import de.btu.openinfra.backend.db.daos.PtLocaleDao;
import de.btu.openinfra.backend.db.pojos.ProjectPojo;
import de.btu.openinfra.backend.rest.OpenInfraResponseBuilder;

/**
 * This class represents the project resource of the REST API. You can access
 * an overview of all available HTTP methods by calling the following link:
 * <a href="http://localhost:8080/openinfra_backend/rest/application.wadl">
 * http://localhost:8080/openinfra_backend/rest/application.wadl</a>
 *
 * The project resource is available by calling the following link:
 * <a href="http://localhost:8080/openinfra_backend/rest/projects">
 * http://localhost:8080/openinfra_backend/rest/projects</a>
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
@Path("/projects")
@Produces({MediaType.APPLICATION_JSON + OpenInfraResponseBuilder.JSON_PRIORITY
    + OpenInfraResponseBuilder.UTF8_CHARSET,
	MediaType.APPLICATION_XML + OpenInfraResponseBuilder.XML_PRIORITY
	+ OpenInfraResponseBuilder.UTF8_CHARSET})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProjectResource {

	/**
	 * This method provides a list of all projects. The provided parameters are
	 * used for paging.
	 *
	 * @param offset the number where to start
	 * @param size   the size of items to provide
	 * @return       a list of projects &lt;= size (window within offset
	 *               and size)
	 */
	@GET
	public List<ProjectPojo> get(
			@QueryParam("language") String language,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		return new ProjectDao(
				null,
				OpenInfraSchemas.META_DATA).getMainProjects(
						PtLocaleDao.forLanguageTag(language));
	}

	@GET
	@Path("count")
	@Produces({MediaType.TEXT_PLAIN})
	public long getMainProjectsCount() {
		return new ProjectDao(
				null,
				OpenInfraSchemas.META_DATA).getMainProjectsCount();
	}

	/**
	 * This method provides a specific project.
	 *
	 * @param projectId the id of the requested project
	 * @return          the requested project
	 */
	@GET
	@Path("{projectId}")
	public ProjectPojo get(
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId) {
		return new ProjectDao(
				projectId,
				OpenInfraSchemas.PROJECTS).read(
						PtLocaleDao.forLanguageTag(language),
						projectId);
	}

	/**
	 * This method delivers a list of sub projects specified by the current
	 * project id in only one hierarchy level.
	 *
	 * @param projectId the id of the current project
	 * @param offset    the number where to start
	 * @param size      the size of items to provide
	 * @return          a list of sub projects
	 */
	@GET
	@Path("{projectId}/subprojects")
	public List<ProjectPojo> getSubProjects(
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		return new ProjectDao(
				projectId,
				OpenInfraSchemas.PROJECTS).readSubProjects(
						PtLocaleDao.forLanguageTag(language),
						offset,
						size);
	}

	@GET
	@Path("{projectId}/subprojects/count")
	@Produces({MediaType.TEXT_PLAIN})
	public long getSubProjectsCount(
			@PathParam("projectId") UUID projectId) {
		return new ProjectDao(
				projectId,
				OpenInfraSchemas.PROJECTS).getSubProjectsCount();
	}

	@GET
	@Path("{projectId}/new")
    public ProjectPojo newSubProject(
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId) {
        return new ProjectDao(
                        projectId,
                        OpenInfraSchemas.PROJECTS)
                    .newSubProject(PtLocaleDao.forLanguageTag(language));
    }

	/**
	 * This method creates a new project.
	 *
	 * You can access this method via curl. An example of the command line could
	 * be as follows:
	 *
	 * curl.exe -X "POST" -H "Content-Type: application/xml"
	 * -d &#64;path/to/file http://localhost:8080/openinfra_backend/projects
	 *
	 * @param project a project object (JSON or XML representations are
	 *                converted into real objects via the JAX-RS stack)
	 * @return        an UUID of the created project
	 */
	@POST
	public Response createProject(ProjectPojo project) {
	    // create the project
		UUID id = ProjectDao.createProject(project);
		// TODO add informations to the meta data schema, this is necessary for
		//      every REST end point this project should use
		return OpenInfraResponseBuilder.postResponse(id);
	}

	/**
	 * Currently, this method should update a project. In contrast to the
	 * definition of the HTTP PUT method, this method updates an existing
	 * resource and doesn't replace  it. This should be discussed/fixed in
	 * the future.
	 *
	 * You can access this method via curl. An example of the command line could
	 * be as follows:
	 *
	 * curl.exe -X "PUT" -H "Content-Type: application/xml" -d
	 * &#64;path/to/file http://localhost:8080/openinfra_backend/projects/{id}
	 *
	 * @param projectId the id of project which should be updated
	 * @return          a response with the specific status
	 */
    @PUT
    @Path("{projectId}")
    public Response updateProject(
    		@PathParam("projectId") UUID projectId,
    		ProjectPojo project) {
    	UUID uuid = new ProjectDao(
    			projectId,
    			OpenInfraSchemas.PROJECTS).createOrUpdate(project);
    	return OpenInfraResponseBuilder.putResponse(uuid);
    }

	/**
	 * This method deletes a project resource.
	 *
	 * You can access this method via curl. An example of the command line could
	 * be as follows:
	 *
	 * curl.exe -I -X "DELETE"
	 * http://localhost:8080/openinfra_backend/projects/{id}
	 *
	 * @param projectId the project which should be deleted
	 * @return          a response with the specific status
	 */
	@DELETE
	@Path("{projectId}")
	public Response deleteProject(
	        @PathParam("projectId") UUID projectId) {
	    // TODO this method will work correctly if the project creation works
	    //      completely
	    System.out.println("not implemented yet");
	    return null;
	    /*
		return OpenInfraResponseBuilder.deleteResponse(
                new ProjectDao(
                        projectId,
                        OpenInfraSchemas.PROJECTS)
                    .deleteProject(),
                projectId);
                */
	}

	@GET
	@Path("{projectId}/parents")
	public List<ProjectPojo> readParents(
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId) {
		return new ProjectDao(
				projectId,
				OpenInfraSchemas.PROJECTS).getParents(
						PtLocaleDao.forLanguageTag(language));
	}

}