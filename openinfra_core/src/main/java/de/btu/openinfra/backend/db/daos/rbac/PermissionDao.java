package de.btu.openinfra.backend.db.daos.rbac;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.daos.OpenInfraDao;
import de.btu.openinfra.backend.db.jpa.model.rbac.Permission;
import de.btu.openinfra.backend.db.pojos.rbac.PermissionPojo;

public class PermissionDao extends OpenInfraDao<PermissionPojo, Permission> {

	public PermissionDao() {
		super(null, OpenInfraSchemas.RBAC, Permission.class);
	}
	
	public PermissionDao(UUID currentProjectId, OpenInfraSchemas schema) {
		super(currentProjectId, schema, Permission.class);
	}

	@Override
	public PermissionPojo mapToPojo(Locale locale, Permission modelObject) {
		PermissionPojo pojo = new PermissionPojo(modelObject);
		pojo.setDescription(modelObject.getDescription());
		pojo.setPermission(modelObject.getPermission());
		return pojo;
	}

	@Override
	public MappingResult<Permission> mapToModel(PermissionPojo pojoObject,
			Permission modelObject) {
		modelObject.setDescription(pojoObject.getDescription());
		modelObject.setPermission(pojoObject.getPermission());
		return new MappingResult<Permission>(modelObject.getId(), modelObject);
	}
	
}
