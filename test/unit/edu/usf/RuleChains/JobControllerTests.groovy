package edu.usf.RuleChains



import grails.test.mixin.*
import org.junit.*
import org.hibernate.criterion.CriteriaSpecification
import groovy.time.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(JobController)
@Mock([JobService,JobHistory,JobLog])
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
    
    void testRescheduleChainJob() {
        controller.params << [
            cronExpression: "0 0 0 0 ? 2014",
            cron: "0 0 0 0 ? 2015",
            name: "testJob"
        ]
        controller.request.method = "POST"
        JobService.metaClass.rescheduleChainJob = { String cronExpression, String cron, String name -> }
        def control = mockFor(JobService)
        control.demand.rescheduleChainJob { cronExpression,cron,name ->
            def jobsMock = [
                [
                    jobName: "testJob",
                    jobGroup: "default",
                    triggers: ["0 0 0 0 ? 2014"]
                ]
            ]
            return [ 
                status: [ jobsMock.find { it.jobName == name }.inject([:]) {m,k,v ->
                    switch(k) {
                        case 'triggers':
                            assert v.findAll { it == cronExpression }.size() < 2
                            m["scheduled"] = new Date()
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
        def model = controller.rescheduleChainJob()
        assert model.status[0].jobName == "testJob"              
        assert model.status[0].scheduled <= new Date()              
    }
    
    void testUpdateChainJob() {
        controller.params << [
            name: "testJob", 
            newName: "renamedTestJob"
        ]
        controller.request.method = "POST"
        JobService.metaClass.updateChainJob = { String name, String newName -> }
        def control = mockFor(JobService)
        control.demand.updateChainJob { name,newName ->
            def jobsMock = [
                [
                    jobName: "testJob",
                    jobGroup: "default",
                    triggers: ["0 0 0 0 ? 2014"]
                ]
            ]
            return [
                updated: (jobsMock.find { it.jobName == name }.jobName != newName)
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.updateChainJob()
        assert model.updated == true                      
    }
    
    void testAddscheduleChainJob() {
        controller.params << [
            cronExpression: "0 0 0 0 ? 2015",
            name: "testJob"
        ]
        controller.request.method = "PUT"
        JobService.metaClass.addscheduleChainJob = { String cronExpression, String name -> }
        def control = mockFor(JobService)
        control.demand.addscheduleChainJob { cronExpression,name -> 
            def jobsMock = [
                [
                    jobName: "testJob",
                    jobGroup: "default",
                    triggers: ["0 0 0 0 ? 2014"]
                ]
            ]        
            return [
                date: { t ->
                    t << cronExpression
                    assert t.size() > 1
                    return new Date()
                }.call(jobsMock.find { it.jobName == name }.triggers)
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.addscheduleChainJob()
        assert model.date <= new Date()                              
    }
    
    void testMergescheduleChainJob() {
        controller.params << [
            mergeName: "testJob",
            name: "testJobDup"
        ]
        controller.request.method = "POST"
        JobService.metaClass.mergescheduleChainJob = { String mergeName, String name -> }
        def control = mockFor(JobService)
        control.demand.mergescheduleChainJob { mergeName,name -> 
            def jobsMock = [
                [
                    jobName: "testJob",
                    jobGroup: "default",
                    triggers: ["0 0 0 0 ? 2014"]
                ],
                [
                    jobName: "testJobDup",
                    jobGroup: "default",
                    triggers: ["0 0 0 0 ? 2015"]
                ]
            ]        
            return [
                mergedTriggers: jobsMock.findAll { it.jobName in [mergeName,name] }.collect { return it.triggers }.flatten(),
                delete: new Date()
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.mergescheduleChainJob()
        assert model.delete <= new Date()   
        assert model.mergedTriggers == ["0 0 0 0 ? 2014","0 0 0 0 ? 2015"]
    }
    
    void testListCurrentlyExecutingJobs() {
        controller.request.method = "GET"
        JobService.metaClass.listCurrentlyExecutingJobs = { -> }
        def control = mockFor(JobService)
        control.demand.listCurrentlyExecutingJobs { -> 
            return [
                executingJobs: [
                    [
                        chain: "testChain",
                        name: "testJob",
                        description: "a test job description",
                        group: "default",
                        cron: "0 0 0 0 ? 2014",
                        fireTime: new Date(),
                        scheduledFireTime: new Date(),
                        input: []
                    ]
                ]
            ]
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.listCurrentlyExecutingJobs()
        assert model.executingJobs[0].chain == "testChain"           
    }
    
    void testGetJobLogs() {
        controller.params << [
            name: "testChain:1234",
            records: 3,
            offset: 0
        ]
        controller.request.method = "GET"
        def control = mockFor(JobService)
        control.demand.getJobLogs { String name,Integer records,Integer offset-> 
            [
                [ 
                    jobHistory: [
                        name: "testChain:1234",
                        chain: "testChain",
                        groupName: "default",
                        description: "",
                        cron: "0 0 0 0 ? 2014",
                        fireTime: new Date(),
                        scheduledFireTime: new Date()
                    ] as JobHistory,
                    jobLogs: [
                        [
                            line: "Line 1",
                            logTime: new Date()
                        ] as JobLog,
                        [
                            line: "Line 2",
                            logTime: new Date()
                        ] as JobLog,
                        [
                            line: "Line 3",
                            logTime: new Date()
                        ] as JobLog,
                        [
                            line: "Line 4",
                            logTime: new Date()
                        ] as JobLog                        
                    ]
                ]
            ].each { jh -> 
                jh.jobHistory.save()
                jh.jobLogs.each { jl ->
                    jh.jobHistory.addToJobLogs(jl)
                    jh.jobHistory.save()
                }
            }
            def jobHistory = JobHistory.findByName(name.trim())
            return [
                jobLogs: JobLog.createCriteria().list(sort: 'id', order:'desc', max: records, offset: offset) {
                    eq('jobHistory',jobHistory)
                },
                jobHistories: JobHistory.list(),
                total: JobLog.countByJobHistory(jobHistory)
            ]            
        }
        controller.jobService = control.createMock()
        
        controller.request.contentType = "text/json"
        // controller.request.content = (["pattern": null] as JSON).toString().getBytes()
        def model = controller.getJobLogs()
        assert model.jobLogs.size() == 3          
    }
}
