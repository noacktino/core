package de.btu.openinfra.backend.db.rbac;

import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.core.UriInfo;

import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.daos.ValueListValueDao;
import de.btu.openinfra.backend.db.jpa.model.ValueList;
import de.btu.openinfra.backend.db.jpa.model.ValueListValue;
import de.btu.openinfra.backend.db.pojos.ValueListValuePojo;

public class ValueListValueRbac extends OpenInfraValueRbac<ValueListValuePojo, 
	ValueListValue, ValueList, ValueListValueDao> {

	public ValueListValueRbac(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId, schema, 
				ValueList.class, ValueListValueDao.class);
	}
	
	public ValueListValuePojo newAttributeValueValues(
			OpenInfraHttpMethod httpMethod, 
			UriInfo uriInfo, Locale locale) {
		checkPermission(httpMethod, uriInfo);
		return new ValueListValueDao(
				currentProjectId, schema).newAttributeValueValues(locale);
	}

}