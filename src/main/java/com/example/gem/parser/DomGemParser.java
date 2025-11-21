package com.example.gem.parser;

import com.example.gem.ColorType;
import com.example.gem.Gem;
import com.example.gem.PreciousnessType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DomGemParser implements GemParser {

    private static final int FACETS_MIN = 4;
    private static final int FACETS_MAX = 15;

    @Override
    public List<Gem> parse(Path xmlPath) throws Exception {
        if (xmlPath == null) {
            throw new IllegalArgumentException("xmlPath is null");
        }
        if (!Files.exists(xmlPath)) {
            throw new IOException("XML file not found: " + xmlPath);
        }
        if (!Files.isRegularFile(xmlPath)) {
            throw new IOException("Not a regular file: " + xmlPath);
        }

        // 1) DOM-білдер
        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        var db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlPath.toFile());
        if (doc.getDocumentElement() == null) {
            throw new IllegalArgumentException("Empty XML document");
        }
        doc.getDocumentElement().normalize();

        // 2) <Gem> елементи
        NodeList nodes = doc.getElementsByTagName("Gem");
        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException("No <Gem> elements found");
        }

        List<Gem> result = new ArrayList<>(nodes.getLength());

        // 3) Розбір кожного Gem
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element e)) {
                continue;
            }

            Gem gem = new Gem();

            // id (обов'язковий)
            String id = e.getAttribute("id"); // ніколи не null
            if (id.isBlank()) {
                throw new IllegalArgumentException(ctx(e) + "Missing required attribute @id");
            }
            gem.setId(id.trim());

            // name, origin
            gem.setName(requiredText(e, "name"));
            gem.setOrigin(requiredText(e, "origin"));

            // preciousness -> enum
            String preciousText = requiredText(e, "preciousness");
            try {
                gem.setPreciousness(
                        PreciousnessType.valueOf(preciousText.trim().toUpperCase())
                );
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        ctx(e) + "Invalid <preciousness>: '" + preciousText
                                + "'. Allowed: " + enumValues(PreciousnessType.values())
                );
            }

            // visualParameters
            Element vp = requiredElement(e, "visualParameters");
            Gem.VisualParameters v = new Gem.VisualParameters();

            // color -> enum
            String colorText = requiredText(vp, "color");
            try {
                v.setColor(ColorType.valueOf(colorText.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        ctx(vp) + "Invalid <color>: '" + colorText
                                + "'. Allowed: " + enumValues(ColorType.values())
                );
            }

            // transparency: 0..100
            BigDecimal transparency = parseDecimal(
                    requiredText(vp, "transparency"),
                    0.0,
                    100.0,
                    ctx(vp) + "Invalid <transparency>. Expected 0..100: "
            );
            v.setTransparency(transparency);

            // facets: 4..15
            int facets = parseFacets(
                    requiredText(vp, "facets"),
                    ctx(vp) + "Invalid <facets>. Expected " + FACETS_MIN + ".." + FACETS_MAX + ": "
            );
            v.setFacets(facets);

            gem.setVisualParameters(v);

            // value: > 0
            BigDecimal value = parseDecimal(
                    requiredText(e, "value"),
                    Double.MIN_VALUE,
                    Double.POSITIVE_INFINITY,
                    ctx(e) + "Invalid <value>. Must be > 0: "
            );
            gem.setValue(value);

            result.add(gem);
        }

        return result;
    }

    private static Element requiredElement(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag); // ніколи не null
        if (nl.getLength() == 0) {
            throw new IllegalArgumentException(
                    ctx(parent) + "Missing <" + tag + "> in <" + parent.getTagName() + ">"
            );
        }
        Node n = nl.item(0);
        if (!(n instanceof Element el)) {
            throw new IllegalArgumentException(
                    ctx(parent) + "<" + tag + "> is not an element"
            );
        }
        return el;
    }

    private static String requiredText(Element parent, String tag) {
        Element el = requiredElement(parent, tag);
        String text = el.getTextContent();
        if (text == null) {
            throw new IllegalArgumentException(ctx(parent) + "Empty <" + tag + ">");
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(ctx(parent) + "Blank <" + tag + ">");
        }
        return trimmed;
    }

    private static BigDecimal parseDecimal(String s,
                                           double minInclusive,
                                           double maxInclusive,
                                           String msg) {
        try {
            BigDecimal bd = new BigDecimal(s.trim());
            double d = bd.doubleValue();
            if (d < minInclusive || d > maxInclusive) {
                throw new IllegalArgumentException(msg + s);
            }
            return bd;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(msg + s, ex);
        }
    }

    /**
     * Спеціальний парсер для кількості граней (facets).
     * Межі задані константами FACETS_MIN..FACETS_MAX.
     */
    private static int parseFacets(String s, String msg) {
        try {
            int v = Integer.parseInt(s.trim());
            if (v < FACETS_MIN || v > FACETS_MAX) {
                throw new IllegalArgumentException(msg + s);
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(msg + s, ex);
        }
    }

    private static String enumValues(Enum<?>[] vals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            sb.append(vals[i].name().toLowerCase());
            if (i + 1 < vals.length) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static String ctx(Element e) {
        return "[in <" + e.getTagName() + ">] ";
    }
}
