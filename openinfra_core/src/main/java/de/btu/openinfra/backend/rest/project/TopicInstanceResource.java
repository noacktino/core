package de.btu.openinfra.backend.rest.project;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import de.btu.openinfra.backend.OpenInfraProperties;
import de.btu.openinfra.backend.db.OpenInfraOrderBy;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.OpenInfraSortOrder;
import de.btu.openinfra.backend.db.daos.PtLocaleDao;
import de.btu.openinfra.backend.db.daos.project.AttributeValueGeomType;
import de.btu.openinfra.backend.db.pojos.TopicCharacteristicPojo;
import de.btu.openinfra.backend.db.pojos.project.AttributeValuePojo;
import de.btu.openinfra.backend.db.pojos.project.TopicInstanceAssociationFromPojo;
import de.btu.openinfra.backend.db.pojos.project.TopicInstanceAssociationToPojo;
import de.btu.openinfra.backend.db.pojos.project.TopicInstancePojo;
import de.btu.openinfra.backend.db.pojos.project.TopicPojo;
import de.btu.openinfra.backend.db.rbac.AttributeValueRbac;
import de.btu.openinfra.backend.db.rbac.OpenInfraHttpMethod;
import de.btu.openinfra.backend.db.rbac.TopicCharacteristicRbac;
import de.btu.openinfra.backend.db.rbac.TopicInstanceAssociationFromRbac;
import de.btu.openinfra.backend.db.rbac.TopicInstanceAssociationToRbac;
import de.btu.openinfra.backend.db.rbac.TopicInstanceRbac;
import de.btu.openinfra.backend.db.rbac.TopicRbac;
import de.btu.openinfra.backend.rest.OpenInfraResponseBuilder;

@Path(OpenInfraResponseBuilder.REST_URI_PROJECTS + "/topicinstances")
@Produces({MediaType.APPLICATION_JSON + OpenInfraResponseBuilder.JSON_PRIORITY
    + OpenInfraResponseBuilder.UTF8_CHARSET,
    MediaType.APPLICATION_XML + OpenInfraResponseBuilder.XML_PRIORITY
    + OpenInfraResponseBuilder.UTF8_CHARSET})
public class TopicInstanceResource {

	@GET
	@Path("count")
	@Produces({MediaType.TEXT_PLAIN})
	public long getTopicInstanceCount(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@PathParam("projectId") UUID projectId) {
		return new TopicInstanceRbac(
				projectId,
				OpenInfraSchemas.PROJECTS).getCount(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo);
	}

	@GET
	@Path("{topicInstanceId}")
	public TopicInstancePojo get(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId) {
		return new TopicInstanceRbac(
				projectId,
				OpenInfraSchemas.PROJECTS).read(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
						PtLocaleDao.forLanguageTag(language),
						topicInstanceId);
	}

	@POST
    public Response create(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId,
            TopicInstancePojo pojo) {
        UUID id = new TopicInstanceRbac(projectId,
                OpenInfraSchemas.PROJECTS).createOrUpdate(
                OpenInfraHttpMethod.valueOf(request.getMethod()),
                uriInfo,
                null,
                pojo);
        return OpenInfraResponseBuilder.postResponse(id);
    }

	@PUT
    @Path("{topicInstanceId}")
    public Response update(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            TopicInstancePojo pojo) {
	    return OpenInfraResponseBuilder.putResponse(
	            new TopicInstanceRbac(
	                    projectId,
	                    OpenInfraSchemas.PROJECTS).createOrUpdate(
	                            OpenInfraHttpMethod.valueOf(
                                        request.getMethod()),
	                            uriInfo,
	                            topicInstanceId,
	                            pojo));
    }

	@DELETE
	@Path("{topicInstanceId}")
	public Response delete(
	        @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId) {
	    boolean deleteResult =
	            new TopicInstanceRbac(
                    projectId,
                    OpenInfraSchemas.PROJECTS).delete(
                            OpenInfraHttpMethod.valueOf(
                                    request.getMethod()),
                            uriInfo,
                            topicInstanceId);
	    return OpenInfraResponseBuilder.deleteResponse(
	            deleteResult,
	            topicInstanceId);
	}

	@GET
	@Path("{topicInstanceId}/associationsto")
	public List<TopicInstanceAssociationToPojo> getAssociations(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

		return new TopicInstanceAssociationToRbac(
				projectId,
				OpenInfraSchemas.PROJECTS).read(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
						PtLocaleDao.forLanguageTag(language),
						topicInstanceId,
						offset,
						size);
	}

	@POST
    @Path("{topicInstanceId}/associationsto")
	public Response create(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            TopicInstanceAssociationToPojo pojo) {
	    return OpenInfraResponseBuilder.postResponse(
                new TopicInstanceAssociationToRbac(
                        projectId,
                        OpenInfraSchemas.PROJECTS).createOrUpdate(
                                OpenInfraHttpMethod.valueOf(
                                        request.getMethod()),
                                uriInfo,
                                pojo,
                                topicInstanceId,
                                pojo.getAssociationInstanceId(),
                                null,
                                null));
    }

	@GET
	@Path("{topicInstanceId}/associationsto/topiccharacteristics")
	public List<TopicCharacteristicPojo> getTopicCharacteristicsTo(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

		return new TopicCharacteristicRbac(
				projectId, OpenInfraSchemas.PROJECTS)
		.readByTopicInstanceAssociationTo(
				OpenInfraHttpMethod.valueOf(request.getMethod()),
				uriInfo, PtLocaleDao.forLanguageTag(language), topicInstanceId,
				offset, size);
	}

	@GET
	@Path("{topicInstanceId}/associationsto/topiccharacteristics/{topCharId}")
	public List<TopicInstanceAssociationToPojo> getAssociationsToByTopchar(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId,
			@PathParam("topCharId") UUID topCharId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size,
			@QueryParam("sortOrder") OpenInfraSortOrder sortOrder,
            @QueryParam("orderBy") OpenInfraOrderBy orderBy) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

		return new TopicInstanceAssociationToRbac(
				projectId, OpenInfraSchemas.PROJECTS)
			.readAssociationToByTopchar(OpenInfraHttpMethod.valueOf(
					request.getMethod()), uriInfo,
					PtLocaleDao.forLanguageTag(language),
					topicInstanceId, topCharId, offset, size, sortOrder,
					orderBy);
	}

	@GET
	@Path("{topicInstanceId}/associationsfrom/topiccharacteristics/{topCharId}")
	public List<TopicInstanceAssociationFromPojo> getAssociationsFromByTopchar(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId,
			@PathParam("topCharId") UUID topCharId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

		return new TopicInstanceAssociationFromRbac(
				projectId, OpenInfraSchemas.PROJECTS)
			.readAssociationFromByTopchar(OpenInfraHttpMethod.valueOf(
					request.getMethod()), uriInfo,
					PtLocaleDao.forLanguageTag(language),
					topicInstanceId, topCharId, offset, size);
	}

	@GET
	@Path("{topicInstanceId}/associationsfrom/topiccharacteristics")
	public List<TopicCharacteristicPojo> getTopicCharacteristicsFrom(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

		return new TopicCharacteristicRbac(
				projectId, OpenInfraSchemas.PROJECTS)
		.readByTopicInstanceAssociationFrom(
				OpenInfraHttpMethod.valueOf(request.getMethod()),
				uriInfo, PtLocaleDao.forLanguageTag(language), topicInstanceId,
				offset, size);
	}


	@GET
	@Path("{topicInstanceId}/associationsto/count")
	@Produces({MediaType.TEXT_PLAIN})
	public long getTopicInstanceAssociationCount(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId")
				UUID topicInstanceId) {
		return new TopicInstanceAssociationToRbac(
				projectId,
				OpenInfraSchemas.PROJECTS).getCount(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
						topicInstanceId);
	}

	@GET
	@Path("{topicInstanceId}/associationsto/{associatedTopicInstanceId}")
	public List<TopicInstanceAssociationToPojo> getAssociations(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@QueryParam("language") String language,
			@PathParam("projectId") UUID projectId,
			@PathParam("topicInstanceId") UUID topicInstanceId,
			@PathParam("associatedTopicInstanceId")
				UUID associatedTopicInstanceId,
			@QueryParam("offset") int offset,
			@QueryParam("size") int size) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

		return new TopicInstanceAssociationToRbac(
				projectId,
				OpenInfraSchemas.PROJECTS).read(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
						PtLocaleDao.forLanguageTag(language),
						topicInstanceId,
						associatedTopicInstanceId, offset, size);
	}

	@PUT
    @Path("{topicInstanceId}/associationsto/{associatedTopicInstanceId}")
	public Response update(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            @PathParam("associatedTopicInstanceId")
                UUID associatedTopicInstanceId,
            TopicInstanceAssociationToPojo pojo) {
	    return OpenInfraResponseBuilder.putResponse(
                new TopicInstanceAssociationToRbac(
                        projectId,
                        OpenInfraSchemas.PROJECTS).createOrUpdate(
                                OpenInfraHttpMethod.valueOf(
                                        request.getMethod()),
                                uriInfo,
                                pojo,
                                topicInstanceId,
                                pojo.getAssociationInstanceId(),
                                associatedTopicInstanceId,
                                pojo.getAssociatedInstance().getUuid()));
    }

	@DELETE
    @Path("{topicInstanceId}/associationsto/{associatedTopicInstanceId}")
    public Response delete(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            @PathParam("associatedTopicInstanceId")
                UUID associatedTopicInstanceId) {
	    return OpenInfraResponseBuilder.deleteResponse(
                new TopicInstanceAssociationToRbac(
                        projectId,
                        OpenInfraSchemas.PROJECTS).delete(
                                OpenInfraHttpMethod.valueOf(
                                      request.getMethod()),
                                uriInfo,
                                topicInstanceId,
                                associatedTopicInstanceId));
	}

	@GET
    @Path("/{topicInstanceId}/attributetypes/{attributeTypeId}/attributevalues"
            + "/new")
    public AttributeValuePojo newAttributeValue(
    		@Context UriInfo uriInfo,
    		@Context HttpServletRequest request,
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            @PathParam("attributeTypeId") UUID attributeTypeId,
            @QueryParam("geomType") AttributeValueGeomType geomType) {
        return new AttributeValueRbac(
                projectId,
                OpenInfraSchemas.PROJECTS).newAttributeValue(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
                        topicInstanceId,
                        attributeTypeId,
                        PtLocaleDao.forLanguageTag(language));
    }

	@GET
    @Path("/{topicInstanceId}/attributetypes/{attributeTypeId}/attributevalues")
    public List<AttributeValuePojo> getAttributeValues(
    		@Context UriInfo uriInfo,
    		@Context HttpServletRequest request,
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            @PathParam("attributeTypeId") UUID attributeTypeId,
            @QueryParam("offset") int offset,
            @QueryParam("size") int size) {
		// Define the specific parameters when not specified correctly
		if(size == 0) {
			offset = OpenInfraProperties.DEFAULT_OFFSET;
			size = OpenInfraProperties.DEFAULT_SIZE;
		} // end if

        return new AttributeValueRbac(
                projectId,
                OpenInfraSchemas.PROJECTS).read(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
                        PtLocaleDao.forLanguageTag(language),
                        topicInstanceId,
                        attributeTypeId,
                        offset,
                        size);
    }

	@GET
    @Path("{topicInstanceId}/topic")
    public TopicPojo get(
    		@Context UriInfo uriInfo,
    		@Context HttpServletRequest request,
            @QueryParam("language") String language,
            @PathParam("projectId") UUID projectId,
            @PathParam("topicInstanceId") UUID topicInstanceId,
            @QueryParam("geomType") AttributeValueGeomType geomType) {
        return new TopicRbac(
                projectId,
                OpenInfraSchemas.PROJECTS).read(
						OpenInfraHttpMethod.valueOf(request.getMethod()),
						uriInfo,
                        PtLocaleDao.forLanguageTag(language),
                        topicInstanceId,
                        geomType);
    }
}
