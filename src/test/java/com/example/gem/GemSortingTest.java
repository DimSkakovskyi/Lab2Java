package com.example.gem;

import com.example.gem.parser.DomGemParser;
import com.example.gem.parser.GemParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GemSortingTest {

    @Test
    void gemsShouldBeSortedByValueDescThenNameAsc() throws Exception {
        Path xml = Path.of("src/main/resources/gems.xml");
        GemParser parser = new DomGemParser();
        List<Gem> list = parser.parse(xml);

        Comparator<Gem> cmp =
                Comparator.comparing(Gem::getValue, Comparator.nullsLast(BigDecimal::compareTo))
                        .reversed()
                        .thenComparing(Gem::getName, String.CASE_INSENSITIVE_ORDER);

        list.sort(cmp);

        // Очікуваний порядок id (по спаданню value, потім name)
        String[] expectedOrder = {
                "g9",  // 50250.00
                "g2",  // 21500.00
                "g3",  // 18990.00
                "g1",  // 12345.67
                "g4",  // 3200.00
                "g8",  // 2750.00
                "g7",  // 2100.00
                "g5",  // 1450.50
                "g6",  // 980.00
                "g10"  // 875.25
        };

        List<String> actualOrder = list.stream()
                .map(Gem::getId)
                .toList();

        assertArrayEquals(expectedOrder, actualOrder.toArray(new String[0]));
    }
}
