package com.example.gem.parser;

import org.xml.sax.*;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XmlValidator {
    private XmlValidator() {}

    public static void validate(Path xml, Path xsd) throws Exception {
        if (xml == null || xsd == null) throw new IllegalArgumentException("xml/xsd path is null");
        if (!Files.isRegularFile(xml)) throw new IllegalArgumentException("XML file not found: " + xml);
        if (!Files.isRegularFile(xsd)) throw new IllegalArgumentException("XSD file not found: " + xsd);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Schema schema = sf.newSchema(xsd.toFile());
        Validator validator = schema.newValidator();

        // Кидаємо помилку з рядком/колонкою
        validator.setErrorHandler(new ErrorHandler() {
            @Override public void warning(SAXParseException e) { /* ігноруємо або логуй */ }
            @Override public void error(SAXParseException e) throws SAXException {
                throw new SAXException(msg("Validation error", e));
            }
            @Override public void fatalError(SAXParseException e) throws SAXException {
                throw new SAXException(msg("Validation fatal", e));
            }
            private String msg(String kind, SAXParseException e) {
                return kind + " at line " + e.getLineNumber() + ", col " + e.getColumnNumber()
                        + ": " + e.getMessage();
            }
        });

        try (InputStream in = Files.newInputStream(xml)) {
            validator.validate(new StreamSource(in));
        }
    }
}
