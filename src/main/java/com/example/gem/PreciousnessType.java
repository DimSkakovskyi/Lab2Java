package com.example.gem;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum PreciousnessType {
    @XmlEnumValue("precious") PRECIOUS,
    @XmlEnumValue("semiprecious") SEMIPRECIOUS
}
