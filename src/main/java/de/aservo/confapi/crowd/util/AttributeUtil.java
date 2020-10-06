package de.aservo.confapi.crowd.util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributeUtil {

    public static final String LIST_SEPARATOR = ",";

    @Nullable
    public static String fromLong(
            @Nullable final Long value) {

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }

    @Nullable
    public static String fromBoolean(
            @Nullable final Boolean value) {

        if (value == null) {
            return null;
        }

        return String.valueOf(value.booleanValue());
    }

    public static String fromIntegerList(
            @Nullable final List<Integer> value) {

        if (value == null) {
            return null;
        }

        return value.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(LIST_SEPARATOR));
    }

    @Nullable
    public static Long toLong(
            @Nullable final String value) {

        if (value == null) {
            return null;
        }

        return Long.valueOf(value);
    }

    @Nullable
    public static Boolean toBoolean(
            @Nullable final String value) {

        if (value == null) {
            return null;
        }

        return Boolean.valueOf(value);
    }

    @Nullable
    public static List<Integer> toIntegerList(
            @Nullable final String value) {

        if (value == null) {
            return null;
        }

        return Stream.of(value.split(LIST_SEPARATOR))
                .sorted()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    private AttributeUtil() {}
    
}
