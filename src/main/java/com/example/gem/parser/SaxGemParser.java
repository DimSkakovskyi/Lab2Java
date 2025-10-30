package com.example.gem.parser;

import com.example.gem.ColorType;
import com.example.gem.Gem;
import com.example.gem.PreciousnessType;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SaxGemParser implements GemParser {

    private static final int FACETS_MIN = 4;
    private static final int FACETS_MAX = 15;

    @Override
    public List<Gem> parse(Path xmlPath) throws Exception {
        if (xmlPath == null) throw new IllegalArgumentException("xmlPath is null");
        if (!Files.exists(xmlPath)) throw new IOException("XML file not found: " + xmlPath);
        if (!Files.isRegularFile(xmlPath)) throw new IOException("Not a regular file: " + xmlPath);

        var factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        var parser = factory.newSAXParser();
        Handler h   = new Handler();
        parser.parse(xmlPath.toFile(), h);

        if (h.items.isEmpty()) {
            throw new IllegalArgumentException("No <Gem> elements found");
        }
        return h.items;
    }

    private static class Handler extends DefaultHandler {
        List<Gem> items = new ArrayList<>();
        Gem gem;
        Gem.VisualParameters vp;
        StringBuilder buf = new StringBuilder();
        Locator locator;

        boolean hasName, hasOrigin, hasPreciousness, hasValue;
        boolean hasColor, hasTransparency, hasFacets;

        @Override public void setDocumentLocator(Locator locator) { this.locator = locator; }

        @Override public void startElement(String uri, String local, String qName, Attributes atts) {
            buf.setLength(0);
            switch (qName) {
                case "Gem" -> {
                    if (gem != null) throw iae("Nested <Gem> is not allowed");
                    gem = new Gem();
                    String id = atts.getValue("id");
                    if (id == null || id.isBlank()) throw iae("Missing required attribute @id on <Gem>");
                    gem.setId(id.trim());
                    hasName = hasOrigin = hasPreciousness = hasValue = false;
                    hasColor = hasTransparency = hasFacets = false;
                }
                case "visualParameters" -> {
                    if (gem == null) throw iae("<visualParameters> must be inside <Gem>");
                    if (vp != null)  throw iae("Duplicated <visualParameters> in <Gem>");
                    vp = new Gem.VisualParameters();
                }
            }
        }

        @Override public void characters(char[] ch, int start, int length) { buf.append(ch, start, length); }

        @Override public void endElement(String uri, String local, String qName) {
            String txt = buf.toString().trim();
            switch (qName) {
                case "Gem" -> {
                    if (vp == null) throw iae("Missing <visualParameters> inside <Gem>");
                    gem.setVisualParameters(vp);

                    if (!hasName)         throw iae("Missing <name> inside <Gem>");
                    if (!hasOrigin)       throw iae("Missing <origin> inside <Gem>");
                    if (!hasPreciousness) throw iae("Missing <preciousness> inside <Gem>");
                    if (!hasValue)        throw iae("Missing <value> inside <Gem>");
                    if (!hasColor)        throw iae("Missing <color> inside <visualParameters>");
                    if (!hasTransparency) throw iae("Missing <transparency> inside <visualParameters>");
                    if (!hasFacets)       throw iae("Missing <facets> inside <visualParameters>");

                    items.add(gem);
                    gem = null; vp = null;
                }

                case "name" -> { ensureInGem("name"); if (txt.isEmpty()) throw iae("Blank <name>"); gem.setName(txt); hasName = true; }
                case "origin" -> { ensureInGem("origin"); if (txt.isEmpty()) throw iae("Blank <origin>"); gem.setOrigin(txt); hasOrigin = true; }
                case "preciousness" -> {
                    ensureInGem("preciousness"); if (txt.isEmpty()) throw iae("Blank <preciousness>");
                    try { gem.setPreciousness(PreciousnessType.valueOf(txt.toUpperCase())); }
                    catch (IllegalArgumentException ex) { throw iae("Invalid <preciousness>: '" + txt + "'"); }
                    hasPreciousness = true;
                }
                case "color" -> {
                    ensureInVP("color"); if (txt.isEmpty()) throw iae("Blank <color>");
                    try { vp.setColor(ColorType.valueOf(txt.toUpperCase())); }
                    catch (IllegalArgumentException ex) { throw iae("Invalid <color>: '" + txt + "'"); }
                    hasColor = true;
                }
                case "transparency" -> {
                    ensureInVP("transparency"); if (txt.isEmpty()) throw iae("Blank <transparency>");
                    vp.setTransparency(parseDecimal(txt, 0.0, 100.0, true, "Invalid <transparency>. Expected 0..100: "));
                    hasTransparency = true;
                }
                case "facets" -> {
                    ensureInVP("facets"); if (txt.isEmpty()) throw iae("Blank <facets>");
                    vp.setFacets(parseInt(txt, FACETS_MIN, FACETS_MAX, "Invalid <facets>. Expected 4..15: "));
                    hasFacets = true;
                }
                case "value" -> {
                    ensureInGem("value"); if (txt.isEmpty()) throw iae("Blank <value>");
                    gem.setValue(parseDecimal(txt, 0.0, Double.POSITIVE_INFINITY, false, "Invalid <value>. Must be > 0: "));
                    hasValue = true;
                }
            }
        }

        /* helpers */
        private void ensureInGem(String tag) { if (gem == null) throw iae("<" + tag + "> must be inside <Gem>"); }
        private void ensureInVP(String tag)  { ensureInGem(tag); if (vp == null) throw iae("<" + tag + "> must be inside <visualParameters>"); }
        private IllegalArgumentException iae(String msg) {
            if (locator != null) return new IllegalArgumentException("[line " + locator.getLineNumber() + ", col " + locator.getColumnNumber() + "] " + msg);
            return new IllegalArgumentException(msg);
        }
        private static int parseInt(String s, int min, int max, String msg) {
            try { int v = Integer.parseInt(s.trim()); if (v < min || v > max) throw new IllegalArgumentException(msg + s); return v; }
            catch (NumberFormatException ex) { throw new IllegalArgumentException(msg + s, ex); }
        }
        private static BigDecimal parseDecimal(String s, double min, double max, boolean inclusiveMax, String msg) {
            try {
                BigDecimal bd = new BigDecimal(s.trim()); double d = bd.doubleValue();
                boolean ok = d >= min && (inclusiveMax ? d <= max : d < max);
                if (!ok) throw new IllegalArgumentException(msg + s);
                return bd;
            } catch (NumberFormatException ex) { throw new IllegalArgumentException(msg + s, ex); }
        }
    }

}
