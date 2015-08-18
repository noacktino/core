package de.btu.openinfra.backend.db.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.eclipse.persistence.jpa.JpaQuery;

import de.btu.openinfra.backend.OpenInfraProperties;
import de.btu.openinfra.backend.db.EntityManagerFactoryCache;
import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraOrderByEnum;
import de.btu.openinfra.backend.db.OpenInfraSortOrder;
import de.btu.openinfra.backend.db.jpa.model.OpenInfraModelObject;
import de.btu.openinfra.backend.db.jpa.model.PtLocale;
import de.btu.openinfra.backend.db.pojos.AttributeValueGeomPojo;
import de.btu.openinfra.backend.db.pojos.AttributeValueGeomzPojo;
import de.btu.openinfra.backend.db.pojos.OpenInfraPojo;

/**
 * This class is used to provide a sophisticated way to manage the JPA entity
 * manager.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 * @param <TypePojo>  defines the referring POJO class of the DAO class
 * @param <TypeModel> defines the referring JPA model class of the DAO class
 */
public abstract class OpenInfraDao<TypePojo extends OpenInfraPojo,
	TypeModel extends OpenInfraModelObject> {

	/**
	 * The JPA entity manager used to access the project or the system database.
	 */
	protected EntityManager em;

	/**
	 * The UUID of the current project required for creating the entity manager.
	 */
	protected UUID currentProjectId;

	/**
	 * This variable defines the underlying JPA model class.
	 */
	protected Class<TypeModel> modelClass;

	/**
	 * The currently used schema.
	 */
	protected OpenInfraSchemas schema;

	/**
	 * This method represents the super constructor in order to create an entity
	 * manager for each data access object (DAO) in a sophisticated way. We
	 * decided to use an application managed entity manager in order to have
	 * more control about its properties and its life cycle. Each DAO provides
	 * its own entity manager and the lifetime of the entity manager depends on
	 * the lifetime of the DAO object. The entity manager is automatically
	 * closed by the finalize method. Thus, don't call the close method on the
	 * entity manager otherwise you'll get an exception if you try to access the
	 * entity manager again.
	 *
	 * This method sets the properties of the connection dynamically. If you
	 * would like to change the connection, this should be the first class to
	 * look at. The connection strings should be managed by a configuration
	 * (properties) file or by a database.
	 *
	 * The super constructor will use the current project id in order to access
	 * the meta database and to retrieve the credentials of the currently used
	 * project database. If the current project id is set to null and the
	 * corresponding schema is specified, the super constructor will provide
	 * access to the system database.
	 *
	 * In order to change the schema value you have to consider query parameters
	 * of the connection URL like so:
	 * "jdbc:postgresql://localhost:5432/openinfra?currentSchema=project".
	 * By considering this, you'll be able to modify the required schema easily,
	 * nicely and dynamically during runtime.
	 *
	 * TODO in order to add a User Rights Management system add specific
	 * functions here
	 *
	 * @param currentProjectId The identifier of the current project.
	 * @param schema           This parameter defines the schema.
	 * @param modelClass       The underlying JPA model class.
	 */
	protected OpenInfraDao(
			UUID currentProjectId,
			OpenInfraSchemas schema,
			Class<TypeModel> modelClass) {
		// 1. Set local parameters
		this.currentProjectId = currentProjectId;
		this.schema = schema;
		this.modelClass = modelClass;

		// 2. Create the final entity manager
		em = EntityManagerFactoryCache.getEntityManagerFactory(
		        currentProjectId,
		        schema).createEntityManager();
	}

	/**
	 * This is the default generic method which provides read access to the
	 * selected database schema without sorting. It is almost the same routine
	 * for all DAO classes to access the database.
	 *
     * @param locale     A Java.util locale objects.
	 * @param offset     the number where to start
	 * @param size       the size of items to provide
	 * @return           a list of objects of type POJO class
	 */
    public List<TypePojo> read(Locale locale, int offset, int size) {
		// 1. Define a map which holds the POJO objects
		List<TypePojo> pojos = new LinkedList<TypePojo>();
		// 2. Define a list of model objects
		List<TypeModel> models = null;
		// 3. Define the named query
	    String namedQuery = modelClass.getSimpleName() + ".findAll";
	    // 4. Retrieve the requested model objects from database
	    models = em.createNamedQuery(
	                namedQuery,
	                modelClass)
	                .setFirstResult(offset)
	                .setMaxResults(size)
	                .getResultList();
		// 5. Finally, map the JPA model objects to POJO objects
		for(TypeModel modelItem : models) {
		    pojos.add(mapToPojo(locale, modelItem));
		} // end for
		return pojos;
	}

	/**
	 * This is a generic method which provides read access to the selected
	 * database schema. It is almost the same routine for all DAO classes to
	 * access the database. If not, this method should be extended in order
	 * to avoid overrides. Overrides could increase the effort regarding the
	 * integration and maintenance of a rights management system.
	 *
     * Since the system schema doesn't provide the project_id column for topic
     * characteristic objects it is necessary to handle this request separately.
     *
     * The meta data schema is also handled separately.
	 *
	 *
     * @param locale     A Java.util locale objects.
	 * @param order      the sort order (ascending or descending)
	 * @param column     the column to sort
	 * @param offset     the number where to start
	 * @param size       the size of items to provide
	 * @return           a list of objects of type POJO class
	 */
	@SuppressWarnings("unchecked")
    public List<TypePojo> read(
    		Locale locale,
    		OpenInfraSortOrder order,
    		OpenInfraOrderByEnum column,
    		int offset,
    		int size) {
		// 1. Define a list which holds the POJO objects
		List<TypePojo> pojos = new LinkedList<TypePojo>();
		// 2. Define a list of model objects
		List<TypeModel> models = null;
		// 3. Use the default values for language and order when null.
		if(locale == null) {
			locale = OpenInfraProperties.DEFAULT_LANGUAGE;
		}
		if(order == null) {
			order = OpenInfraProperties.DEFAULT_ORDER;
		}
		// 4. Read the required ptlocale object
		PtLocale ptl = new PtLocaleDao(currentProjectId, schema).read(locale);
		if(column == null) {
			// 5.a When the column is null redirect to another method
			return read(locale, offset, size);
		} else {
	        // 5.a Construct the origin SQL-based named query and replace the
			//     the placeholder by the required column and sort order.
	        String sqlString = em.createNamedQuery(
	        		modelClass.getSimpleName() + ".findAllByLocaleAndOrder")
	        		.unwrap(JpaQuery.class).getDatabaseQuery().getSQLString();
	        sqlString = String.format(sqlString, column.name());
	        sqlString += " " + order.name();
	        // 5.b Retrieve the requested model objects from database
	        models = em.createNativeQuery(
	        		sqlString,
	                modelClass)
	                .setParameter(1, ptl.getId().toString())
	                .setFirstResult(offset)
	                .setMaxResults(size)
	                .getResultList();
		} // end if else

		// 6. Finally, map the JPA model objects to POJO objects
		for(TypeModel modelItem : models) {
		    pojos.add(mapToPojo(locale, modelItem));
		} // end for
		return pojos;
	}

	/**
	 * A special method which returns model objects. This method is primarily
	 * used to create a search index.
	 *
	 * @return a list of all model objects
	 */
	public List<TypeModel> read() {
		String namedQuery = modelClass.getSimpleName() + ".findAll";
		return em.createNamedQuery(
				namedQuery,
				modelClass).getResultList();
	}

	/**
	 * This function creates a new or updates an existing object in the
	 * database. The update function depends on the specified language.
	 *
	 * @param pojo   the POJO object which should be stored in the database
	 * @return       the UUID of the newly created or replaced object
	 * @throws RuntimeException
	 */
	public UUID createOrUpdate(TypePojo pojo)
			throws RuntimeException {

	    UUID resultId = null;

		// 2. Get the transaction and merge (create or replace) the JPA model
		// object.
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();			
			switch(modelClass.getSimpleName()) {
			    // special handling for geometry classes
			    case "AttributeValueGeom":
			        // fall through
			    case "AttributeValueGeomz":
			        Query geomQuery = createGeomQuery(pojo);
	                if (geomQuery != null) {
	                    // execute the query
	                    geomQuery.executeUpdate();
	                }
	                break;
			    default:
			        TypeModel model = createModelObject(pojo.getUuid());
			        // abort if the pojo and the type model is null
			        if (pojo == null || model == null) {
			            return null;
			        }

			        // 1. Map the POJO object to a JPA model object
			        MappingResult<TypeModel> result = mapToModel(pojo, model);

			        // abort here if the result is null
			        if (result == null) {
			            return null;
			        }
			        em.merge(result.getModelObject());
			        resultId = result.getId();
			}
			et.commit();
			return resultId;
		} catch(RuntimeException ex) {
			if(et != null && et.isActive()) {
				et.rollback();
			} // end if
			throw ex;
		} // end try catch
    }

	/**
	 * This special method will create a geometry query for creating or
	 * updating. Depending on the modelClass different queries will be
	 * generated.
	 *
	 * @param pojo the POJO object which should be stored in the database
	 * @return     the query or null if the modelClass is not
	 *             AttributeValueGeom or AttributeValueGeomz
	 */
	private Query createGeomQuery(TypePojo pojo) {
        Query geomQuery = null;

        // handle special cases
        switch (modelClass.getSimpleName()) {
        // create the special query for AttributeValueGeomz
        case "AttributeValueGeomz":
            if (pojo.getUuid() == null) {
                // get the NamedNativeQuery
                String sqlString = em.createNamedQuery(
                        modelClass.getSimpleName() + ".insert")
                        .unwrap(JpaQuery.class).getDatabaseQuery()
                        .getSQLString();
                // format the prepared INSERT statement
                String queryString = String.format(
                        sqlString,
                        // set the PostGIS function for the geomType
                        AttributeValueGeomWriteType.valueOf(
                                ((AttributeValueGeomzPojo) pojo)
                                .getGeomType().toString())
                                .getPsqlFnSignature());
                geomQuery = em.createNativeQuery(queryString);
            } else {
                // TODO: JPA replaces the native query with its own query that
                // leads to a type error: varchar / geometry
                /*
                // get the NamedNativeQuery
                String sqlString = em.createNamedQuery(
                        modelClass.getSimpleName() + ".insert")
                        .unwrap(JpaQuery.class).getDatabaseQuery()
                        .getSQLString();
                // format the prepared UPDATE statement
                String queryString = String.format(
                        sqlString,
                        // set the PostGIS function for the geomType
                        AttributeValueGeomWriteType.valueOf(
                                ((AttributeValueGeomzPojo) pojo)
                                .getGeomType().toString())
                                .getPsqlFnSignature());
                geomQuery = em.createNativeQuery(queryString);
                // set the id of the entry that should be updated
                geomQuery.setParameter(4, pojo.getUuid());
                */
                System.out.println("currently not working");
            }

            // set the attributeTypeToAttributeTypeGroup id
            geomQuery.setParameter(1, ((AttributeValueGeomzPojo) pojo)
                    .getAttributeTypeToAttributeTypeGroupId());
            // set the topic instance id
            geomQuery.setParameter(2, ((AttributeValueGeomzPojo) pojo)
                    .getTopicInstanceId());
            // set the geometry value
            geomQuery.setParameter(3, ((AttributeValueGeomzPojo) pojo)
                    .getGeom());
            break;
        case "AttributeValueGeom":
            if (pojo.getUuid() == null) {
             // get the NamedNativeQuery
                String sqlString = em.createNamedQuery(
                        modelClass.getSimpleName() + ".insert")
                        .unwrap(JpaQuery.class).getDatabaseQuery()
                        .getSQLString();
                // format the prepared INSERT statement
                String queryString = String.format(
                        sqlString,
                        // set the PostGIS function for the geomType
                        AttributeValueGeomWriteType.valueOf(
                                ((AttributeValueGeomPojo) pojo)
                                .getGeomType().toString())
                                .getPsqlFnSignature());
                geomQuery = em.createNativeQuery(queryString);
            } else {
                // TODO: JPA replaces the native query with its own query that
                // leads to a type error: varchar / geometry
                /*
                // get the NamedNativeQuery
                String sqlString = em.createNamedQuery(
                        modelClass.getSimpleName() + ".insert")
                        .unwrap(JpaQuery.class).getDatabaseQuery()
                        .getSQLString();
                // format the prepared UPDATE statement
                String queryString = String.format(
                        sqlString
                        // set the PostGIS function for the geomType
                        AttributeValueGeomWriteType.valueOf(
                                ((AttributeValueGeomPojo) pojo)
                                .getGeomType().toString())
                                .getPsqlFnSignature());
                geomQuery = em.createNativeQuery(queryString);
                // set the id of the entry that should be updated
                geomQuery.setParameter(4, pojo.getUuid());
                */
                System.out.println("currently not working");
            }

            // set the attributeTypeToAttributeTypeGroup id
            geomQuery.setParameter(1, ((AttributeValueGeomPojo) pojo)
                    .getAttributeTypeToAttributeTypeGroupId());
            // set the topic instance id
            geomQuery.setParameter(2, ((AttributeValueGeomPojo) pojo)
                    .getTopicInstanceId());
            // set the geometry value
            geomQuery.setParameter(3, ((AttributeValueGeomPojo) pojo)
                    .getGeom());
            break;
        default:
            // return null if the modelClass is no geometry, this should never
            // happen
            return null;
        }
        return geomQuery;
	}
	/**
	 * This is a generic method which reads a specific pojo object. We decided
	 * to hide this functionality behind a protected class so that each deriving
	 * class must implement its own read method. This should provide more type
	 * safety.
	 *
     * @param locale             A Java.util locale object.
	 * @param id                 the UUID of the required model class
	 * @return                   The requested POJO object if present otherwise
	 *                           null.
	 */
    public TypePojo read(Locale locale, UUID id) {
		TypePojo pojo = null;
		try {
            TypeModel tm = em.find(modelClass, id);
            if(tm != null) {
                pojo = mapToPojo(locale, tm);
            }
		} catch(Exception ex) {
            ex.printStackTrace();
        } // end try catch
		return pojo;
	}

	/**
	 * This is an abstract method which must be implemented by deriving classes.
	 * It should map a JPA model object into corresponding a POJO object.
	 *
	 * This method is really slow. Thus, don't use this method too often. It's
	 * recommended to provide another static method which should be much faster.
	 *
	 * @param modelObject the JPA model object
	 * @return            a corresponding POJO object
	 */
	public abstract TypePojo mapToPojo(Locale locale, TypeModel modelObject);

	/**
	 * This is an abstract method which must be implemented by deriving classes.
	 * It should map a POJO object into a corresponding JPA model object.
	 *
	 * @param modelObject the pre initialized model object
	 * @param pojoObject  the POJO object
	 * @return            a corresponding JPA model object or null if the pojo
	 *                    object is null
	 */
	public abstract MappingResult<TypeModel> mapToModel(
			TypePojo pojoObject, TypeModel modelObject);

	/**
	 * This method deletes an entity from the database.
	 *
	 * @param uuid the uuid which specifies the entity
	 * @return     true when the entity was deleted false when the uuid doesn't
	 *             exists
	 */
	public boolean delete(UUID uuid) {
		TypeModel o = em.find(modelClass, uuid);
		EntityTransaction et = em.getTransaction();
		if(o != null) {
			try {
				et.begin();
				em.remove(o);
				et.commit();
				return true;
			} catch(RuntimeException ex) {
				if(et != null && et.isActive()) {
					et.rollback();
				} // end if
				throw ex;
			} // end try catch
		} else {
			return false;
		} // end if else
	}

	/**
	 * This method delivers a model object. When the UUID is not null this
	 * method tries to deliver an existing object. When the UUID is null it
	 * creates a new object and generates a random UUID.
	 *
	 * @param id the UUID of the requested object or null
	 * @return   a model object (new or old) or null if the pojo id doesn't
	 *           exists
	 */
	protected TypeModel createModelObject(UUID id) {
		TypeModel tm = null;
		if(id != null) {
			tm = em.find(modelClass, id);
			// TODO check if tm null
		} else {
			try {
				tm = modelClass.newInstance();
				tm.setId(UUID.randomUUID());
			} catch(Exception ex) {
				ex.printStackTrace();
			} // end try catch
		} // end if else
		return tm;
	}

	/**
	 * This method retrieves the count of objects and uses a named query. If the
	 * query doesn't exists, it will throw an exception.
	 *
	 * @return count of objects or zero
	 */
	public long getCount() {
		String namedQuery = modelClass.getSimpleName() + ".count";
		Long count = 0L;
		count = em.createNamedQuery(
            namedQuery,
            Long.class).getSingleResult().longValue();
		return count;
	}

	/**
	 * This method closes the entity manager automatically. Don't close the
	 * entity manager manually. This will cause an exception when you try to
	 * access the entity manager again during life time of the current object
	 * instance.
	 */
	@Override
	protected void finalize() throws Throwable {
		if(em != null) {
			em.close();
		} // end if
		super.finalize();
	}

}
