package de.btu.openinfra.backend.db.daos;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.jpa.model.ValueList;
import de.btu.openinfra.backend.db.jpa.model.ValueListValue;
import de.btu.openinfra.backend.db.pojos.LocalizedString;
import de.btu.openinfra.backend.db.pojos.PtFreeTextPojo;
import de.btu.openinfra.backend.db.pojos.ValueListValuePojo;

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
		return mapToPojoStatically(locale, vlv);
	}

	/**
	 * This is a static representation of the mapToPojo method and maps a JPA
	 * model object into the referring POJO object.
	 *
	 * @param vlv the JPA model object
	 * @return    the referring PJO object
	 */
	public static ValueListValuePojo mapToPojoStatically(
			Locale locale,
			ValueListValue vlv) {
		if(vlv != null) {
			ValueListValuePojo vlvPojo = new ValueListValuePojo();
			vlvPojo.setUuid(vlv.getId());
			vlvPojo.setTrid(vlv.getXmin());
			vlvPojo.setVisibility(vlv.getVisibility());
			vlvPojo.setDescriptions(PtFreeTextDao.mapToPojoStatically(
					locale,
					vlv.getPtFreeText1()));
			vlvPojo.setNames(PtFreeTextDao.mapToPojoStatically(
					locale,
					vlv.getPtFreeText2()));
			vlvPojo.setBelongsToValueList(
					vlv.getValueList().getId());
			return vlvPojo;
		} else {
			return null;
		} // end if else
	}

	@Override
	public MappingResult<ValueListValue> mapToModel(
			ValueListValuePojo pojo,
			ValueListValue vlv) {

        // in case the name or the belongs to value is empty
        if (pojo.getNames() == null ||
                pojo.getBelongsToValueList() == null) {
            return null;
        }

        // in case the name value is empty
        if (pojo.getNames().getLocalizedStrings().get(0)
                .getCharacterString() == "") {
            return null;
        }

        PtFreeTextDao ptfDao = new PtFreeTextDao(currentProjectId, schema);
        // set the description (is optional)
        if (pojo.getDescriptions() != null) {
            vlv.setPtFreeText1(
                    ptfDao.getPtFreeTextModel(pojo.getDescriptions()));
        }

        // set the name
        vlv.setPtFreeText2(ptfDao.getPtFreeTextModel(pojo.getNames()));

        // set the value list the value belongs to
        if (pojo.getBelongsToValueList() != null) {
            vlv.setValueList(
                    em.find(ValueList.class, pojo.getBelongsToValueList()));
        }

        // set the visibility of the value
        vlv.setVisibility(pojo.getVisibility());

        // return the model as mapping result
        return new MappingResult<ValueListValue>(vlv.getId(), vlv);
	}

	/**
     * This method creates a ValueListValuePojo shell that contains some
     * informations about the name, the description and the locale, the
     * visibility and the value list the value belongs to.
     *
     * TODO this method is identically implemented in other dao classes
     * and should be moved abstractly to the OpenInfraDao class
     *
     * @param locale the locale the informations should be saved at
     * @return       the ValueListValuePojo
     */
    public ValueListValuePojo newAttributeValueValues(Locale locale) {
        // create the return pojo
        ValueListValuePojo pojo = new ValueListValuePojo();

        PtLocaleDao ptl = new PtLocaleDao(currentProjectId, schema);
        List<LocalizedString> lcs = new LinkedList<LocalizedString>();
        LocalizedString ls = new LocalizedString();

        // set an empty character string
        ls.setCharacterString("");

        // set the locale of the character string
        ls.setLocale(PtLocaleDao.mapToPojoStatically(
                locale,
                ptl.read(locale)));
        lcs.add(ls);

        // add the localized string for the name
        pojo.setNames(new PtFreeTextPojo(lcs, null));

        // add the localized string for the description
        pojo.setDescriptions(new PtFreeTextPojo(lcs, null));

        // set the initial visibility
        pojo.setVisibility(true);

        // set the value list the value belongs to
        pojo.setBelongsToValueList(null);

        return pojo;
    }

}
