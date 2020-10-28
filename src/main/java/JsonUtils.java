import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static ObjectMapper objectMapper = new CustomObjectMapper();

    public static <T> T inputStreamToObject(InputStream ins, Class<T> clazz) {
        if (ins == null) {
            return null;
        }
        try {
            return objectMapper.readValue(ins, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T mapToObject(Map<?, ?> map, Class<T> clazz) throws IOException {
        return stringToObject(objectToJSONString(map), clazz);
    }

    public static String objectToJSONString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T stringToObject(String jsonString, Class<T> clazz) throws IOException {
        if (jsonString == null) {
            return null;
        }
        return objectMapper.readValue(jsonString, clazz);
    }

    public static <T> T stringToObject(String jsonString, TypeReference<T> type) {
        if (jsonString == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T treeToObject(JsonNode jsonNode, Class<T> clazz) {
        if (jsonNode == null) {
            return null;
        }
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonParser treeAsTokens(TreeNode object) {
        return objectMapper.treeAsTokens(object);
    }

    public static <T> T readValue(JsonParser jsonParser, TypeReference<T> type) {
        try {
            return objectMapper.readValue(jsonParser, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static <T> List<T> stringToList(String jsonString, Class<T> cla) throws IOException {
        if (jsonString == null || "".equals(jsonString)) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(jsonString, getCollectionType(List.class, cla));
    }
}
