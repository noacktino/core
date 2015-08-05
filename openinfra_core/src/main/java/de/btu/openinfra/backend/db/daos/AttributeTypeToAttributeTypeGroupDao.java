package de.btu.openinfra.backend.db.daos;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.jpa.model.AttributeType;
import de.btu.openinfra.backend.db.jpa.model.AttributeTypeGroup;
import de.btu.openinfra.backend.db.jpa.model.AttributeTypeToAttributeTypeGroup;
import de.btu.openinfra.backend.db.pojos.AttributeTypeToAttributeTypeGroupPojo;

/**
 * This class represents the AttributeTypeToAttributeTypeGroup and is used to
 * access the underlying layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class AttributeTypeToAttributeTypeGroupDao extends
	OpenInfraValueValueDao<AttributeTypeToAttributeTypeGroupPojo,
	AttributeTypeToAttributeTypeGroup, AttributeTypeGroup, AttributeType> {

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public AttributeTypeToAttributeTypeGroupDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId,
			  schema,
			  AttributeTypeToAttributeTypeGroup.class,
			  AttributeTypeGroup.class,
			  AttributeType.class);
	}

	@Override
	public AttributeTypeToAttributeTypeGroupPojo mapToPojo(
			Locale locale,
			AttributeTypeToAttributeTypeGroup modelObject) {
		return mapToPojoStatically(locale, modelObject);
	}

	/**
	 * This method implements the method mapToPojo in a static way.
	 *
	 * @param locale the requested language as Java.util locale
	 * @param atg    the model object
	 * @return       the POJO object when the model object is not null else null
	 */
	public static AttributeTypeToAttributeTypeGroupPojo mapToPojoStatically(
			Locale locale,
			AttributeTypeToAttributeTypeGroup atg) {
		AttributeTypeToAttributeTypeGroupPojo pojo =
				new AttributeTypeToAttributeTypeGroupPojo();
		pojo.setAttributeType(AttributeTypeDao.mapToPojoStatically(
				locale,
				atg.getAttributeType()));
		pojo.setAttributeTypeGroupId(atg.getId());
		pojo.setDefaultValue(ValueListValueDao.mapToPojoStatically(
				locale,
				atg.getValueListValue()));
		pojo.setMultiplicity(MultiplicityDao.mapToPojoStatically(
				atg.getMultiplicityBean()));
		pojo.setOrder(atg.getOrder());
		pojo.setUuid(atg.getId());
		return pojo;
	}

	@Override
	public MappingResult<AttributeTypeToAttributeTypeGroup> mapToModel(
			AttributeTypeToAttributeTypeGroupPojo pojo,
			AttributeTypeToAttributeTypeGroup at) {

        // TODO set the model values

        // return the model as mapping result
        return new MappingResult<AttributeTypeToAttributeTypeGroup>(
                at.getId(), at);
	}

}
