package de.aservo.confapi.crowd.service;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import de.aservo.confapi.commons.exception.NotFoundException;
import de.aservo.confapi.commons.model.AbstractDirectoryBean;
import de.aservo.confapi.commons.model.DirectoriesBean;
import de.aservo.confapi.crowd.model.util.DirectoryBeanUtil;
import de.aservo.confapi.crowd.service.api.DirectoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;

@Component
@ExportAsService(DirectoriesService.class)
public class DirectoriesServiceImpl implements DirectoriesService {

    private static final Logger log = LoggerFactory.getLogger(DirectoriesServiceImpl.class);

    @ComponentImport
    private final DirectoryManager directoryManager;

    @Inject
    public DirectoriesServiceImpl(
            final DirectoryManager directoryManager) {

        this.directoryManager = directoryManager;
    }

    @Override
    public DirectoriesBean getDirectories() {
        final EntityQuery<Directory> directoryEntityQuery = QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory())
                .returningAtMost(EntityQuery.ALL_RESULTS);

        return new DirectoriesBean(directoryManager.searchDirectories(directoryEntityQuery).stream()
                .filter(d -> d.getType().equals(DirectoryType.INTERNAL))
                .map(DirectoryBeanUtil::toDirectoryBean)
                .collect(Collectors.toList())
        );
    }

    @Override
    public AbstractDirectoryBean getDirectory(
            final long id) {

        try {
            final Directory directory = directoryManager.findDirectoryById(id);
            return DirectoryBeanUtil.toDirectoryBean(directory);
        } catch (DirectoryNotFoundException e) {
            throw new NotFoundException(e);
        }
    }
}
