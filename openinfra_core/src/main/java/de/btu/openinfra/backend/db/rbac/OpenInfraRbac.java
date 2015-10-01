package de.btu.openinfra.backend.db.rbac;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;

import de.btu.openinfra.backend.db.OpenInfraOrderByEnum;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.OpenInfraSortOrder;
import de.btu.openinfra.backend.db.daos.OpenInfraDao;
import de.btu.openinfra.backend.db.daos.TopicInstanceDao;
import de.btu.openinfra.backend.db.jpa.model.OpenInfraModelObject;
import de.btu.openinfra.backend.db.pojos.OpenInfraPojo;

/**
 * This class implements the role-based access control layer. Methods of this
 * class are called by rest classes and the call passes a user (subject) 
 * permissions check. When the permissions check is successful, the underlying
 * DAO class is called.
 * 
 * @see OpenInfraDao
 * 
 * Permission examples:
 *  
 * Root permission (access to all rest resources with read, write, update and
 * delete):
 * *:*:*
 *  
 * Permission to access all projects with read and write (update and delete)
 * permission:
 * /projects/{id}:*:* or /projects/{id}:r,w:* 
 *  
 * Permission to read all available projects:
 * /projects/{id}:r:*
 *  
 * Permission to read only one project:
 * /projects/{id}:r:e7d42bff-4e40-4f43-9d1b-1dc5a190cd75
 *  
 * Permission to read two projects:
 * /projects/{id}:r:e7d42bff-4e40-4f43-9d1b-1dc5a190cd75,fd27a347-4e33-4ed7-aebc-eeff6dbf1054
 * 
 * These permissions can be extended to secure each resource in detail. For 
 * example you want to secure the following url:
 * /projects/{id}/topiccharacteristics/{id}/topicinstances
 * Insert the currentProjectId like so
 * /projects/e7d42bff-4e40-4f43-9d1b-1dc5a190cd75/topiccharacteristics/{id}/topicinstances:r:{id}
 * 
 * Permission to read information from system schema:
 * /system:r
 * 
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 * @param <TypePojo>
 * @param <TypeModel>
 * @param <TypeDao>
 */
public abstract class OpenInfraRbac<
	TypePojo extends OpenInfraPojo,
	TypeModel extends OpenInfraModelObject,
	TypeDao extends OpenInfraDao<TypePojo, TypeModel>> {
	
	/**
	 * The UUID of the current project required for creating the entity manager.
	 */
	protected UUID currentProjectId;
	
	/**
	 * The referring DAO class.
	 */
	protected Class<TypeDao> dao;

	/**
	 * The currently used schema.
	 */
	protected OpenInfraSchemas schema;
	
	/**
	 * The current user.
	 */
	protected Subject user;
	
	private String v = "v\\d(\\d)?";
	private String p = "/projects/";
	private String tc = "topiccharacteristics/";
	private String ti = "topicinstances/";
	private String regex_project = v + p +
			"[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}.{1,}";
	private String regex_project_admin = v + p +
			"[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}";
	private String regex_project_topic_characteristic = v + p
					+ "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}/" + tc
					+ "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}.{0,}";
	private String regex_project_topic_instance = v + p
			+ "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}/" + ti
			+ "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}.{0,}";
	
	/**
	 * This defines the constructor types in order to call the constructor in a 
	 * generic way via: 
	 * getDeclaredConstructor(constructorTypes).newInstance(constructorTypes)
	 */
	protected Class<?>[] constructorTypes =	
			new Class[] {UUID.class, OpenInfraSchemas.class};
	
	/**
	 * This is the default constructor method which is used to identify the
	 * current subject and to manage the information required to create the
	 * underlying DAO objects.
	 * 
	 * @param currentProjectId the project which is currently accessed
	 * @param schema           the schema which is currently accessed
	 * @param dao              the related DAO class
	 */
	protected OpenInfraRbac(
			UUID currentProjectId,
			OpenInfraSchemas schema,
			Class<TypeDao> dao) {
		this.currentProjectId = currentProjectId;
		this.schema = schema;
		this.dao = dao;
		this.user = SecurityUtils.getSubject();
	}
	
	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @param locale
	 * @param offset
	 * @param size
	 * @return
	 */
	public List<TypePojo> read(
			OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, Locale locale, int offset, int size) {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).read(locale, offset, size);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @param locale
	 * @param order
	 * @param column
	 * @param offset
	 * @param size
	 * @return
	 */
	public List<TypePojo> read(
			OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo,
    		Locale locale,
    		OpenInfraSortOrder order,
    		OpenInfraOrderByEnum column,
    		int offset,
    		int size) {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).read(locale, order, column, offset, size);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @param locale
	 * @param id
	 * @return
	 */
	public TypePojo read(
			OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo,
			Locale locale, 
			UUID id) {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).read(locale, id);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @return
	 */
	public long getCount(OpenInfraHttpMethod httpMethod, UriInfo uriInfo) {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).getCount();
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @param pojo
	 * @return
	 * @throws RuntimeException
	 */
	public UUID createOrUpdate(
			OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo,
			TypePojo pojo)
			throws RuntimeException {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).createOrUpdate(pojo);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @param pojo
	 * @param valueId
	 * @return
	 * @throws RuntimeException
	 */
    public UUID createOrUpdate(
    		OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, 
			TypePojo pojo, 
			JSONObject json)
            throws RuntimeException {
    	checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).createOrUpdate(pojo, json);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
    }
    
	/**
	 * This is a generic method which is provided by all RBAC classes.
	 * 
	 * @param pojo
	 * @param valueId
	 * @return
	 * @throws RuntimeException
	 */
    public UUID createOrUpdate(
    		OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, 
			TypePojo pojo, 
			UUID valueId, 
			JSONObject json)
            throws RuntimeException {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).createOrUpdate(pojo, valueId, json);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
    }
    
    /**
     * This is a generic method which is provided by all RBAC classes.
     * 
     * @param pojo
     * @param firstAssociationId
     * @param firstAssociationIdFromPojo
     * @return
     * @throws RuntimeException
     */
    public UUID createOrUpdate(
    		OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, 
			TypePojo pojo, 
			UUID firstAssociationId,
            UUID firstAssociationIdFromPojo, JSONObject json) 
            		throws RuntimeException {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).createOrUpdate(pojo, 
							firstAssociationId, firstAssociationIdFromPojo, 
							json);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
    }
    
    /**
     * This is a generic method which is provided by all RBAC classes.
     * 
     * @param pojo
     * @param firstAssociationId
     * @param firstAssociationIdFromPojo
     * @param secondAssociationId
     * @param secondAssociationIdFromPojo
     * @return
     * @throws RuntimeException
     */
    public UUID createOrUpdate(
    		OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, TypePojo pojo, UUID firstAssociationId,
            UUID firstAssociationIdFromPojo, UUID secondAssociationId,
            UUID secondAssociationIdFromPojo, JSONObject json)
            throws RuntimeException {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).createOrUpdate(pojo, firstAssociationId, 
							firstAssociationIdFromPojo, secondAssociationId, 
							secondAssociationIdFromPojo, json);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
    }
    
    /**
     * This is a generic method which is provided by all RBAC classes.
     * 
     * @param uuid
     * @return
     */
    public boolean delete(
    		OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, 
			UUID uuid) {
		checkPermission(httpMethod, uriInfo);
		try {
			return dao.getDeclaredConstructor(constructorTypes).newInstance(
					currentProjectId, 
					schema).delete(uuid);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(
					ex.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		}
    }
	
    /**
     * This method is used to check the permissions of the current subject. This
	 * class throws a WebApplicationException when the current user is either 
	 * not authenticated or unauthorized to access the requested resource. This
	 * WebApplicationException is automatically handled by the embedding web 
	 * container (it might be Apache Tomcat).
     * 
     * @param httpMethod               the method which has been used to access
     *                                 the current resource
     * @param uriInfo                  the URI information
     * @throws WebApplicationException when the subject (user) is not allowed to
     *                                 access the requested resource
     */
	protected void checkPermission(
			OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo) throws WebApplicationException {
		
		if(!user.isAuthenticated()) {
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		
		switch (schema) {
		case PROJECTS:
			// Is the current URI path really a project path?
			if(uriInfo.getPath().matches(regex_project)) {
				// Is the subject (user) permitted to read or write the project?
				if(currentProjectId != null && user.isPermitted(
						"/projects/{id}:" + httpMethod.getAccess() + ":" +
								currentProjectId)) {
					// Is the current URI path a topic characteristic or a
					// topic instance? If not, the subject (user) is permitted
					// in any case since it has access to the project.
					if(uriInfo.getPath().matches(
							regex_project_topic_characteristic)) {
						// Get the topic characteristic UUID from path 
						String tcId = uriInfo.getPath().substring(
								uriInfo.getPath().lastIndexOf(tc) + 
								tc.length());
						tcId = tcId.substring(0, tcId.indexOf("/"));
						// Generate the required access string
						String req_access = "/projects/" + currentProjectId +
								"/topiccharacteristics/{id}:" +
								httpMethod.getAccess() + ":" + tcId;
						if(user.isPermitted(req_access)) {
							return;
						}
					} else if(uriInfo.getPath().matches(
							regex_project_topic_instance)) {
						// Get the topic instance UUID from path 
						String tiId = uriInfo.getPath().substring(
								uriInfo.getPath().lastIndexOf(ti) + 
								ti.length());
						tiId = tiId.substring(0, tiId.indexOf("/"));
						// Get the responding topic characteristic UUID from DB
						UUID tcId = new TopicInstanceDao(
								currentProjectId, schema).read(
										null, UUID.fromString(tiId))
										.getTopicCharacteristic().getUuid();
						// Generate the required access string
						String req_access = "/projects/" + currentProjectId +
								"/topiccharacteristics/{id}:" +
								httpMethod.getAccess() + ":" + tcId;
						if(user.isPermitted(req_access)) {
							return;
						}						
					} else {
						return;
					} // end else if
				}		
			} // end if regex_project
			if(uriInfo.getPath().matches(regex_project_admin)) {
				if(currentProjectId != null && user.isPermitted(
						"/projects/" + currentProjectId + ":" + 
								httpMethod.getAccess())) {
					return;
				}
			}
			break;

		case META_DATA:
			break;
			
		case RBAC:
			if(user.isPermitted("/rbac:" + httpMethod.getAccess())) {
				return;
			}
			break;
			
		case SYSTEM:
			if(user.isPermitted("/system:" + httpMethod.getAccess())) {
				return;
			}
			break;
		}
		
		throw new WebApplicationException(Response.Status.FORBIDDEN);
		
	}

}