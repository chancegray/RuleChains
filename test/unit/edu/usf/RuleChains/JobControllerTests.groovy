package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(JobController)
@Mock([JobService])
class JobControllerTests {

    void testListChainJobs() {
        controller.request.method = "GET"
        def control = mockFor(JobService)
        control.demand.listChainJobs { -> 
            return [
                jobGroups: [
                    [
                        name: 'default',
                        jobs: [
                            [
                                name: 'test1',
                                triggers: '0 0 0 0 ? 2014',
                                chain: 'testchain',
                                input: []
                            ]
                        ]
                    ]
                ]
            ]
        }
        controller.jobService = control.createMock()

        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listChainJobs()
        assert model.jobGroups[0].name == "default"
    }
    
    void testCreateChainJob() {
        controller.params << [
            cronExpression: "0 0 0 0 ? 2015",
            name: "newChainJob",
            input: []
        ]
        controller.request.method = "PUT"
        JobService.metaClass.createChainJob = { String cronExpression,String name,def input = [] -> }
        def control = mockFor(JobService)
        control.demand.createChainJob { cronExpression,name,input -> 
            return [
                date: System.currentTimeMillis()
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.createChainJob()
        assert model.date <= System.currentTimeMillis()        
    }
    
    void testRemoveChainJob() {
        controller.params.name = "testJob"
        controller.request.method = "DELETE"
        JobService.metaClass.removeChainJob = { String name -> }
        def control = mockFor(JobService)
        control.demand.removeChainJob { name ->
            return [
                status: [ 
                    [
                        jobName: name,
                        jobGroup: 'default',
                        removed: System.currentTimeMillis()
                    ] 
                ]
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.removeChainJob()
        assert model.status[0].jobName == "testJob"      
    }
    void testUnscheduleChainJob() {
        controller.params << [
            name: "testJob",
            cronExpression: "0 0 0 0 ? 2014"
        ]
        controller.request.method = "DELETE"
        JobService.metaClass.unscheduleChainJob = { String cronExpression, String name -> }
        def control = mockFor(JobService)
        control.demand.unscheduleChainJob { cronExpression,name ->
            def jobsMock = [
                [
                    jobName: "testJob",
                    jobGroup: "default",
                    triggers: ["0 0 0 0 ? 2014","0 0 0 0 ? 2015"]
                ]
            ]
            return [ 
                status: [ jobsMock.find { it.jobName == "testJob" }.inject([:]) {m,k,v ->
                    switch(k) {
                        case 'triggers':
                            assert v.findAll { it != cronExpression }.size() < 2
                            m["removed"] = System.currentTimeMillis()
                            break
                        default:
                            m[k] = v
                            break
                    }
                    return m
                } ]
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.unscheduleChainJob()
        assert model.status[0].jobName == "testJob"      
    }
}
