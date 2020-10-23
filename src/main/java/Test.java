import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : gaozhiwen
 * @date : 2020/10/14
 */
public class Test {
    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            strings.add(String.valueOf(i));
        }
        System.out.println(strings);
        System.out.println(strings.subList(0, strings.size() > 10 ? 10 : strings.size()));
    }

}
