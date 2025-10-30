package com.example.gem;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GemType", propOrder = {
        "name", "preciousness", "origin", "visualParameters", "value"
})
@XmlRootElement(name = "Gem")
public class Gem {

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private PreciousnessType preciousness;

    @XmlElement(required = true)
    private String origin;

    @XmlElement(required = true)
    private VisualParameters visualParameters;

    @XmlElement(required = true)
    private BigDecimal value;

    @XmlAttribute(name = "id", required = true)
    private String id;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PreciousnessType getPreciousness() { return preciousness; }
    public void setPreciousness(PreciousnessType preciousness) { this.preciousness = preciousness; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public VisualParameters getVisualParameters() { return visualParameters; }
    public void setVisualParameters(VisualParameters visualParameters) { this.visualParameters = visualParameters; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "VisualParameters", propOrder = {"color", "transparency", "facets"})
    public static class VisualParameters {

        @XmlElement(required = true)
        private ColorType color;

        @XmlElement(required = true)
        private BigDecimal transparency; // 0..100, 2 знаки після коми

        @XmlElement(required = true)
        private int facets; // 4..15

        public ColorType getColor() { return color; }
        public void setColor(ColorType color) { this.color = color; }

        public BigDecimal getTransparency() { return transparency; }
        public void setTransparency(BigDecimal transparency) { this.transparency = transparency; }

        public int getFacets() { return facets; }
        public void setFacets(int facets) { this.facets = facets; }
    }
}
