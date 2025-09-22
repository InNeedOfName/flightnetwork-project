package org.pi2;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.pi2.model.ModelTest;
import org.pi2.service.api.APITest;
import org.pi2.service.api.EndpointsTest;
import org.pi2.service.data.CsvLoaderTest;
import org.pi2.service.data.DatabaseManagerTest;
import org.pi2.service.planning.TripPlannerTest;


@Suite
@SelectClasses({
        //Testing services
        CsvLoaderTest.class,
        DatabaseManagerTest.class,
        EndpointsTest.class,
        TripPlannerTest.class,
        //Testing models
        ModelTest.class,
        //Testing main parts
        APITest.class,
        MainTest.class,

})
public class TotalTest {
}