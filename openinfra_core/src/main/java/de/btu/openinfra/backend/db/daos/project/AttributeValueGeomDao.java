package de.btu.openinfra.backend.db.daos.project;

import java.util.Locale;
import java.util.UUID;

import javax.persistence.Query;

import org.eclipse.persistence.jpa.JpaQuery;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.daos.OpenInfraDao;
import de.btu.openinfra.backend.db.jpa.model.AttributeTypeToAttributeTypeGroup;
import de.btu.openinfra.backend.db.jpa.model.AttributeValueGeom;
import de.btu.openinfra.backend.db.jpa.model.TopicInstance;
import de.btu.openinfra.backend.db.pojos.project.AttributeValueGeomPojo;
import de.btu.openinfra.backend.exception.OpenInfraEntityException;
import de.btu.openinfra.backend.exception.OpenInfraExceptionTypes;

/**
 * This class represents the AttributeValueGeom and is used to access the
 * underlying layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class AttributeValueGeomDao
	extends OpenInfraDao<AttributeValueGeomPojo, AttributeValueGeom> {

	/**
	 * This variable defines the default geometry type. The default type is
	 * used to implement the default read methods {@see OpenInfraDao}.
	 */
	// TODO delete this
	private AttributeValueGeomType defaultGeomType =
			AttributeValueGeomType.TEXT;

    /**
     * This variable defines the name of the data type which is specified in the
     * database.
     */
    public static final String DATA_TYPE_NAME = "geometry(Geometry)";

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public AttributeValueGeomDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId, schema, AttributeValueGeom.class);
	}

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 * @param geomType         the required geom type
	 */
	public AttributeValueGeomDao(
			UUID currentProjectId,
			OpenInfraSchemas schema,
			AttributeValueGeomType geomType) {
		super(currentProjectId, schema, AttributeValueGeom.class);
		if(geomType != null) {
			defaultGeomType = geomType;
		} // end if
	}

	@Override
	public AttributeValueGeomPojo mapToPojo(
			Locale locale,
			AttributeValueGeom avg) {
	    // get the NamedNativeQuery
        String sqlString = em.createNamedQuery(
                AttributeValueGeom.class.getSimpleName() + ".select")
                .unwrap(JpaQuery.class).getDatabaseQuery().getSQLString();
	    // format the SQL statement for retrieving geometry values
		String queryString = String.format(
		        sqlString,
				defaultGeomType.getPsqlFnSignature());

        // add the id parameter to the query
        Query qGeom = em.createNativeQuery(queryString);
        qGeom.setParameter(1, avg.getId());

        AttributeValueGeomPojo pojo = new AttributeValueGeomPojo(avg);

        // set the topic instance id
        pojo.setTopicInstanceId(avg.getTopicInstance().getId());
        // execute the SQL statement and set the geometry value
        pojo.setGeom(qGeom.getResultList().get(0).toString());
        // set the geometry type
        pojo.setGeomType(defaultGeomType);
        // set the attribute type to attribute type id group of the value
        pojo.setAttributeTypeToAttributeTypeGroupId(
                avg.getAttributeTypeToAttributeTypeGroup().getId());

        return pojo;
	}

	@Override
	public MappingResult<AttributeValueGeom> mapToModel(
			AttributeValueGeomPojo pojo,
			AttributeValueGeom avg) {

        try {
            // in case the geometry is an empty string
            if (pojo.getGeom().equals("")) {
                throw new OpenInfraEntityException(
                        OpenInfraExceptionTypes.MISSING_GEOM_IN_POJO);
            }

            // set the textual information
            avg.setGeom(pojo.getGeom());

            // set the attribute type to attribute type group
            avg.setAttributeTypeToAttributeTypeGroup(em.find(
                    AttributeTypeToAttributeTypeGroup.class,
                    pojo.getAttributeTypeToAttributeTypeGroupId()));

            // set the topic instance
            avg.setTopicInstance(
                    em.find(TopicInstance.class, pojo.getTopicInstanceId()));
        } catch (NullPointerException npe) {
            throw new OpenInfraEntityException(
                    OpenInfraExceptionTypes.MISSING_DATA_IN_POJO);
        }

        return new MappingResult<AttributeValueGeom>(avg.getId(), avg);
	}

}
