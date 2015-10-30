package de.btu.openinfra.backend.db.pojos;

import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import de.btu.openinfra.backend.db.daos.MetaDataDao;
import de.btu.openinfra.backend.db.jpa.model.OpenInfraModelObject;

@XmlRootElement
public class AttributeTypeToAttributeTypeGroupPojo extends OpenInfraMetaDataPojo {

    private AttributeTypePojo attributeType;
    private UUID attributeTypeGroupId;
    private MultiplicityPojo multiplicity;
    private ValueListValuePojo defaultValue;
    private Integer order;

    /* Default constructor */
    public AttributeTypeToAttributeTypeGroupPojo() {
    }

    /* Constructor that will set the id, trid and meta data automatically */
    public AttributeTypeToAttributeTypeGroupPojo(OpenInfraModelObject modelObject, MetaDataDao mdDao) {
        super(modelObject, mdDao);
    }

    public AttributeTypePojo getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeTypePojo attributeType) {
        this.attributeType = attributeType;
    }

    public UUID getAttributeTypeGroupId() {
        return attributeTypeGroupId;
    }

    public void setAttributeTypeGroupId(UUID attributeTypeGroupId) {
        this.attributeTypeGroupId = attributeTypeGroupId;
    }

    public MultiplicityPojo getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(MultiplicityPojo multiplicity) {
        this.multiplicity = multiplicity;
    }

    public ValueListValuePojo getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(ValueListValuePojo defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

}
