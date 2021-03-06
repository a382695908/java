package com.jsoniter;

import com.jsoniter.spi.Binding;
import com.jsoniter.spi.ClassDescriptor;
import com.jsoniter.spi.JsoniterSpi;
import com.jsoniter.spi.TypeLiteral;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;

public class TestGenerics extends TestCase {

    static {
//        JsonIterator.setMode(DecodingMode.REFLECTION_MODE);
    }

    public void test_int_list() throws IOException {
        JsonIterator iter = JsonIterator.parse("[1,2,3]");
        List<Integer> val = iter.read(new TypeLiteral<ArrayList<Integer>>() {
        });
        assertArrayEquals(new Integer[]{1, 2, 3}, val.toArray(new Integer[0]));
    }

    public void test_string_list() throws IOException {
        JsonIterator iter = JsonIterator.parse("['hello', 'world']".replace('\'', '"'));
        List<String> val = iter.read(new TypeLiteral<List<String>>() {
        });
        assertArrayEquals(new String[]{"hello", "world"}, val.toArray(new String[0]));
    }

    public void test_linked_list() throws IOException {
        JsonIterator iter = JsonIterator.parse("['hello', 'world']".replace('\'', '"'));
        List<String> val = iter.read(new TypeLiteral<LinkedList<String>>() {
        });
        assertArrayEquals(new String[]{"hello", "world"}, val.toArray(new String[0]));
    }

    public void test_string_set() throws IOException {
        JsonIterator iter = JsonIterator.parse("['hello']".replace('\'', '"'));
        Set<String> val = iter.read(new TypeLiteral<Set<String>>() {
        });
        assertArrayEquals(new String[]{"hello"}, val.toArray(new String[0]));
    }

    public void test_string_map() throws IOException {
        JsonIterator iter = JsonIterator.parse("{'hello': 'world'}".replace('\'', '"'));
        Map<String, String> val = iter.read(new TypeLiteral<Map<String, String>>() {
        });
        assertEquals("world", val.get("hello"));
    }

    public void test_list_of_list() throws Exception {
        JsonIterator iter = JsonIterator.parse("[[1,2],[3,4]]");
        List<List<Integer>> listOfList = iter.read(new TypeLiteral<List<List<Integer>>>() {
        });
        assertEquals(Integer.valueOf(4), listOfList.get(1).get(1));
    }

    public void test_complex_object() throws IOException {
        JsonIterator iter = JsonIterator.parse("{'field1': 100, 'field2': [[1,2],[3,4]]}".replace('\'', '"'));
        ComplexObject val = iter.read(ComplexObject.class);
        assertEquals(100, val.field1);
        assertEquals(Integer.valueOf(4), val.field2.get(1).get(1));
    }

    public static class Class1<A, B> {
        public List<A> field1;
        public B[] field2;
        public List<B>[] field3;
        public List<A[]> field4;
        public List<Map<A, List<B>>> getField6() {
            return null;
        }
        public <T> T getField7() {
            return null;
        }
        public void setField8(List<A> a) {
        }
    }

    public static class Class2<C, D, E> extends Class1<C, D> {
        public E field5;
    }

    public static class Class3 extends Class2<String, Integer, Float> {
    }

    public void test_generic_super_class() throws IOException {
        ClassDescriptor desc = JsoniterSpi.getDecodingClassDescriptor(Class3.class, true);
        Map<String, String> fieldDecoderCacheKeys = new HashMap<String, String>();
        for (Binding field : desc.allDecoderBindings()) {
            fieldDecoderCacheKeys.put(field.name, field.valueTypeLiteral.getDecoderCacheKey());
        }
        for (Binding field : JsoniterSpi.getEncodingClassDescriptor(Class3.class, true).getters) {
            fieldDecoderCacheKeys.put(field.name, field.valueTypeLiteral.getDecoderCacheKey());
        }
        assertEquals(new HashMap<String, String>() {{
            put("field1", "decoder.java.util.List_java.lang.String");
            put("field2", "decoder.java.lang.Integer_array");
            put("field3", "decoder.java.util.List_java.lang.Integer_array");
            put("field4", "decoder.java.util.List_java.lang.String_array");
            put("field5", "decoder.java.lang.Float");
            put("field6", "decoder.java.util.List_java.util.Map_java.lang.String_java.util.List_java.lang.Integer");
            put("field7", "decoder.java.lang.Object");
            put("field8", "decoder.java.util.List_java.lang.String");
        }}, fieldDecoderCacheKeys);
    }
}
