package com.example.gem;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class XslTransformationTest {

    @Test
    void xslShouldGroupGemsByPreciousnessWithoutDuplication() throws Exception {
        Path xml = Path.of("src/main/resources/gems.xml");
        Path xsl = Path.of("src/main/resources/xsl/group-gems.xsl");

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(xsl.toFile()));

        // Це не обов'язково для тесту, але корисно мати читабельний результат
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2"
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new StreamSource(xml.toFile()), new StreamResult(out));

        String resultXml = out.toString(StandardCharsets.UTF_8);
        assertNotNull(resultXml);
        assertTrue(resultXml.contains("<root>"), "Кореневий елемент root має бути присутнім");

        // Парсимо результат у DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(resultXml.getBytes(StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();
        assertEquals("root", root.getTagName());

        NodeList preciousNodes = root.getElementsByTagName("precious");
        NodeList semiNodes = root.getElementsByTagName("semiprecious");

        assertEquals(1, preciousNodes.getLength(), "Має бути рівно один елемент <precious>");
        assertEquals(1, semiNodes.getLength(), "Має бути рівно один елемент <semiprecious>");

        Element precious = (Element) preciousNodes.item(0);
        Element semiprecious = (Element) semiNodes.item(0);

        NodeList preciousGems = precious.getElementsByTagName("Gem");
        NodeList semipreciousGems = semiprecious.getElementsByTagName("Gem");

        assertEquals(4, preciousGems.getLength(), "У precious мають бути 4 дорогоцінні камені");
        assertEquals(6, semipreciousGems.getLength(), "У semiprecious має бути 6 напівдорогоцінних");

        // Перевірка id та відсутності дублювання
        Set<String> allIds = new HashSet<>();

        assertGemIds(preciousGems, allIds,
                new String[]{"g1", "g2", "g3", "g9"},
                "precious");

        assertGemIds(semipreciousGems, allIds,
                new String[]{"g4", "g5", "g6", "g7", "g8", "g10"},
                "semiprecious");

        // Всі id (разом) мають бути унікальними і дорівнювати 10
        assertEquals(10, allIds.size(), "Повинно бути рівно 10 унікальних id у результаті XSL");
    }

    private static void assertGemIds(NodeList gems, Set<String> globalIds,
                                     String[] expectedIds, String groupName) {

        Set<String> localIds = new HashSet<>();
        for (int i = 0; i < gems.getLength(); i++) {
            Element g = (Element) gems.item(i);
            String id = g.getAttribute("id");
            assertNotNull(id, groupName + ": Gem без id");
            assertFalse(id.isBlank(), groupName + ": Gem має пустий id");

            assertTrue(localIds.add(id),
                    groupName + ": локальне дублювання id=" + id);
            assertTrue(globalIds.add(id),
                    "Глобальне дублювання id=" + id + " між групами");
        }

        // Строга перевірка складу id
        assertEquals(expectedIds.length, localIds.size(),
                groupName + ": розмір групи не збігається з очікуваним");

        for (String expectedId : expectedIds) {
            assertTrue(localIds.contains(expectedId),
                    groupName + ": очікуваний id " + expectedId + " відсутній");
        }
    }
}
