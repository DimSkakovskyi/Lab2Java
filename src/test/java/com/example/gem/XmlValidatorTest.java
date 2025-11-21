package com.example.gem;

import com.example.gem.parser.XmlValidator;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class XmlValidatorTest {

    @Test
    void validGemsXmlShouldPassValidation(){
        Path xml = Path.of("src/main/resources/gems.xml");
        Path xsd = Path.of("src/main/resources/xsd/gem.xsd");

        assertDoesNotThrow(() -> XmlValidator.validate(xml, xsd));
    }

    @Test
    void invalidXmlShouldFailValidation() throws Exception {
        Path xsd = Path.of("src/main/resources/xsd/gem.xsd");

        // Робимо тимчасовий некоректний XML (без обов’язкового атрибута id)
        String badXmlContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<Gems>\n" +
                        "  <Gem>\n" +                      // немає id, немає повного набору елементів
                        "    <name>Test</name>\n" +
                        "    <preciousness>precious</preciousness>\n" +
                        "  </Gem>\n" +
                        "</Gems>\n";

        Path tempXml = Files.createTempFile("bad-gems", ".xml");
        Files.writeString(tempXml, badXmlContent);

        assertThrows(Exception.class, () -> XmlValidator.validate(tempXml, xsd));
    }
}
