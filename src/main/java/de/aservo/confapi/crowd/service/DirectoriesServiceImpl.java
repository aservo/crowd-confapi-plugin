package de.aservo.confapi.crowd.service;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import de.aservo.confapi.commons.exception.BadRequestException;
import de.aservo.confapi.commons.exception.InternalServerErrorException;
import de.aservo.confapi.commons.exception.NotFoundException;
import de.aservo.confapi.commons.exception.ServiceUnavailableException;
import de.aservo.confapi.commons.model.AbstractDirectoryBean;
import de.aservo.confapi.commons.model.DirectoriesBean;
import de.aservo.confapi.commons.model.DirectoryInternalBean;
import de.aservo.confapi.commons.service.api.DirectoriesService;
import de.aservo.confapi.crowd.model.util.DirectoryBeanUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.atlassian.crowd.embedded.api.DirectoryType.INTERNAL;
import static de.aservo.confapi.crowd.model.util.DirectoryBeanUtil.toDirectory;

@Component
@ExportAsService(DirectoriesService.class)
public class DirectoriesServiceImpl implements DirectoriesService {

    private static final int RETRY_AFTER_IN_SECONDS = 5;

    @ComponentImport
    private final DirectoryManager directoryManager;

    @Inject
    public DirectoriesServiceImpl(
            final DirectoryManager directoryManager) {

        this.directoryManager = directoryManager;
    }

    @Override
    public DirectoriesBean getDirectories() {
        return new DirectoriesBean(findAllDirectories().stream()
                .map(DirectoryBeanUtil::toDirectoryBean)
                .collect(Collectors.toList())
        );
    }

    @Override
    public AbstractDirectoryBean getDirectory(
            final long id) {

        final Directory directory = findDirectory(id);
        return DirectoryBeanUtil.toDirectoryBean(directory);
    }

    @Override
    public DirectoriesBean setDirectories(
            @NotNull final DirectoriesBean directoriesBean,
            final boolean testConnection) {

        final Map<String, Directory> existingDirectoriesByName = findAllDirectories().stream()
                .collect(Collectors.toMap(Directory::getName, Function.identity()));

        for (AbstractDirectoryBean directoryBean : directoriesBean.getDirectories()) {
            if (existingDirectoriesByName.containsKey(directoryBean.getName())) {
                setDirectory(existingDirectoriesByName.get(directoryBean.getName()).getId(), directoryBean, testConnection);
            } else {
                addDirectory(directoryBean, testConnection);
            }
        }

        return getDirectories();
    }

    @Override
    public AbstractDirectoryBean setDirectory(
            final long id,
            @NotNull final AbstractDirectoryBean directoryBean,
            final boolean testConnection) {

        if (!(directoryBean instanceof DirectoryInternalBean)) {
            throw new BadRequestException(String.format(
                    "Setting directory type '%s' is not supported (yet)", directoryBean.getClass()));
        }

        final Directory existingDirectory = findDirectory(id);

        try {
            final Directory mergedDirectory = toDirectory(directoryBean, existingDirectory);
            final Directory updatedDirectory = directoryManager.updateDirectory(mergedDirectory);
            return DirectoryBeanUtil.toDirectoryInternalBean(updatedDirectory);
        } catch (DirectoryNotFoundException e) {
            // this should not happen
            throw new InternalServerErrorException(String.format(
                    "When trying to update directory '%s', it could not be found anymore", existingDirectory.getName()));
        }
    }

    @Override
    public AbstractDirectoryBean addDirectory(
            final @NotNull AbstractDirectoryBean directoryBean,
            final boolean testConnection) {

        if (!(directoryBean instanceof DirectoryInternalBean)) {
            throw new BadRequestException(String.format(
                    "Adding directory type '%s' is not supported (yet)", directoryBean.getClass()));
        }

        try {
            final Directory directory = toDirectory(directoryBean);
            final Directory addedDirectory = directoryManager.addDirectory(directory);
            return DirectoryBeanUtil.toDirectoryBean(addedDirectory);
        } catch (DirectoryInstantiationException e) {
            throw new InternalServerErrorException(String.format("Could not create directory '%s'", directoryBean.getName()));
        }
    }

    @Override
    public void deleteDirectories(
            final boolean force) {

        if (!force) {
            throw new BadRequestException("'force = true' must be supplied to delete all entries");
        }

        final Collection<Directory> directories = findAllDirectories();

        directories.stream()
                .sorted(Comparator.comparing(Directory::getCreatedDate))
                .limit(directories.size() - 1L)
                .forEach(d -> deleteDirectory(d.getId()));
    }

    @Override
    public void deleteDirectory(
            final long id) {

        if (findAllDirectories().size() <= 1) {
            throw new BadRequestException("Cannot delete directory '%s' as this is the last remaining directory");
        }

        final Directory directory = findDirectory(id);

        try {
            directoryManager.removeDirectory(directory);
        } catch (DirectoryNotFoundException e) {
            throw new InternalServerErrorException(String.format(
                    "When trying to update directory '%s', it could not be found anymore", directory.getName()));
        } catch (DirectoryCurrentlySynchronisingException e) {
            throw new ServiceUnavailableException(e, RETRY_AFTER_IN_SECONDS);
        }
    }

    @Nonnull
    private Directory findDirectory(
            final long id) {

        try {
            return directoryManager.findDirectoryById(id);
        } catch (DirectoryNotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    @Nonnull
    private Collection<Directory> findAllDirectories() {
        final EntityQuery<Directory> allDirectoriesEntityQuery = QueryBuilder
                .queryFor(Directory.class, EntityDescriptor.directory())
                .returningAtMost(EntityQuery.ALL_RESULTS);

        return directoryManager.searchDirectories(allDirectoriesEntityQuery);
    }

    /**
     * This comparator allows sorting directories as follows:
     *
     * - Non-internal directories come first
     * - For the same directory types, directories with more recent creation date come first
     *
     * This comparator is used for the "delete-all" method in order to make sure that the oldest internal directory
     * will never be deleted in order not to lock out from Crowd.
     */
    static class DirectoryComparator implements Comparator<Directory> {
        @Override
        public int compare(Directory d1, Directory d2) {
            if (!d1.getType().equals(INTERNAL) && d2.getType().equals(INTERNAL)) return -1;
            if (d1.getType().equals(INTERNAL) && !d2.getType().equals(INTERNAL)) return 1;
            return d2.getCreatedDate().compareTo(d1.getCreatedDate());
        }
    }

}
