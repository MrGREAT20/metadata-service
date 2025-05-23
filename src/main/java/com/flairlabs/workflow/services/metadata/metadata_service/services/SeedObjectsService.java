package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntityRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.FieldRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.EntityType;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldDataType;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SeedObjectsService {

    public List<EntityRequestDto> getOnProvisonObjects(){
        List<EntityRequestDto> results = new ArrayList<>();

        FieldRequestDto leadsIdTable = new FieldRequestDto();
        leadsIdTable.setFieldName("id");
        leadsIdTable.setFieldType(FieldType.PRIMARY_KEY);
        leadsIdTable.setFieldDataType(FieldDataType.UUID);
        leadsIdTable.setAutoGenerate(true);
        leadsIdTable.setRequired(true);

        FieldRequestDto leadsNameTable = new FieldRequestDto();
        leadsNameTable.setFieldName("name");
        leadsNameTable.setFieldType(FieldType.COLUMN);
        leadsNameTable.setFieldDataType(FieldDataType.TEXT);
        leadsNameTable.setRequired(true);

        EntityRequestDto leads = new EntityRequestDto("leads", "This is a core table", Arrays.asList(leadsIdTable, leadsNameTable), EntityType.CORE);
        results.add(leads);
        return results;
    }

}
