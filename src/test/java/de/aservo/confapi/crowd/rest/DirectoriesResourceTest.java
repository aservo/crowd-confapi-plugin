package de.aservo.confapi.crowd.rest;

import de.aservo.confapi.commons.model.DirectoriesBean;
import de.aservo.confapi.commons.model.DirectoryInternalBean;
import de.aservo.confapi.commons.service.api.DirectoriesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class DirectoriesResourceTest {

    @Mock
    private DirectoriesService directoriesService;

    private DirectoriesResourceImpl directoriesResource;

    @Before
    public void setup() {
        directoriesResource = new DirectoriesResourceImpl(directoriesService);
    }

    @Test
    public void testGetDirectories() {
        final DirectoriesBean directoriesBean = new DirectoriesBean(Collections.singletonList(DirectoryInternalBean.EXAMPLE_1));
        doReturn(directoriesBean).when(directoriesService).getDirectories();

        final Response response = directoriesResource.getDirectories();
        assertEquals(200, response.getStatus());

        final DirectoriesBean responseDirectoriesBean = (DirectoriesBean) response.getEntity();
        assertEquals(directoriesBean, responseDirectoriesBean);
    }

    @Test
    public void testGetDirectory() {
        final DirectoryInternalBean directoryBean = DirectoryInternalBean.EXAMPLE_1;
        doReturn(directoryBean).when(directoriesService).getDirectory(anyLong());

        final Response response = directoriesResource.getDirectory(directoryBean.getId());
        assertEquals(200, response.getStatus());

        final DirectoryInternalBean responseDirectoryBean = (DirectoryInternalBean) response.getEntity();
        assertEquals(directoryBean, responseDirectoryBean);
    }

}
