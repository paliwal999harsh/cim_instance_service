package com.paliwal999harsh.cloudinstancemanager.microservices.instance.config;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.paliwal999harsh.cloudinstancemanager.dto.InstanceDTO;
import com.paliwal999harsh.cloudinstancemanager.microservices.instance.model.InstanceEntity;

@Mapper(componentModel = "spring")
public interface InstanceMapper {
    
    InstanceDTO entityToDto(InstanceEntity instanceEntity);
    
    @Mappings({
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "instanceId", ignore=true)
    })
    InstanceEntity DtoToEntity(InstanceDTO instanceDTO);

    List<InstanceDTO> entityListToDtoList(List<InstanceEntity> instanceEntities);

    List<InstanceEntity> DtoListToEntityList(List<InstanceDTO> instanceDtos);
}
