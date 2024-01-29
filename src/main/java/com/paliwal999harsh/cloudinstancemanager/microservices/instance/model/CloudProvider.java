package com.paliwal999harsh.cloudinstancemanager.microservices.instance.model;

import lombok.Data;

@Data
public class CloudProvider {
    public final static String Azure = "Azure";
    public final static String AWS = "AWS";

    public static Boolean isValid(String value) {
        return value.equals(Azure) || value.equals(AWS);
    }
}
