package com.flairlabs.workflow.services.metadata.metadata_service.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;

@Repository
public interface IEntityDefinitionRepository extends JpaRepository<EntityDefinition, UUID> {
    boolean existsByName(String name);
}
