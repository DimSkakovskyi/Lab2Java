package com.example.gem.parser;

import com.example.gem.ColorType;
import com.example.gem.Gem;
import com.example.gem.PreciousnessType;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StaxGemParser implements GemParser {

    @Override
    public List<Gem> parse(Path xmlPath) throws Exception {
        var xif = XMLInputFactory.newInstance();
        try (var in = Files.newInputStream(xmlPath)) {
            var r = xif.createXMLStreamReader(in);
            List<Gem> items = new ArrayList<>();
            Gem gem = null; Gem.VisualParameters vp = null;
            String tag = null;

            while (r.hasNext()) {
                int ev = r.next();
                if (ev == XMLStreamConstants.START_ELEMENT) {
                    tag = r.getLocalName();
                    switch (tag) {
                        case "Gem" -> {
                            gem = new Gem();
                            gem.setId(r.getAttributeValue(null, "id"));
                        }
                        case "visualParameters" -> vp = new Gem.VisualParameters();
                    }
                } else if (ev == XMLStreamConstants.CHARACTERS) {
                    if (tag == null) continue;
                    String txt = r.getText().trim();
                    if (txt.isEmpty() || gem == null) continue;
                    switch (tag) {
                        case "name" -> gem.setName(txt);
                        case "preciousness" -> gem.setPreciousness(PreciousnessType.valueOf(txt.toUpperCase()));
                        case "origin" -> gem.setOrigin(txt);
                        case "color" -> vp.setColor(ColorType.valueOf(txt.toUpperCase()));
                        case "transparency" -> vp.setTransparency(new BigDecimal(txt));
                        case "facets" -> vp.setFacets(Integer.parseInt(txt));
                        case "value" -> gem.setValue(new BigDecimal(txt));
                    }
                } else if (ev == XMLStreamConstants.END_ELEMENT) {
                    String end = r.getLocalName();
                    if ("visualParameters".equals(end)) {
                        // attach later when Gem closes
                    } else if ("Gem".equals(end)) {
                        gem.setVisualParameters(vp);
                        items.add(gem);
                        gem = null; vp = null;
                    }
                    tag = null;
                }
            }
            return items;
        }
    }
}
