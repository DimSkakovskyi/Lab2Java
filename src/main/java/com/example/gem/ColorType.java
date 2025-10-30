package com.example.gem;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum ColorType {
    @XmlEnumValue("green")  GREEN,
    @XmlEnumValue("red")    RED,
    @XmlEnumValue("yellow") YELLOW
}
