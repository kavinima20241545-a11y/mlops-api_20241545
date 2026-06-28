package com.mlops.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
Extended Class
 */
@ApplicationPath("/api/v1")
public class MLOpsApplication extends Application {
    // Jersey auto-scans for @Provider and @Path classes when using its
    // ResourceConfig or package scanning – no need to list them manually here.
}
