package com.flairlabs.workflow.services.metadata.metadata_service.models;

import com.flairlabs.workflow.services.metadata.metadata_service.multitenant.TenantBaseModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "runtime_changelog", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeChangelog extends TenantBaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;

    @Column(name = "changelog_xml")
    private String changelogXml;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
