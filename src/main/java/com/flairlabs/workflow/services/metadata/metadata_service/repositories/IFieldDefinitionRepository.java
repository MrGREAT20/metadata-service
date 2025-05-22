package com.flairlabs.workflow.services.metadata.metadata_service.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;

@Repository
public interface IFieldDefinitionRepository extends JpaRepository<FieldDefinition, UUID> {
    boolean existsByName(String name);

}
