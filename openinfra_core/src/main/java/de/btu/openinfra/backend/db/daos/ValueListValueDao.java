package de.btu.openinfra.backend.db.daos;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.jpa.model.ValueList;
import de.btu.openinfra.backend.db.jpa.model.ValueListValue;
import de.btu.openinfra.backend.db.pojos.ValueListValuePojo;
import de.btu.openinfra.backend.exception.OpenInfraEntityException;
import de.btu.openinfra.backend.exception.OpenInfraExceptionTypes;

/**
 * This class represents the ValueListValue and is used to access the underlying
 * layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class ValueListValueDao
	extends OpenInfraValueDao<ValueListValuePojo, ValueListValue, ValueList> {

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public ValueListValueDao(UUID currentProjectId, OpenInfraSchemas schema) {
		super(currentProjectId, schema, ValueListValue.class, ValueList.class);
	}

	@Override
	public ValueListValuePojo mapToPojo(Locale locale, ValueListValue vlv) {
	    if(vlv != null) {
            ValueListValuePojo pojo = new ValueListValuePojo(vlv);
            PtFreeTextDao pftDao = new PtFreeTextDao(currentProjectId, schema);

            pojo.setVisibility(vlv.getVisibility());
            pojo.setDescriptions(pftDao.mapToPojo(
                    locale,
                    vlv.getPtFreeText1()));
            pojo.setNames(pftDao.mapToPojo(
                    locale,
                    vlv.getPtFreeText2()));
            pojo.setBelongsToValueList(
                    vlv.getValueList().getId());
            return pojo;
        } else {
            return null;
        } // end if else
	}

	@Override
	public MappingResult<ValueListValue> mapToModel(
			ValueListValuePojo pojo,
			ValueListValue vlv) {

	    PtFreeTextDao ptfDao = new PtFreeTextDao(currentProjectId, schema);
        // set the description (is optional)
        if (pojo.getDescriptions() != null) {
            vlv.setPtFreeText1(
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
            vlv.setPtFreeText2(ptfDao.getPtFreeTextModel(pojo.getNames()));
	    } catch (NullPointerException npe) {
            throw new OpenInfraEntityException(
                    OpenInfraExceptionTypes.MISSING_NAME_IN_POJO);
        }

	    try {
            // set the value list the value belongs to
            vlv.setValueList(
                    em.find(ValueList.class, pojo.getBelongsToValueList()));

            // set the visibility of the value
            vlv.setVisibility(pojo.getVisibility());
	    } catch (NullPointerException npe) {
            throw new OpenInfraEntityException(
                    OpenInfraExceptionTypes.MISSING_DATA_IN_POJO);
        }

        // return the model as mapping result
        return new MappingResult<ValueListValue>(vlv.getId(), vlv);
	}

}
