package de.btu.openinfra.backend.rest.view.file;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Template;

import de.btu.openinfra.backend.db.pojos.file.FilePojo;
import de.btu.openinfra.backend.rest.OpenInfraResponseBuilder;

@Path("/v1/files")
@Produces(MediaType.TEXT_HTML +
		OpenInfraResponseBuilder.UTF8_CHARSET +
		OpenInfraResponseBuilder.HTML_PRIORITY)
public class FileResource {

	@GET
	@Template(name="/views/list/Files.jsp")
	@Produces(MediaType.TEXT_HTML +
			OpenInfraResponseBuilder.UTF8_CHARSET +
			OpenInfraResponseBuilder.HTML_PRIORITY)
    public List<FilePojo> files(
    		@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
        return new de.btu.openinfra.backend.rest.file.FileResource()
        .files(uriInfo, request, language, offset, size);
    }


	@GET
	@Path("upload")
	@Template(name="/views/Upload.jsp")
	@Produces(MediaType.TEXT_HTML +
			OpenInfraResponseBuilder.UTF8_CHARSET +
			OpenInfraResponseBuilder.HTML_PRIORITY)
    public Response uploadFiles() {
        return Response.ok().entity("file upload").build();
    }

}