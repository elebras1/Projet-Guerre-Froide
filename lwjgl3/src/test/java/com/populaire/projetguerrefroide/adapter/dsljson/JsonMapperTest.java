package com.populaire.projetguerrefroide.adapter.dsljson;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonMapperTest {

    private final JsonMapper mapper = new JsonMapper();

    private JsonValue parse(String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return mapper.parse(bytes);
    }

    @Test
    void testIsTypeMethods() throws IOException {
        String json = """
            {
              "str": "hello",
              "int": 123,
              "double": 3.14,
              "boolTrue": true,
              "boolFalse": false,
              "nullVal": null,
              "arr": [1, 2, 3],
              "obj": {"a": 1}
            }
            """;

        JsonValue root = parse(json);

        assertTrue(root.get("str").isString());
        assertTrue(root.get("int").isLong());
        assertTrue(root.get("double").isDouble());
        assertTrue(root.get("boolTrue").isBoolean());
        assertTrue(root.get("boolFalse").isBoolean());
        assertTrue(root.get("nullVal").isNull());
        assertTrue(root.get("arr").isArray());
        assertTrue(root.get("obj").isObject());
    }

    @Test
    void testArrayAccessAndBounds() throws IOException {
        String json = "[10, 20, 30]";
        JsonValue root = parse(json);

        assertTrue(root.isArray());
        assertEquals(3, root.getSize());

        assertEquals(10, root.get(0).asLong());
        assertEquals(30, root.get(2).asLong());
        assertNull(root.get(3)); // out of bounds
    }

    @Test
    void testObjectFieldAccess() throws IOException {
        String json = "{\"foo\":1,\"bar\":2}";
        JsonValue root = parse(json);

        assertTrue(root.isObject());
        assertEquals(1, root.get("foo").asLong());
        assertEquals(2, root.get("bar").asLong());
        assertNull(root.get("baz")); // key doesn't exist
    }

    @Test
    void testNullValueHandling() throws IOException {
        String json = "{\"value\":null}";
        JsonValue root = parse(json);
        JsonValue value = root.get("value");

        assertTrue(value.isNull());
        assertEquals("null", value.toString());
    }

    @Test
    void testToStringConversion() throws IOException {
        String json = """
            {
              "string": "abc",
              "int": 5,
              "float": 2.5,
              "bool": true,
              "null": null,
              "obj": {},
              "arr": []
            }
            """;
        JsonValue root = parse(json);

        assertEquals("abc", root.get("string").toString());
        assertEquals("5", root.get("int").toString());
        assertEquals("2.5", root.get("float").toString());
        assertEquals("true", root.get("bool").toString());
        assertEquals("null", root.get("null").toString());
        assertEquals("<object>", root.get("obj").toString());
        assertEquals("<array>", root.get("arr").toString());
    }

    @Test
    void testObjectIteration() throws IOException {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        JsonValue root = parse(json);

        Set<String> keys = new HashSet<>();
        Set<Long> values = new HashSet<>();

        for (Iterator<Map.Entry<String, JsonValue>> it = root.objectIterator(); it.hasNext(); ) {
            Map.Entry<String, JsonValue> entry = it.next();
            keys.add(entry.getKey());
            values.add(entry.getValue().asLong());
        }

        assertEquals(Set.of("a", "b", "c"), keys);
        assertEquals(Set.of(1L, 2L, 3L), values);
    }

    @Test
    void testArrayIteration() throws IOException {
        String json = "[\"x\",\"y\",\"z\"]";
        JsonValue root = parse(json);

        List<String> results = new ArrayList<>();
        for (Iterator<JsonValue> it = root.arrayIterator(); it.hasNext(); ) {
            JsonValue val = it.next();
            results.add(val.asString());
        }

        assertEquals(List.of("x", "y", "z"), results);
    }

    @Test
    void testDeeplyNestedStructure() throws IOException {
        String json = """
            {
              "level1": {
                "level2": {
                  "level3": {
                    "value": "deep"
                  }
                }
              }
            }
            """;
        JsonValue root = parse(json);

        assertEquals("deep", root.get("level1")
            .get("level2")
            .get("level3")
            .get("value")
            .asString());
    }

    @Test
    void testEmptyArrayAndObject() throws IOException {
        String json = """
            {
              "emptyArr": [],
              "emptyObj": {}
            }
            """;
        JsonValue root = parse(json);

        assertTrue(root.get("emptyArr").isArray());
        assertEquals(0, root.get("emptyArr").getSize());

        assertTrue(root.get("emptyObj").isObject());
        assertEquals(0, root.get("emptyObj").getSize());
    }

    @Test
    void testPerformanceParsing1KoJsonRepeated60000Times() throws IOException {
        String json = """
            {
              "user": {
                "id": 123,
                "name": "Alice",
                "email": "alice@example.com",
                "roles": ["admin", "user"],
                "active": true,
                "preferences": {
                  "theme": "dark",
                  "notifications": true
                }
              },
              "timestamp": "2025-05-29T12:34:56Z"
            }
            """;

        int iterations = 60_000;

        System.gc();
        long start = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            JsonValue root = parse(json);
            if (i == 0) {
                assertEquals("Alice", root.get("user").get("name").asString());
            }
        }

        long end = System.nanoTime();
        System.out.printf("Parsed %d times in %.2f ms%n", iterations, (end - start) / 1_000_000.0);
    }
}
