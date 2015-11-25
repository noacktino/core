package de.btu.openinfra.backend.db.daos;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.jpa.model.ValueList;
import de.btu.openinfra.backend.db.jpa.model.ValueListXValueList;
import de.btu.openinfra.backend.db.pojos.ValueListAssociationPojo;
import de.btu.openinfra.backend.exception.OpenInfraEntityException;
import de.btu.openinfra.backend.exception.OpenInfraExceptionTypes;

/**
 * This class represents the ValueListAssociation and is used to access the
 * underlying layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class ValueListAssociationDao
	extends OpenInfraValueValueDao<ValueListAssociationPojo,
	ValueListXValueList, ValueList, ValueList> {

    /**
     * This is the required constructor which calls the super constructor and in
     * turn creates the corresponding entity manager.
     *
     * @param currentProjectId the current project id (this should be null when
     *                         the system schema is selected)
     * @param schema           the required schema
     */
	public ValueListAssociationDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId, schema, ValueListXValueList.class,
				ValueList.class, ValueList.class);
	}

	@Override
	public ValueListAssociationPojo mapToPojo(
			Locale locale,
			ValueListXValueList vlxvl) {
		return mapToPojoStatically(locale, vlxvl);
	}

	@Override
	public MappingResult<ValueListXValueList> mapToModel(
			ValueListAssociationPojo pojo,
			ValueListXValueList vlxvl) {
        // TODO set the model values

        // return the model as mapping result
        return new MappingResult<ValueListXValueList>(vlxvl.getId(), vlxvl);
	}

	/**
     * This method implements the method mapToPojo in a static way.
     *
     * @param locale the requested language as Java.util locale
     * @param vlxvl  the model object
     * @return       the POJO object when the model object is not null else null
     */
	public static ValueListAssociationPojo mapToPojoStatically(
			Locale locale,
			ValueListXValueList vlxvl) {

		try {
			ValueListAssociationPojo pojo =
					new ValueListAssociationPojo(vlxvl);
			pojo.setAssociationValueListId(vlxvl.getValueList1Bean().getId());
			pojo.setRelationship(ValueListValueDao.mapToPojoStatically(locale,
					vlxvl.getValueListValue()));
			pojo.setAssociatedValueList(ValueListDao.mapToPojoStatically(locale,
					vlxvl.getValueList2Bean()));

			return pojo;
		} catch (NullPointerException npe) {
            throw new OpenInfraEntityException(
                    OpenInfraExceptionTypes.MISSING_DATA_IN_POJO);
        }
	}
}
