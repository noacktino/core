package de.btu.openinfra.backend.db.daos.meta;

import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.daos.OpenInfraDao;
import de.btu.openinfra.backend.db.jpa.model.meta.Level;
import de.btu.openinfra.backend.db.pojos.meta.LevelPojo;
import de.btu.openinfra.backend.exception.OpenInfraEntityException;
import de.btu.openinfra.backend.exception.OpenInfraExceptionTypes;

/**
 * This class represents the log level and is used to access the underlying
 * layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class LevelDao
    extends OpenInfraDao<LevelPojo, Level> {

    /**
     * This is the required constructor which calls the super constructor and in
     * turn creates the corresponding entity manager.
     * @param currentProjectId The identifier of the current project.
     * @param schema           the required schema
     */
    public LevelDao(UUID currentProjectId, OpenInfraSchemas schema) {
        super(null, OpenInfraSchemas.META_DATA, Level.class);
    }

    @Override
    public LevelPojo mapToPojo(Locale locale, Level l) {
        if(l != null) {
            LevelPojo pojo = new LevelPojo(l);
            pojo.setLevel(l.getLevel());
            return pojo;
        } else {
            return null;
        }
    }

    @Override
    public MappingResult<Level> mapToModel(LevelPojo pojo, Level lv) {
        if(pojo != null) {
            Level resultLevel = null;
            try {
                resultLevel = lv;
                if(resultLevel == null) {
                    resultLevel = new Level();
                    resultLevel.setId(pojo.getUuid());
                }
                resultLevel.setLevel(pojo.getLevel());
            } catch (NullPointerException npe) {
                throw new OpenInfraEntityException(
                        OpenInfraExceptionTypes.MISSING_DATA_IN_POJO);
            }
            return new MappingResult<Level>(resultLevel.getId(), resultLevel);
        }
        else {
            return null;
        }
    }

}
