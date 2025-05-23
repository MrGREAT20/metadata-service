package com.flairlabs.workflow.services.metadata.metadata_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flairlabs.workflow.services.metadata.metadata_service.multitenant.TenantBaseModel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "field_definition", uniqueConstraints = {
        @UniqueConstraint(name = "uk_field_entity_name", columnNames = {
                "entity_id",
                "name"
        })
}, schema = "master")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinition extends TenantBaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    @JsonIgnore
    private EntityDefinition entity;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(name = "field_data_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FieldDataType fieldDataType;

    @Column(name = "field_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "auto_generate")
    private Boolean autoGenerate;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "default_value")
    private String defaultValue;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Only for FOREIGN_KEY
    @Column(name = "reference_entity_id")
    private String referenceEntityId;

    @Column(name = "reference_field_id")
    private String referenceFieldId;

    public enum FieldDataType {
        TEXT,
        NUMBER,
        BOOLEAN,
        DATE,
        DATETIME,
        EMAIL,
        PHONE,
        URL,
        DECIMAL,
        UUID
    }

    public enum FieldType {
        COLUMN,
        FOREIGN_KEY,
        PRIMARY_KEY
    }

}
