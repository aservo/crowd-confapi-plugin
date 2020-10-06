package de.aservo.confapi.crowd.service;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.MockDirectoryInternal;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import de.aservo.confapi.commons.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DirectoriesServiceTest {

    @Mock
    private DirectoryManager directoryManager;

    private DirectoriesServiceImpl directoriesService;

    @Before
    public void setup() {
        directoriesService = new DirectoriesServiceImpl(directoryManager);
    }

    @Test
    public void testGetDirectories() throws DirectoryNotFoundException {
        doReturn(Collections.singletonList(new MockDirectoryInternal())).when(directoryManager).searchDirectories(any(EntityQuery.class));
        assertNotNull(directoriesService.getDirectories());
    }

    @Test
    public void testGetDirectory() throws DirectoryNotFoundException {
        doReturn(new MockDirectoryInternal()).when(directoryManager).findDirectoryById(anyLong());
        assertNotNull(directoriesService.getDirectory(1L));
    }

    @Test(expected = NotFoundException.class)
    public void testGetDirectoryNotFoundException() throws DirectoryNotFoundException {
        doThrow(new DirectoryNotFoundException("Directory")).when(directoryManager).findDirectoryById(anyLong());
        assertNull(directoriesService.getDirectory(1L));
    }

    @Test
    public void testDirectoryComparator() {
        final List<Directory> directories = new ArrayList<>();

        {
            final Directory directoryInternal1 = mock(Directory.class);
            doReturn(1L).when(directoryInternal1).getId();
            doReturn("Old Internal Directory").when(directoryInternal1).getName();
            doReturn(DirectoryType.INTERNAL).when(directoryInternal1).getType();
            doReturn(toDate(LocalDate.now().minusDays(3))).when(directoryInternal1).getCreatedDate();
            directories.add(directoryInternal1);
        }

        {
            final Directory directoryAzureAd = mock(Directory.class);
            doReturn(2L).when(directoryAzureAd).getId();
            doReturn("Azure AD Directory").when(directoryAzureAd).getName();
            doReturn(DirectoryType.AZURE_AD).when(directoryAzureAd).getType();
            doReturn(toDate(LocalDate.now().minusDays(1))).when(directoryAzureAd).getCreatedDate();
            directories.add(directoryAzureAd);
        }

        {
            final Directory directoryInternal2 = mock(Directory.class);
            doReturn(3L).when(directoryInternal2).getId();
            doReturn("New Internal Directory").when(directoryInternal2).getName();
            doReturn(DirectoryType.INTERNAL).when(directoryInternal2).getType();
            doReturn(toDate(LocalDate.now())).when(directoryInternal2).getCreatedDate();
            directories.add(directoryInternal2);
        }

        directories.sort(new DirectoriesServiceImpl.DirectoryComparator());

        assertEquals(2L, (long) directories.get(0).getId());
        assertEquals(3L, (long) directories.get(1).getId());
        assertEquals(1L, (long) directories.get(2).getId());
    }

    private Date toDate(
            final LocalDate localDate) {

        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
