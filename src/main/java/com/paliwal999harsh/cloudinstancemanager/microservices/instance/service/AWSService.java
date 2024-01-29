package com.paliwal999harsh.cloudinstancemanager.microservices.instance.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paliwal999harsh.cloudinstancemanager.microservices.instance.model.CloudProvider;
import com.paliwal999harsh.cloudinstancemanager.microservices.instance.model.InstanceEntity;
import com.paliwal999harsh.cloudinstancemanager.microservices.instance.repository.InstanceRepo;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

@Service
public class AWSService implements InstanceService{

    private static Logger log = LoggerFactory.getLogger(AWSService.class);

    @Autowired
    InstanceRepo instanceRepo;

    static Region region = Region.AP_SOUTH_1;
    Ec2Client ec2;
    Ec2Waiter ec2Waiter;
    

    AWSService(){
        ec2 = Ec2Client.builder()
            .region(region)
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();
        ec2Waiter = Ec2Waiter.builder()
            .overrideConfiguration(b -> b.maxAttempts(100))
            .client(ec2)
            .build();
    }

    /**
     * Creates a new AWS EC2 instance.
     *
     * @param instanceName The name of the new instance.
     * @return The created InstanceEntity.
     */
    @Override
    public InstanceEntity createInstance(String instanceName) {
        String amiId = "ami-02a2af70a66af6dfb";
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.T2_MICRO)
                .maxCount(1)
                .minCount(1)
                .build();
        try {
			RunInstancesResponse response = ec2.runInstances(runRequest);
			String instanceId = response.instances().get(0).instanceId();
            Tag tag = Tag.builder()
                    .key("Name")
                    .value(instanceName)
                    .build();
            CreateTagsRequest tagRequest = CreateTagsRequest.builder()
            .resources(instanceId)
            .tags(tag)
            .build();
            ec2.createTags(tagRequest);
			log.info("Successfully started EC2 Instance {} based on AMI {}",
			        instanceId, amiId);
			InstanceEntity instance = new InstanceEntity(instanceName,instanceName,CloudProvider.AWS);
			return instanceRepo.save(instance);
		}
        catch (Ec2Exception e) {
            // Log the exception details
            log.error("AWS EC2 Exception: {}",e.getMessage());
        
            // Rethrow the exception or handle it accordingly
            throw e;
        } 
        catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    /**
     * Retrieves the status of a specific AWS EC2 instance.
     *
     * @param instanceName The name of the instance.
     * @return The status of the specified instance.
     */
    @Override
    public String getInstanceStatus(String instanceName) {
        InstanceEntity instance = instanceRepo.findByInstanceName(instanceName);
        DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
            .instanceIds(instance.getInstanceId())
            .build();
            
        try {
            DescribeInstancesResponse instancesResponse = ec2.describeInstances(instanceRequest);
            String state = instancesResponse.reservations().get(0).instances().get(0).state().name().name();
            return state;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves information about all AWS EC2 instances.
     *
     * @return A list of all InstanceEntity objects.
     */
    @Override
    public List<InstanceEntity> getAllInstances() {
        String nextToken = null;
        try{
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response;
				try {
                    List<InstanceEntity> instances = new ArrayList<>();
					response = ec2.describeInstances(request);
					for (Reservation reservation : response.reservations()) {
                        instances.addAll(reservation.instances().stream().map(instance -> {
                            return new InstanceEntity(instance.instanceId(), instance.publicDnsName(), CloudProvider.AWS);
                        }).collect(Collectors.toList()));
                    }                
                    nextToken = response.nextToken();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
        }
        log.info("Instance Data in DB {}",instanceRepo.count());
        return instanceRepo.findAll();
    }

    /**
     * Stops an AWS EC2 instance.
     *
     * @param instanceName The name of the instance to stop.
     */
    @Override
    public void stopInstance(String instanceName) {
        InstanceEntity instance = instanceRepo.findByInstanceName(instanceName);
        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instance.getInstanceId())
                .build();

        ec2.stopInstances(request);
        DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
            .instanceIds(instance.getInstanceId())
            .build();
            
        WaiterResponse<DescribeInstancesResponse> waiterResponse;
        try {
            waiterResponse = ec2Waiter.waitUntilInstanceStopped(instanceRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            log.info("Successfully stopped instance {}",instance.getInstanceId());
        } catch (Exception e) {
            log.atError().notify();
            log.error("Unable to stop the instance, Exception Occureed:",e);
        }
    }

    /**
     * Starts an AWS EC2 instance.
     *
     * @param instanceName The name of the instance to start.
     */
    @Override
    public void startInstance(String instanceName) {
        InstanceEntity instance = instanceRepo.findByInstanceName(instanceName);
        StartInstancesRequest request = StartInstancesRequest.builder()
                .instanceIds(instance.getInstanceId())
                .build();

        ec2.startInstances(request);
        DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
            .instanceIds(instance.getInstanceId())
            .build();
            
        WaiterResponse<DescribeInstancesResponse> waiterResponse;
        try {
            waiterResponse = ec2Waiter.waitUntilInstanceRunning(instanceRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            log.info("Successfully started instance {}",instance.getInstanceId());
        } catch (Exception e) {
            log.atError().notify();
            log.error("Unable to start the instance, Exception Occureed:",e);
        }
    }
    
    
}
