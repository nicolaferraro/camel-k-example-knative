package org.acme;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.inject.Inject;

@Path("/samples")
public class MachineLearningResource {

    @Inject
    private SimpleAlgorithm algorithm;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Action appendAndPredict(double value) {
        System.out.println("Received " + value);
        
        Action res = algorithm.predict(value);
        System.out.println("Result is " + res);
        
        return res;
    }

}
