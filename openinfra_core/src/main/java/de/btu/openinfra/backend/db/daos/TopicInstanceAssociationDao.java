package de.btu.openinfra.backend.db.daos;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.btu.openinfra.backend.db.MappingResult;
import de.btu.openinfra.backend.db.OpenInfraSchemas;
import de.btu.openinfra.backend.db.jpa.model.TopicInstance;
import de.btu.openinfra.backend.db.jpa.model.TopicInstanceXTopicInstance;
import de.btu.openinfra.backend.db.pojos.project.TopicInstanceAssociationPojo;

/**
 * This class represents the TopicInstanceAssociation and is used to access the
 * underlying layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class TopicInstanceAssociationDao extends OpenInfraValueValueDao<
	TopicInstanceAssociationPojo,
	TopicInstanceXTopicInstance,
	TopicInstance, TopicInstance> {

	/**
	 * This is the required constructor which calls the super constructor and
	 * in turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public TopicInstanceAssociationDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(
				currentProjectId,
				schema,
				TopicInstanceXTopicInstance.class,
				TopicInstance.class, TopicInstance.class);
	}

	/**
	 * This method returns a list of parents relative to specified topic
	 * instance object.
	 *
	 * @param locale the given locale
	 * @param self   the specified topic instance object
	 * @return       a list of parent topic instances
	 */
	public List<TopicInstanceAssociationPojo> readParents(
			Locale locale, UUID self) {

		TopicInstanceDao ti =
				new TopicInstanceDao(currentProjectId, schema);

		List<TopicInstanceAssociationPojo> parents =
				new LinkedList<TopicInstanceAssociationPojo>();

		TopicInstanceXTopicInstance parent = readParent(locale, self);

		while(true) {
			if(parent != null) {
				parents.add(
						new TopicInstanceAssociationPojo(
								parent.getId(),
								ti.mapToPojo(
										locale,
										parent.getTopicInstance1Bean()),
								RelationshipTypeDao.mapToPojoStatically(
										locale,
										parent.getRelationshipType(),
										new MetaDataDao(
										        currentProjectId, schema))));
				parent = readParent(
						locale,
						parent.getTopicInstance1Bean().getId());
			} else {
				break;
			} // end if else

			// ++++++++++ Dirty hack!!! +++++++++
			// TODO Obviously, there is a bug in the test data. Cycles can occur
			// and a parent is a child of it's child. This must be discussed.
			// Workaround: in order to provide the functionality, the loop will
			// terminate in the third step.

			int count = 0;
			if(parents.size() > 0) {
				for(TopicInstanceAssociationPojo help : parents) {
					for(TopicInstanceAssociationPojo p : parents) {
						if(help.getUuid().equals(p.getUuid())) {
							count++;
						}
					}
					if(count > 3) {
						break;
					}
				}
			}

			if(count == 4) {
				System.out.println("Big Problem in "
						+ "TopicInstanceAssotionationDao --> readParents"
						+ " This should be discussed and fixed!");
				break;
			}

		} // end while

		Collections.reverse(parents);
		return parents;
	}

	private TopicInstanceXTopicInstance readParent(Locale locale, UUID self) {
		List<TopicInstanceXTopicInstance> txt =
				em.createNamedQuery(
						"TopicInstanceXTopicInstance.findParent",
						TopicInstanceXTopicInstance.class)
						.setParameter(
								"self",
								em.find(TopicInstance.class, self))
						.getResultList();
		if(txt != null && txt.size() > 0) {
			return txt.get(0);
		} else {
			return null;
		}
	}


	@Override
	public TopicInstanceAssociationPojo mapToPojo(
			Locale locale,
			TopicInstanceXTopicInstance txt) {
	    if (txt != null) {
	        MetaDataDao mdDao = new MetaDataDao(currentProjectId, schema);
	        TopicInstanceAssociationPojo pojo =
	                new TopicInstanceAssociationPojo(txt, mdDao);

	        pojo.setRelationshipType(
	                RelationshipTypeDao.mapToPojoStatically(
	                        locale,
	                        txt.getRelationshipType(),
	                        mdDao));
	        pojo.setAssociatedInstance(
	                new TopicInstanceDao(currentProjectId, schema).mapToPojo(
	                        locale,
	                        txt.getTopicInstance2Bean()));
            return pojo;
        } else {
            return null;
        }
	    /*
	    return mapToPojoStatically(locale, txt,
                new MetaDataDao(currentProjectId, schema));
                */
	}

	/**
     * This method implements the method mapToPojo in a static way.
     *
     * @param locale the requested language as Java.util locale
     * @param txt    the model object
     * @param mdDao  the meta data DAO
     * @return       the POJO object when the model object is not null else null
     */
    public static TopicInstanceAssociationPojo mapToPojoStatically(
            Locale locale,
            TopicInstanceXTopicInstance txt,
            MetaDataDao mdDao) {
        if (txt != null) {
            TopicInstanceAssociationPojo pojo =
                    new TopicInstanceAssociationPojo(txt, mdDao);

            pojo.setRelationshipType(
                    RelationshipTypeDao.mapToPojoStatically(
                            locale,
                            txt.getRelationshipType(),
                            mdDao));
            pojo.setAssociatedInstance(
                    TopicInstanceDao.mapToPojoStatically(
                            locale,
                            txt.getTopicInstance2Bean(),
                            mdDao));
            return pojo;
        } else {
            return null;
        }
    }

	@Override
	public MappingResult<TopicInstanceXTopicInstance> mapToModel(
			TopicInstanceAssociationPojo pojo,
			TopicInstanceXTopicInstance txt) {

        // TODO set the model values

        // return the model as mapping result
        return new MappingResult<TopicInstanceXTopicInstance>(
                txt.getId(), txt);
	}

}
