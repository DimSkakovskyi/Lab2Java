package com.example.gem.parser;

import com.example.gem.Gem;
import java.nio.file.Path;
import java.util.List;

public interface GemParser {
    List<Gem> parse(Path xmlPath) throws Exception;
}
