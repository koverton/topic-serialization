package com.solace.dev;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class TopicDefinitionParserTest {
    @Test
    public void basicStaticTest() {
        String expression = "static/stuff";
        List<Level> levels = TopicDefinitionParser.parse(expression);
        assertEquals(1, levels.size());
        assertEquals(Level.LvlType.STATIC, levels.get(0).type);
    }

    @Test
    public void basicFieldTest() {
        String expression = "{field1}";
        List<Level> levels = TopicDefinitionParser.parse(expression);
        assertEquals(1, levels.size());
        assertEquals(Level.LvlType.FIELD, levels.get(0).type);
        assertEquals("field1", levels.get(0).value);
    }

    @Test
    public void mixedPattern1Test() {
        String expression = "{field1}/{field2}/more/static/a{field3}";
        List<Level> levels = TopicDefinitionParser.parse(expression);
        assertEquals(5, levels.size());
        assertEquals(Level.LvlType.FIELD, levels.get(0).type);
        assertEquals(Level.LvlType.STATIC, levels.get(1).type);
        assertEquals(Level.LvlType.FIELD, levels.get(2).type);
        assertEquals(Level.LvlType.STATIC, levels.get(3).type);
        assertEquals(Level.LvlType.FIELD, levels.get(4).type);
    }

    @Test
    public void mixedPattern2Test() {
        String expression = "static/stuff/{field1}/{field2}/more/static/a{field3}/foo";
        List<Level> levels = TopicDefinitionParser.parse(expression);
        assertEquals(7, levels.size());
        assertEquals(Level.LvlType.STATIC, levels.get(0).type);
        assertEquals(Level.LvlType.FIELD, levels.get(1).type);
        assertEquals(Level.LvlType.STATIC, levels.get(2).type);
        assertEquals(Level.LvlType.FIELD, levels.get(3).type);
        assertEquals(Level.LvlType.STATIC, levels.get(4).type);
        assertEquals(Level.LvlType.FIELD, levels.get(5).type);
        assertEquals(Level.LvlType.STATIC, levels.get(6).type);
    }

    @Test
    public void mixedPattern3Test() {
        String expression = "{field1}{field2}";
        List<Level> levels = TopicDefinitionParser.parse(expression);
        assertEquals(2, levels.size());
        assertEquals(Level.LvlType.FIELD, levels.get(0).type);
        assertEquals(Level.LvlType.FIELD, levels.get(1).type);
    }
}
