package com.paliwal999harsh.cloudinstancemanager.microservices.instance.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.paliwal999harsh.cloudinstancemanager.microservices.instance.model.InstanceEntity;

@Repository
public interface InstanceRepo extends MongoRepository<InstanceEntity, Long>{

    InstanceEntity findByInstanceName(String instanceName);

}
