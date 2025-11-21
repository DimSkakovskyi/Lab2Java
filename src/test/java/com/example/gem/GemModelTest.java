package com.example.gem;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class GemModelTest {

    @Test
    void gemAndVisualParametersShouldStoreValuesCorrectly() {
        Gem gem = new Gem();
        gem.setId("g1");
        gem.setName("Emerald");
        gem.setPreciousness(PreciousnessType.PRECIOUS);
        gem.setOrigin("Colombia");
        gem.setValue(new BigDecimal("12345.67"));

        Gem.VisualParameters vp = new Gem.VisualParameters();
        vp.setColor(ColorType.GREEN);
        vp.setTransparency(new BigDecimal("85.00"));
        vp.setFacets(8);

        gem.setVisualParameters(vp);

        assertEquals("g1", gem.getId());
        assertEquals("Emerald", gem.getName());
        assertEquals(PreciousnessType.PRECIOUS, gem.getPreciousness());
        assertEquals("Colombia", gem.getOrigin());
        assertEquals(new BigDecimal("12345.67"), gem.getValue());

        assertNotNull(gem.getVisualParameters());
        assertEquals(ColorType.GREEN, gem.getVisualParameters().getColor());
        assertEquals(new BigDecimal("85.00"), gem.getVisualParameters().getTransparency());
        assertEquals(8, gem.getVisualParameters().getFacets());
    }
}
