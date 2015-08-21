package de.btu.openinfra.backend.db.rbac;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.daos.AttributeValueGeomType;
import de.btu.openinfra.backend.db.daos.TopicDao;
import de.btu.openinfra.backend.db.pojos.TopicPojo;

public class TopicRbac {
	
	/**
	 * The UUID of the current project.
	 */
	private UUID currentProjectId;
	/**
	 * The currently used schema.
	 */
	private OpenInfraSchemas schema;
	
	public TopicRbac(UUID currentProjectId, OpenInfraSchemas schema) {
		this.currentProjectId = currentProjectId;
		this.schema = schema;
	}
	
	public TopicPojo read(
			Locale locale, 
			UUID topicInstanceId, 
			AttributeValueGeomType geomType) {
		// Since this Class is not a rbac class, we use a closely related class 
		// to check the permission.
		// TODO this should be TopicInstance
			new TopicCharacteristicRbac(
					currentProjectId, schema).checkPermission();
		return new TopicDao(currentProjectId, schema).read(
				locale, topicInstanceId, geomType);
	}
}
