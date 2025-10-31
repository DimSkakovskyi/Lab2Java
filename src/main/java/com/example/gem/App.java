// src/main/java/com/example/gem/App.java
package com.example.gem;

import com.example.gem.parser.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        Path xml = Path.of("src/main/resources/gems.xml");
        Path xsd = Path.of("src/main/resources/xsd/gem.xsd");

        // Валідація перед парсингом
        XmlValidator.validate(xml, xsd);
        System.out.println("XML is VALID against XSD ✔");

        // Далі — методи парсингу
        GemParser dom  = new DomGemParser();
        GemParser sax  = new SaxGemParser();
        GemParser stax = new StaxGemParser();

        List<Gem> a = dom.parse(xml);
        List<Gem> b = sax.parse(xml);
        List<Gem> c = stax.parse(xml);

        var byValueDescThenName =
                Comparator.comparing(Gem::getValue, Comparator.nullsLast(BigDecimal::compareTo))
                        .reversed()
                        .thenComparing(Gem::getName, String.CASE_INSENSITIVE_ORDER);

        a.sort(byValueDescThenName);
        b.sort(byValueDescThenName);
        c.sort(byValueDescThenName);

        System.out.println("DOM:");  a.forEach(g -> System.out.println(g.getId()+"  "+g.getName()+"  "+g.getValue()));
        System.out.println("\nSAX:"); b.forEach(g -> System.out.println(g.getId()+"  "+g.getName()+"  "+g.getValue()));
        System.out.println("\nStAX:");c.forEach(g -> System.out.println(g.getId()+"  "+g.getName()+"  "+g.getValue()));
    }
}
