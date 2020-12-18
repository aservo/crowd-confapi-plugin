package de.aservo.confapi.crowd.model.util;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.MockDirectoryInternal;
import de.aservo.confapi.commons.model.AbstractDirectoryBean;
import de.aservo.confapi.commons.model.DirectoryInternalBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.crowd.directory.AbstractInternalDirectory.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryBeanUtilTest {

    @Test
    public void testToDirectoryInternalBean() {
        final Directory directory = new MockDirectoryInternal();
        final AbstractDirectoryBean directoryBean = DirectoryBeanUtil.toDirectoryBean(directory);

        assertNotNull(directoryBean);
        assertEquals(directoryBean.getId(), directory.getId());
        assertEquals(directoryBean.getName(), directory.getName());
        assertEquals(directoryBean.getDescription(), directory.getDescription());
        assertEquals(directoryBean.getActive(), directory.isActive());

        final DirectoryInternalBean directoryInternalBean = (DirectoryInternalBean) directoryBean;
        final Map<String, String> attributes = directory.getAttributes();
        assertNotNull(directoryInternalBean.getCredentialPolicy());
        assertEquals(attributes.get(ATTRIBUTE_PASSWORD_REGEX), directoryInternalBean.getCredentialPolicy().getPasswordRegex());
        assertEquals(attributes.get(ATTRIBUTE_PASSWORD_COMPLEXITY_MESSAGE), directoryInternalBean.getCredentialPolicy().getPasswordComplexityMessage());
        assertEquals(Long.valueOf(attributes.get(ATTRIBUTE_PASSWORD_MAX_ATTEMPTS)), directoryInternalBean.getCredentialPolicy().getPasswordMaxAttempts());
        assertNull(directoryInternalBean.getCredentialPolicy().getPasswordHistoryCount());
        assertEquals(Long.valueOf(attributes.get(ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME)), directoryInternalBean.getCredentialPolicy().getPasswordMaxChangeTime());
        assertNotNull(directoryInternalBean.getCredentialPolicy().getPasswordMaxChangeTime());
        assertEquals(attributes.get(ATTRIBUTE_USER_ENCRYPTION_METHOD), directoryInternalBean.getCredentialPolicy().getPasswordEncryptionMethod());
    }

    @Test
    public void testToDirectoryGenericBean() {
        final Directory directory = spy(new MockDirectoryInternal());
        doReturn(DirectoryType.CUSTOM).when(directory).getType();
        final AbstractDirectoryBean directoryBean = DirectoryBeanUtil.toDirectoryBean(directory);

        assertNotNull(directoryBean);
        assertEquals(directoryBean.getId(), directory.getId());
        assertEquals(directoryBean.getName(), directory.getName());
        assertEquals(directoryBean.getDescription(), directory.getDescription());
        assertEquals(directoryBean.getActive(), directory.isActive());
    }

    @Test
    public void testToDirectory() {
        final DirectoryInternalBean directoryBean = DirectoryInternalBean.EXAMPLE_1;
        final Directory directory = DirectoryBeanUtil.toDirectory(directoryBean);

        assertNotNull(directory);
        assertEquals(directory.getName(), directoryBean.getName());

        final Map<String, String> attributes = directory.getAttributes();
        assertNotNull(directory.getAttributes());
        assertEquals(directoryBean.getCredentialPolicy().getPasswordRegex(), attributes.get(ATTRIBUTE_PASSWORD_REGEX));
        assertEquals(directoryBean.getCredentialPolicy().getPasswordComplexityMessage(), attributes.get(ATTRIBUTE_PASSWORD_COMPLEXITY_MESSAGE));
        assertEquals(String.valueOf(directoryBean.getCredentialPolicy().getPasswordMaxAttempts()), attributes.get(ATTRIBUTE_PASSWORD_MAX_ATTEMPTS));
        assertEquals(String.valueOf(directoryBean.getCredentialPolicy().getPasswordHistoryCount()), attributes.get(ATTRIBUTE_PASSWORD_HISTORY_COUNT));
        assertEquals(String.valueOf(directoryBean.getCredentialPolicy().getPasswordMaxChangeTime()), attributes.get(ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME));
        assertNotNull(attributes.get(ATTRIBUTE_PASSWORD_EXPIRATION_NOTIFICATION_PERIODS));
        assertEquals(directoryBean.getCredentialPolicy().getPasswordEncryptionMethod(), attributes.get(ATTRIBUTE_USER_ENCRYPTION_METHOD));
    }

}
