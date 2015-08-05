package de.btu.openinfra.backend.db.daos;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.jpa.model.AttributeType;
import de.btu.openinfra.backend.db.jpa.model.AttributeTypeXAttributeType;
import de.btu.openinfra.backend.db.jpa.model.ValueListValue;
import de.btu.openinfra.backend.db.pojos.AttributeTypeAssociationPojo;

/**
 * This class represents the AttributeTypeAssociation and is used to access the
 * underlying layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class AttributeTypeAssociationDao
	extends OpenInfraValueValueDao<AttributeTypeAssociationPojo,
	AttributeTypeXAttributeType, AttributeType, AttributeType> {

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public AttributeTypeAssociationDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId, schema, AttributeTypeXAttributeType.class,
				AttributeType.class, AttributeType.class);
	}

	@Override
	public AttributeTypeAssociationPojo mapToPojo(
			Locale locale,
			AttributeTypeXAttributeType association) {
		return mapToPojoStatically(locale, association);
	}

	@Override
	public MappingResult<AttributeTypeXAttributeType> mapToModel(
			AttributeTypeAssociationPojo pojo,
			AttributeTypeXAttributeType atxat) {

	    // return null if the pojo is null
        if (pojo != null) {

            // set the first attribute type
            atxat.setAttributeType1Bean(em.find(
                    AttributeType.class, pojo.getAssociationAttributeTypeId()));

            // set the related attribute type
            atxat.setAttributeType2Bean(em.find(
                    AttributeType.class,
                    pojo.getAssociatedAttributeType().getUuid()));

            // set the relationship
            atxat.setValueListValue(em.find(
                    ValueListValue.class, pojo.getRelationship().getUuid()));

            // return the model as mapping result
            return new MappingResult<AttributeTypeXAttributeType>(
                    atxat.getId(), atxat);
        } else {
            return null;
        }
	}

	/**
	 * This method implements the method mapToPojo in a static way.
	 *
	 * @param locale       the requested language as Java.util locale
	 * @param association  the model object
	 * @return             the POJO object when the model object is not null
	 *                     else null
	 */
	public static AttributeTypeAssociationPojo mapToPojoStatically(
			Locale locale,
			AttributeTypeXAttributeType atxat) {

		if(atxat != null) {
			AttributeTypeAssociationPojo pojo =
					new AttributeTypeAssociationPojo();

			// set the id of the pojo
			pojo.setUuid(atxat.getId());

			// set the relationship type object
			pojo.setRelationship(ValueListValueDao.mapToPojoStatically(locale,
			        atxat.getValueListValue()));

			// set the association attribute type id
			pojo.setAssociationAttributeTypeId(atxat.getAttributeType1Bean()
			        .getId());

			// set the associated attribute type object
			pojo.setAssociatedAttributeType(
				AttributeTypeDao.mapToPojoStatically(locale,
				        atxat.getAttributeType2Bean()));

			return pojo;
		}
		else {
			return null;
		}
	}

	/**
     * This method creates a AttributeTypeAssociationPojo shell that contains
     * informations about the attribute type the association starts from.
     *
     * @param attributeTypeId the attribute type id the association starts from
     * @return                the AttributeTypePojo
     */
    public AttributeTypeAssociationPojo newAttributeTypeAssociation(
            UUID attributeTypeId) {
        // create the return pojo
        AttributeTypeAssociationPojo pojo = new AttributeTypeAssociationPojo();

        //pojo.set

        return pojo;
    }
}
