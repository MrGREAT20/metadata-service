package com.flairlabs.workflow.services.metadata.metadata_service.repositories;

import com.flairlabs.workflow.services.metadata.metadata_service.models.RuntimeChangelog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IRuntimeChangelogRepository extends JpaRepository<RuntimeChangelog, UUID> {
}
