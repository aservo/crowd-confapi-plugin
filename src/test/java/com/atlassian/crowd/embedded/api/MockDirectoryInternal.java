package com.atlassian.crowd.embedded.api;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.atlassian.crowd.directory.AbstractInternalDirectory.*;
import static com.atlassian.crowd.password.factory.PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER;

public class MockDirectoryInternal implements Directory {

    public static final String ATTRIBUTE_PASSWORD_REGEX_VALUE                           = ".*";
    public static final String ATTRIBUTE_PASSWORD_COMPLEXITY_MESSAGE_VALUE              = "complex";
    public static final long   ATTRIBUTE_PASSWORD_MAX_ATTEMPTS_VALUE                    = 10L;
    public static final String ATTRIBUTE_PASSWORD_HISTORY_COUNT_VALUE                   = null;
    public static final long   ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME_VALUE                 = 60L;
    public static final List<String> ATTRIBUTE_PASSWORD_EXPIRATION_NOTIFICATION_PERIODS_VALUE = Arrays.asList("1", "7");

    private final Map<String, String> attributes;
    private final Set<OperationType> allowedOperations;

    public MockDirectoryInternal() {
        this.attributes = Stream.of(new String[][] {
                { ATTRIBUTE_PASSWORD_REGEX, ATTRIBUTE_PASSWORD_REGEX_VALUE },
                { ATTRIBUTE_PASSWORD_COMPLEXITY_MESSAGE, ATTRIBUTE_PASSWORD_COMPLEXITY_MESSAGE_VALUE },
                // don't add ATTRIBUTE_PASSWORD_MAX_ATTEMPTS attribute
                { ATTRIBUTE_PASSWORD_MAX_ATTEMPTS, String.valueOf(ATTRIBUTE_PASSWORD_MAX_ATTEMPTS_VALUE) },
                { ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME, String.valueOf(ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME_VALUE) },
                { ATTRIBUTE_PASSWORD_EXPIRATION_NOTIFICATION_PERIODS, String.join(",", ATTRIBUTE_PASSWORD_EXPIRATION_NOTIFICATION_PERIODS_VALUE) },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        this.allowedOperations = new HashSet<>();
    }

    @Override
    public Long getId() {
        return 1L;
    }

    @Override
    public String getName() {
        return "Mock Directory";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getEncryptionType() {
        return ATLASSIAN_SECURITY_ENCODER;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public Set<OperationType> getAllowedOperations() {
        return allowedOperations;
    }

    @Override
    public DirectoryType getType() {
        return DirectoryType.INTERNAL;
    }

    @Override
    public String getImplementationClass() {
        return null;
    }

    @Override
    public Date getCreatedDate() {
        return null;
    }

    @Override
    public Date getUpdatedDate() {
        return null;
    }

    @Nullable
    @Override
    public Set<String> getValues(String key) {
        final String value = getValue(key);

        if (value != null) {
            return Collections.singleton(value);
        }

        return null;
    }

    @Nullable
    @Override
    public String getValue(String key) {
        return attributes.get(key);
    }

    @Override
    public Set<String> getKeys() {
        return attributes.keySet();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

}
