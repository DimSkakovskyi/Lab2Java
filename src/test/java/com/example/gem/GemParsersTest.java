package com.example.gem;

import com.example.gem.parser.DomGemParser;
import com.example.gem.parser.GemParser;
import com.example.gem.parser.SaxGemParser;
import com.example.gem.parser.StaxGemParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GemParsersTest {

    private static final Path XML_PATH = Path.of("src/main/resources/gems.xml");

    @Test
    void domParserShouldReadAllGemsStrictly() throws Exception {
        GemParser parser = new DomGemParser();
        List<Gem> list = parser.parse(XML_PATH);

        assertEquals(10, list.size(), "DOM: очікується 10 дорогоцінних каменів");

        assertGemStrict(list, "g1",
                "Emerald", PreciousnessType.PRECIOUS, "Colombia",
                ColorType.GREEN, "85.00", 8, "12345.67");

        assertGemStrict(list, "g2",
                "Ruby", PreciousnessType.PRECIOUS, "Myanmar",
                ColorType.RED, "70.00", 12, "21500.00");

        assertGemStrict(list, "g3",
                "Sapphire", PreciousnessType.PRECIOUS, "Sri Lanka",
                ColorType.BLUE, "60.50", 10, "18990.00");

        assertGemStrict(list, "g4",
                "Topaz", PreciousnessType.SEMIPRECIOUS, "Brazil",
                ColorType.YELLOW, "92.00", 6, "3200.00");

        assertGemStrict(list, "g5",
                "Amethyst", PreciousnessType.SEMIPRECIOUS, "Uruguay",
                ColorType.PURPLE, "35.00", 9, "1450.50");

        assertGemStrict(list, "g6",
                "Garnet", PreciousnessType.SEMIPRECIOUS, "India",
                ColorType.RED, "40.00", 4, "980.00");

        assertGemStrict(list, "g7",
                "Peridot", PreciousnessType.SEMIPRECIOUS, "Pakistan",
                ColorType.GREEN, "55.25", 15, "2100.00");

        assertGemStrict(list, "g8",
                "Opal", PreciousnessType.SEMIPRECIOUS, "Australia",
                ColorType.WHITE, "10.00", 7, "2750.00");

        assertGemStrict(list, "g9",
                "Diamond", PreciousnessType.PRECIOUS, "Botswana",
                ColorType.COLORLESS, "100.00", 8, "50250.00");

        assertGemStrict(list, "g10",
                "Citrine", PreciousnessType.SEMIPRECIOUS, "Madagascar",
                ColorType.YELLOW, "0.00", 5, "875.25");
    }

    @Test
    void saxAndStaxParsersShouldMatchDomExactly() throws Exception {
        GemParser domParser = new DomGemParser();
        GemParser saxParser = new SaxGemParser();
        GemParser staxParser = new StaxGemParser();

        List<Gem> dom = domParser.parse(XML_PATH);
        List<Gem> sax = saxParser.parse(XML_PATH);
        List<Gem> stax = staxParser.parse(XML_PATH);

        assertEquals(dom.size(), sax.size(), "SAX: розмір повинен збігатися з DOM");
        assertEquals(dom.size(), stax.size(), "StAX: розмір повинен збігатися з DOM");

        // Перевіряємо для кожного id, що всі парсери читають однаково
        for (Gem domGem : dom) {
            Gem saxGem = findById(sax, domGem.getId());
            Gem staxGem = findById(stax, domGem.getId());

            assertNotNull(saxGem, "SAX: Gem з id=" + domGem.getId() + " має існувати");
            assertNotNull(staxGem, "StAX: Gem з id=" + domGem.getId() + " має існувати");

            assertGemEquals(domGem, saxGem, "SAX має збігатися з DOM для id=" + domGem.getId());
            assertGemEquals(domGem, staxGem, "StAX має збігатися з DOM для id=" + domGem.getId());
        }
    }

    // ---- helpers ----

    private static Gem findById(List<Gem> list, String id) {
        return list.stream().filter(g -> id.equals(g.getId())).findFirst().orElse(null);
    }

    private static void assertGemStrict(
            List<Gem> list,
            String id,
            String name,
            PreciousnessType preciousness,
            String origin,
            ColorType color,
            String transparency,
            int facets,
            String value
    ) {
        Gem gem = findById(list, id);
        assertNotNull(gem, "Gem з id=" + id + " має бути в списку");

        assertEquals(id, gem.getId());
        assertEquals(name, gem.getName());
        assertEquals(preciousness, gem.getPreciousness());
        assertEquals(origin, gem.getOrigin());

        assertNotNull(gem.getVisualParameters(), "visualParameters не має бути null");
        assertEquals(color, gem.getVisualParameters().getColor());
        assertEquals(new BigDecimal(transparency), gem.getVisualParameters().getTransparency());
        assertEquals(facets, gem.getVisualParameters().getFacets());

        assertEquals(new BigDecimal(value), gem.getValue());
    }

    private static void assertGemEquals(Gem expected, Gem actual, String msgPrefix) {
        assertEquals(expected.getId(), actual.getId(), msgPrefix + ": id");
        assertEquals(expected.getName(), actual.getName(), msgPrefix + ": name");
        assertEquals(expected.getPreciousness(), actual.getPreciousness(), msgPrefix + ": preciousness");
        assertEquals(expected.getOrigin(), actual.getOrigin(), msgPrefix + ": origin");

        assertNotNull(actual.getVisualParameters(), msgPrefix + ": visualParameters null");
        assertEquals(expected.getVisualParameters().getColor(), actual.getVisualParameters().getColor(), msgPrefix + ": color");
        assertEquals(expected.getVisualParameters().getTransparency(), actual.getVisualParameters().getTransparency(), msgPrefix + ": transparency");
        assertEquals(expected.getVisualParameters().getFacets(), actual.getVisualParameters().getFacets(), msgPrefix + ": facets");

        assertEquals(expected.getValue(), actual.getValue(), msgPrefix + ": value");
    }
}
