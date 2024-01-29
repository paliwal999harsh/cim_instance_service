package com.paliwal999harsh.cloudinstancemanager.microservices.instance.events;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.paliwal999harsh.cloudinstancemanager.dto.LeaseDTO;
import com.paliwal999harsh.cloudinstancemanager.microservices.instance.model.InstanceEntity;
import com.paliwal999harsh.cloudinstancemanager.util.SequenceGenerator;

@Component
public class InstanceModelListener extends AbstractMongoEventListener<InstanceEntity> {

    private RestTemplate restTemplate;

    @Autowired
    private SequenceGenerator sequenceGenerator;

    InstanceModelListener(){
        restTemplate = new RestTemplate();
    }
    @Override
    public void onBeforeConvert(BeforeConvertEvent<InstanceEntity> event) {
        if (event.getSource().getId() == null) {
            event.getSource().setId(sequenceGenerator.generateSequence(InstanceEntity.SEQUENCE_NAME));
        }
    }

    @Override
    public void onAfterSave(AfterSaveEvent<InstanceEntity> event){
        String leaseServiceUrl = "http://lease-service:8080/cim/api/v1/lease";
        restTemplate.postForEntity(leaseServiceUrl, event.getSource(), LeaseDTO.class);
    }
}

