package com.paliwal999harsh.cloudinstancemanager.microservices.instance.service;

import java.util.List;

import com.paliwal999harsh.cloudinstancemanager.microservices.instance.model.InstanceEntity;
/**
 * Service interface for managing instances.
 */
public interface InstanceService {
    
    /**
     * Creates a new instance.
     *
     * @param instanceName The name of the new instance.
     * @return The created InstanceEntity.
     */
    InstanceEntity createInstance(String instanceName);

    /**
     * Retrieves information about all instances.
     *
     * @return A list of all InstanceEntity objects.
     */
    List<InstanceEntity> getAllInstances();

    /**
     * Stops an instance.
     *
     * @param instanceName The name of the instance to stop.
     */
    void stopInstance(String instanceName);

    /**
     * Starts an instance.
     *
     * @param instanceName The name of the instance to start.
     */
    void startInstance(String instanceName);

    /**
     * Retrieves the status of a specific instance.
     *
     * @param instanceName The name of the instance.
     * @return The status of the specified instance.
     */
    String getInstanceStatus(String instanceName);
}
