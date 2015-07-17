package de.btu.openinfra.backend.db.daos;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.Query;

import de.btu.openinfra.backend.db.jpa.model.AttributeType;
import de.btu.openinfra.backend.db.jpa.model.AttributeValue;
import de.btu.openinfra.backend.db.jpa.model.AttributeValueDomain;
import de.btu.openinfra.backend.db.jpa.model.AttributeValueGeom;
import de.btu.openinfra.backend.db.jpa.model.AttributeValueGeomz;
import de.btu.openinfra.backend.db.jpa.model.AttributeValueValue;
import de.btu.openinfra.backend.db.jpa.model.TopicInstance;
import de.btu.openinfra.backend.db.pojos.AttributeTypePojo;
import de.btu.openinfra.backend.db.pojos.AttributeValueDomainPojo;
import de.btu.openinfra.backend.db.pojos.AttributeValueGeomPojo;
import de.btu.openinfra.backend.db.pojos.AttributeValueGeomzPojo;
import de.btu.openinfra.backend.db.pojos.AttributeValuePojo;
import de.btu.openinfra.backend.db.pojos.AttributeValueValuePojo;
import de.btu.openinfra.backend.db.pojos.LocalizedString;
import de.btu.openinfra.backend.db.pojos.PtFreeTextPojo;
import de.btu.openinfra.backend.db.pojos.ValueListValuePojo;

/**
 * This class represents the AttributeValue and is used to access the underlying
 * layer generated by JPA.
 *
 * @author <a href="http://www.b-tu.de">BTU</a> DBIS
 *
 */
public class AttributeValueDao extends OpenInfraValueDao<AttributeValuePojo,
	AttributeValue, TopicInstance> {

	/**
	 * This variable defines the default geometry type. The default type is
	 * used to implement the default read methods {@see OpenInfraDao}.
	 */
	// TODO: add a static parameter or make it dynamically
	private AttributeValueGeomType defaultGeomType =
			AttributeValueGeomType.TEXT;

	/**
	 * This variable defines the SQL string which is used to retrieve the
	 * requested geometry type string from database.
	 */
	public static final String GEOM_CLAUSE =
			"select %s "
			+ "from attribute_value_geom%s "
			+ "where id = cast(? as uuid)";

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 */
	public AttributeValueDao(
			UUID currentProjectId,
			OpenInfraSchemas schema) {
		super(currentProjectId, schema,
				AttributeValue.class, TopicInstance.class);
	}

	/**
	 * This is the required constructor which calls the super constructor and in
	 * turn creates the corresponding entity manager.
	 *
	 * @param currentProjectId the current project id (this should be null when
	 *                         the system schema is selected)
	 * @param schema           the required schema
	 * @param geomType         the required geom type
	 */
	public AttributeValueDao(
			UUID currentProjectId,
			OpenInfraSchemas schema,
			AttributeValueGeomType geomType) {
		super(currentProjectId, schema,
				AttributeValue.class, TopicInstance.class);
		if(geomType != null) {
			defaultGeomType = geomType;
		} // end if
	}

	/**
	 * This is a specific read method which provides the ability to set a
	 * specific geometry type.
	 *
	 * @param attributeValueId the id of the required object
	 * @param geomType         the required geometry type
	 * @return                 the requested object
	 */
	public AttributeValuePojo read(
			Locale locale,
			UUID attributeValueId,
			AttributeValueGeomType geomType) {
		if(geomType != null) {
			defaultGeomType = geomType;
		} // end if
		return mapToPojo(
				locale,
				em.find(AttributeValue.class, attributeValueId));
	}

	@Override
	public AttributeValuePojo mapToPojo(Locale locale, AttributeValue av) {
		if(av != null) {
			// 1. Define the Attribute value POJO that will be returned at
			// the end
			AttributeValuePojo avPojo = new AttributeValuePojo();
			// 2. Define the id which is used heavily throughout this method
			UUID id = av.getId();
			// 3. Retrieve the specific type of this attribute value
			// (not the geometry type)
			AttributeValueTypes type =
					AttributeValueTypes.valueOf(
							getAttributeValueTypeAsString(id));
			// 4. Define the attribute by the retrieved attribute value type
			switch (type) {
			// 4.a This is a standard query which can be implemented in a static
			//     way.
			case ATTRIBUTE_VALUE_VALUE:
				avPojo.setAttributeValueValue(
						AttributeValueValueDao.mapToPojoStatically(
								locale,
								em.find(AttributeValueValue.class, id)));
				break;

			// 4.b This is a standard query which can be implemented in a static
			//     way.
			case ATTRIBUTE_VALUE_DOMAIN:
				avPojo.setAttributeValueDomain(
						AttributeValueDomainDao.mapToPojoStatically(
								locale,
								em.find(AttributeValueDomain.class, id)));
				break;

			// 4.c This is a specific query which must be implemented
			//     dynamically
			case ATTRIBUTE_VALUE_GEOM:
				// We create a fake geom object and set the id of this object
				// in order to provide the required geom object.
				AttributeValueGeom avg = new AttributeValueGeom();
				avg.setId(id);
				avPojo.setAttributeValueGeom(
						new AttributeValueGeomDao(
								currentProjectId,
								schema,
								defaultGeomType).mapToPojo(locale, avg));
				break;

			// 4.c This is a specific query which must be implemented
			//     dynamically
			case ATTRIBUTE_VALUE_GEOMZ:
				// We create a fake geom object and set the id of this object
				// in order to provide the required geom object.
				AttributeValueGeomz avgz = new AttributeValueGeomz();
				avgz.setId(id);
				avPojo.setAttributeValueGeomz(
						new AttributeValueGeomzDao(
								currentProjectId,
								schema,
								defaultGeomType).mapToPojo(locale, avgz));
				break;

			default:
				// This should never happen!
				System.err.append(this + ": This should never happen!");
				break;

			} // end switch

			avPojo.setAttributeTypeId(
					av.getAttributeTypeToAttributeTypeGroup()
					.getAttributeType()
					.getId());
			avPojo.setUuid(id);
			avPojo.setAttributeValueType(type);
			return avPojo;

		} else {
			return null;
		} // end if else
	}


	/**
	 * This method sends a special SQL query to the database in order to specify
	 * the value type of an attribute
	 *
	 * @param uuid the id of the attribute
	 * @return     the name of the value type of this attribute as string
	 */
	private String getAttributeValueTypeAsString(UUID uuid) {
		Query q = em.createNativeQuery(
				"SELECT cast(p.relname as varchar) "
				+ "FROM attribute_value a, pg_class p "
				+ "WHERE a.id = cast(? as uuid) "
				+ "AND a.tableoid = p.oid;");
		q.setParameter(1, uuid.toString());
		return ((String)q.getResultList().get(0)).toUpperCase();
	}

	@Override
	public MappingResult<AttributeValue> mapToModel(
			AttributeValuePojo pojo,
			AttributeValue av) {
		// 1. Select the current value type
		switch (pojo.getAttributeValueType()) {
		case ATTRIBUTE_VALUE_VALUE:
		    // set the topic instance
			av.setTopicInstance(em.find(
					TopicInstance.class,
					pojo.getAttributeValueValue().getTopicInstanceId()));

			// create a list with all attribute value values
			List<AttributeValueValue> avvList =
					av.getTopicInstance().getAttributeValueValues();

			// create an iterator for this list
			Iterator<AttributeValueValue> avvIt = avvList.iterator();

			// 2. Iterate over all values and remove the current attribute value
			//    from list (if it belongs to the list)
			while(avvIt.hasNext()) {
				AttributeValueValue avv = avvIt.next();
				if(avv.getId().equals(
						pojo.getAttributeValueValue().getUuid())) {
					avvList.remove(avv);
					break;
				} // end if
			} // end while
			AttributeValueValueDao avvd = new AttributeValueValueDao(
					currentProjectId,
					schema);
			// add the new value to the list
			avvList.add(avvd.mapToModel(
							pojo.getAttributeValueValue(),
							avvd.createModelObject(
									pojo.getAttributeValueValue().getUuid()))
									.getModelObject());

			// add the values to the model
			av.getTopicInstance().setAttributeValueValues(avvList);
			break;

		case ATTRIBUTE_VALUE_GEOM:
		    System.out.println(pojo.getAttributeValueType() + " not implemented yet (AttributeValueDao.mapToModel");
		    // TODO The mapping from string to geometry is not possible.
		    /*
		    // set the topic instance
		    av.setTopicInstance(em.find(
                    TopicInstance.class,
                    pojo.getAttributeValueGeom().getTopicInstanceId()));

		    // create a list with all attribute value values
            List<AttributeValueGeom> avgList =
                    av.getTopicInstance().getAttributeValueGeoms();

            // create an iterator for this list
            Iterator<AttributeValueGeom> avgIt = avgList.iterator();

            // 2. Iterate over all values and remove the current attribute value
            //    from list (if it belongs to the list)
            while(avgIt.hasNext()) {
                AttributeValueGeom avg = avgIt.next();
                if(avg.getId().equals(
                        pojo.getAttributeValueGeom().getUuid())) {
                    avgList.remove(avg);
                    break;
                } // end if
            } // end while
            AttributeValueGeomDao avgd = new AttributeValueGeomDao(
                    currentProjectId,
                    schema);
            // add the new value to the list
            avgList.add(avgd.mapToModel(
                            pojo.getAttributeValueGeom(),
                            avgd.createModelObject(
                                    pojo.getAttributeValueGeom().getUuid()))
                                    .getModelObject());

            // add the values to the model
            av.getTopicInstance().setAttributeValueGeoms(avgList);
            */
            break;

		case ATTRIBUTE_VALUE_GEOMZ:
            System.out.println(pojo.getAttributeValueType() + " not implemented yet (AttributeValueDao.mapToModel");
            break;
		case ATTRIBUTE_VALUE_DOMAIN:
            System.out.println(pojo.getAttributeValueType() + " not implemented yet (AttributeValueDao.mapToModel");
            break;
		default:
			break;
		}

		// 3. Create the mapping result
		MappingResult<AttributeValue> mr = new MappingResult<AttributeValue>();
		mr.setId(pojo.getUuid());
		mr.setModelObject(av);
		return mr;

	}

	/**
	 * This method creates a AttributeValuePojo shell that contains some
	 * informations about the topicInstance, the attributeType and the locale.
	 *
	 * @param topicInstanceId the id of the topic instance the attribute value
	 *                        belongs to
	 * @param attributeTypeId the attribute type of the attribute value
	 * @param locale          the locale the informations should be saved at
	 * @return                the AttributeValuePojo
	 */
	public AttributeValuePojo createEmptyShell(
	        UUID topicInstanceId,
	        UUID attributeTypeId,
	        Locale locale) {

	    // create the return pojo
	    AttributeValuePojo pojo = new AttributeValuePojo();

	    // get the attribute type pojo from the passed attribute type id
	    AttributeTypePojo atP = AttributeTypeDao.mapToPojoStatically(
	            locale,
	            em.find(AttributeType.class, attributeTypeId));

	    // get the actual data type
	    String dataType = atP.getDataType().getNames()
	            .getLocalizedStrings().get(0).getCharacterString();

	    // set a isXX flag true if the data type is not varchar or text
	    boolean isXX = (dataType.equalsIgnoreCase("varchar") ||
	            dataType.equalsIgnoreCase("text")) ? false : true;

	    PtLocaleDao ptl = new PtLocaleDao(currentProjectId, schema);
        List<LocalizedString> lcs = new LinkedList<LocalizedString>();
        LocalizedString ls = new LocalizedString();

        // depending on the isXX flag
        if(isXX) {
            // set a xx locale
            ls.setLocale(
                    PtLocaleDao.mapToPojoStatically(
                            locale,
                            ptl.read(new Locale(
                                    PtFreeTextDao.NON_LINGUISTIC_CONTENT))));
        } else {
            // or the locale that was passed by the resource
            ls.setLocale(PtLocaleDao.mapToPojoStatically(
                    locale,
                    ptl.read(locale)));
        }

        // set the initial value
        ls.setCharacterString("");
        lcs.add(ls);

        // set the attribute type id
        pojo.setAttributeTypeId(attributeTypeId);

        // set the attribute value type
        pojo.setAttributeValueType(atP.getType());

        // retrieve the attribute type to attribute type group id
        // TODO this is just a fast workaround and should be replaced by a
        // named query or something like that
        Query q = em.createNativeQuery(
                "SELECT ata.id "
                + "FROM "
                + "attribute_type_group_to_topic_characteristic att, "
                + "attribute_type_to_attribute_type_group ata, "
                + "topic_instance ti "
                + "WHERE "
                + "att.topic_characteristic_id = ti.topic_characteristic_id AND "
                + "att.id = ata.attribute_type_group_to_topic_characteristic_id AND "
                + "ti.id = ? AND "
                + "ata.attribute_type_id = ?");
        q.setParameter(1, topicInstanceId);
        q.setParameter(2, attributeTypeId);

        // save the attribute type to attribute type group id from the query
        UUID ataId = (UUID.fromString(q.getResultList().get(0).toString()));

        // check if the domain of the attribute type is set
	    if (atP.getDomain() != null) {
	        // if true, we will need to create an attribute value domain object
	        AttributeValueDomainPojo avdP = new AttributeValueDomainPojo();
	        // set the topic instance the value belongs to
            avdP.setTopicInstanceId(topicInstanceId);
            // create the value list value object with an empty string
            ValueListValuePojo vlv = new ValueListValuePojo();
            vlv.setNames(new PtFreeTextPojo(lcs, null));
            avdP.setDomain(vlv);

            // add the attribute value domain object to the return container
            pojo.setAttributeValueDomain(avdP);
	    } else {
	        // check the type of the data type
	        switch (dataType) {
            case AttributeValueGeomDao.DATA_TYPE_NAME:
                // initialize a attribute value geom object and add it
                AttributeValueGeomPojo avgP = new AttributeValueGeomPojo();
                // set the topic instance the value belongs to
                avgP.setTopicInstanceId(topicInstanceId);
                // set the attribute type to attribute type group id
                avgP.setAttributeTypeToAttributeTypeGroupId(ataId);
                // add an empty value
                avgP.setGeom("");
                System.out.println("ID: " + ataId);
                // add the attribute value geom object to the return container
                pojo.setAttributeValueGeom(avgP);
                break;
            case AttributeValueGeomzDao.DATA_TYPE_NAME:
                // initialize a attribute value geomz object and add it
                AttributeValueGeomzPojo avgzP = new AttributeValueGeomzPojo();
                // set the topic instance the value belongs to
                avgzP.setTopicInstanceId(topicInstanceId);
                // set the attribute type to attribute type group id
                avgzP.setAttributeTypeToAttributeTypeGroupId(ataId);
                // add an empty value
                avgzP.setGeom("");

                // add the attribute value geomz object to the return container
                pojo.setAttributeValueGeomz(avgzP);
                break;
            default:
                // initialize a attribute value value object
                AttributeValueValuePojo avvP = new AttributeValueValuePojo();
                // set the topic instance the value belongs to
                avvP.setTopicInstanceId(topicInstanceId);
                // set the attribute type to attribute type group id
                avvP.setAttributeTypeToAttributeTypeGroupId(ataId);
                // add an empty value object
                avvP.setValue(new PtFreeTextPojo(lcs, null));

                // add the attribute value geomz object to the return container
                pojo.setAttributeValueValue(avvP);
                break;
            }
	    }

	    return pojo;
	}
}

