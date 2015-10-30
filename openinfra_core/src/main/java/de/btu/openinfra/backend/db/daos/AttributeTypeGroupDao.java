package de.btu.openinfra.backend.db.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.jpa.model.AttributeTypeGroup;
import de.btu.openinfra.backend.db.pojos.AttributeTypeGroupPojo;
import de.btu.openinfra.backend.exception.OpenInfraEntityException;
import de.btu.openinfra.backend.exception.OpenInfraExceptionTypes;

/**
 * This class represents the AttributeTypeGroup and is used to access the
 * underlying layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class AttributeTypeGroupDao
	extends OpenInfraDao<AttributeTypeGroupPojo, AttributeTypeGroup> {

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public AttributeTypeGroupDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId, schema, AttributeTypeGroup.class);
	}

	/**
	 * This method delivers a list of AttributeTypeGroupPojo objects (sub
	 * groups) referring to a specific AttributeTypeGroup.
	 *
	 * @param attributeTypeGroupId the id of the belonging AttributeTypeGroup
	 * @return                     a list of AttributeTypeGroupPojo objects
	 */
	public List<AttributeTypeGroupPojo> readSubGroups(
			Locale locale,
			UUID attributeTypeGroupId) {
		// 1. Find the corresponding AttributeTypeGroup. The find method is used
		//    here in order to benefit from the JPA caching mechanisms.
		AttributeTypeGroup atg = em.find(
				AttributeTypeGroup.class,
				attributeTypeGroupId);
		// 2. Map sub groups and return them
		List<AttributeTypeGroupPojo> pojos =
				new LinkedList<AttributeTypeGroupPojo>();

		if(atg != null) {
			for(AttributeTypeGroup g : atg.getAttributeTypeGroups()) {
				pojos.add(mapToPojoStatically(locale, g,
				        new MetaDataDao(currentProjectId, schema)));
			} // end for
		} // end if
		return pojos;
	}

	@Override
	public AttributeTypeGroupPojo mapToPojo(
			Locale locale,
			AttributeTypeGroup atg) {
		return mapToPojoStatically(locale, atg,
		        new MetaDataDao(currentProjectId, schema));
	}

	/**
	 * This method implements the method mapToPojo in a static way.
	 *
	 * @param locale the requested language as Java.util locale
	 * @param atg    the model object
	 * @param mdDao  the meta data DAO
	 * @return       the POJO object when the model object is not null else null
	 */
	public static AttributeTypeGroupPojo mapToPojoStatically(
			Locale locale,
			AttributeTypeGroup atg,
			MetaDataDao mdDao) {
		if(atg != null) {
		    AttributeTypeGroupPojo pojo =
		            new AttributeTypeGroupPojo(atg, mdDao);

			AttributeTypeGroup subgroupOf = atg.getAttributeTypeGroup();
			if(subgroupOf != null) {
				pojo.setSubgroupOf(subgroupOf.getId());
			} // end if

			pojo.setDescriptions(PtFreeTextDao.mapToPojoStatically(
					locale,
					atg.getPtFreeText1()));
			pojo.setNames(PtFreeTextDao.mapToPojoStatically(
					locale,
					atg.getPtFreeText2()));

			return pojo;
		} else {
			return null;
		} // end if else
	}

	@Override
	public MappingResult<AttributeTypeGroup> mapToModel(
			AttributeTypeGroupPojo pojo,
			AttributeTypeGroup atg) {

        PtFreeTextDao ptfDao =
                new PtFreeTextDao(currentProjectId, schema);

        // set the description (optional)
        if (pojo.getDescriptions() != null) {
            atg.setPtFreeText1(
                    ptfDao.getPtFreeTextModel(pojo.getDescriptions()));
        }

        try {
            // in case the name value is empty
            if (pojo.getNames().getLocalizedStrings().get(0)
                    .getCharacterString() == "") {
                throw new OpenInfraEntityException(
                        OpenInfraExceptionTypes.MISSING_NAME_IN_POJO);
            }

            // set the name
            atg.setPtFreeText2(ptfDao.getPtFreeTextModel(pojo.getNames()));
        } catch (NullPointerException npe) {
            throw new OpenInfraEntityException(
                    OpenInfraExceptionTypes.MISSING_NAME_IN_POJO);
        }

        // set the parent attribute type group id (optional)
        if (pojo.getSubgroupOf() != null) {
            atg.setAttributeTypeGroup(
                    em.find(AttributeTypeGroup.class,
                            pojo.getSubgroupOf()));
        } else {
            // reset the parent attribute type group id
            atg.setAttributeTypeGroup(null);
        }

        // return the model as mapping result
        return new MappingResult<AttributeTypeGroup>(atg.getId(), atg);
	}

}
