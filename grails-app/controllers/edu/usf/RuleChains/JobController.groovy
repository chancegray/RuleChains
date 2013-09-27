package edu.usf.RuleChains
import grails.converters.*

class JobController {
    def jobService
    def listChainJobs() {
        withFormat {
            html {
                return jobService.listChainJobs()
            }
            xml {
                render jobService.listChainJobs() as XML
            }
            json {
                JSON.use("deep") { render jobService.listChainJobs() as JSON }
            }
        }   
    }
    def createChainJob() {
        withFormat {
            html {
                return jobService.createChainJob(params.cronExpression,params.name,params.input)
            }
            xml {
                render jobService.createChainJob(params.cronExpression,params.name,params.input) as XML
            }
            json {
                JSON.use("deep") { render jobService.createChainJob(params.cronExpression,params.name,params.input) as JSON }
            }
        }   
    }
    def removeChainJob() {
        withFormat {
            html {
                return jobService.removeChainJob(params.name)
            }
            xml {
                render jobService.removeChainJob(params.name) as XML
            }
            json {
                JSON.use("deep") { render jobService.removeChainJob(params.name) as JSON }
            }
        }   
    }
    def unscheduleChainJob() {
        withFormat {
            html {
                return jobService.unscheduleChainJob(params.cronExpression,params.name)
            }
            xml {
                render jobService.unscheduleChainJob(params.cronExpression,params.name) as XML
            }
            json {
                JSON.use("deep") { render jobService.unscheduleChainJob(params.cronExpression,params.name) as JSON }
            }
        }   
    }
    def rescheduleChainJob() {
        withFormat {
            html {
                return jobService.rescheduleChainJob(params.cronExpression,params.cron,params.name)
            }
            xml {
                render jobService.rescheduleChainJob(params.cronExpression,params.cron,params.name) as XML
            }
            json {
                JSON.use("deep") { render jobService.rescheduleChainJob(params.cronExpression,params.cron,params.name) as JSON }
            }
        }   
    }
    def updateChainJob() {
        // String name,String newName
        withFormat {
            html {
                return jobService.updateChainJob(params.name,params.newName)
            }
            xml {
                render jobService.updateChainJob(params.name,params.newName) as XML
            }
            json {
                JSON.use("deep") { render jobService.updateChainJob(params.name,params.newName) as JSON }
            }
        }   
    }
    def addscheduleChainJob() {
        withFormat {
            html {
                return jobService.addscheduleChainJob(params.cronExpression,params.name)
            }
            xml {
                render jobService.addscheduleChainJob(params.cronExpression,params.name) as XML
            }
            json {
                JSON.use("deep") { render jobService.addscheduleChainJob(params.cronExpression,params.name) as JSON }
            }
        }   
    }
    def mergescheduleChainJob() {
        withFormat {
            html {
                return jobService.mergescheduleChainJob(params.mergeName,params.name)
            }
            xml {
                render jobService.mergescheduleChainJob(params.mergeName,params.name) as XML
            }
            json {
                JSON.use("deep") { render jobService.mergescheduleChainJob(params.mergeName,params.name) as JSON }
            }
        }   
    }
    def listCurrentlyExecutingJobs() {
        withFormat {
            html {
                return jobService.listCurrentlyExecutingJobs()
            }
            xml {
                render jobService.listCurrentlyExecutingJobs() as XML
            }
            json {
                JSON.use("deep") { render jobService.listCurrentlyExecutingJobs() as JSON }
            }
        }   
    }
    def getJobLogs() {
        withFormat {
            html {   
                return (jobService.getJobLogs(
                    params.name,
                    params.iDisplayLength?(Math.min( params.iDisplayLength ? params.iDisplayLength.toInteger() : 20,  100) ):(Math.min( params.records ? params.records.toInteger() : 20,  100) ),
                    params.iDisplayStart?(params?.iDisplayStart?.toInteger() ?: 0):(params?.offset?.toInteger() ?: 0)
                ) << [ sEcho: params.sEcho ])
            }
            xml {
                render jobService.getJobLogs(
                    params.name,
                    params.iDisplayLength?(Math.min( params.iDisplayLength ? params.iDisplayLength.toInteger() : 20,  100) ):(Math.min( params.records ? params.records.toInteger() : 20,  100) ),
                    params.iDisplayStart?(params?.iDisplayStart?.toInteger() ?: 0):(params?.offset?.toInteger() ?: 0)
                ) as XML
            }
            json {
                JSON.use("deep") { 
                    def jobLogsResponse = jobService.getJobLogs(
                        params.name,
                        params.iDisplayLength?(Math.min( params.iDisplayLength ? params.iDisplayLength.toInteger() : 20,  100) ):(Math.min( params.records ? params.records.toInteger() : 20,  100) ),
                        params.iDisplayStart?(params?.iDisplayStart?.toInteger() ?: 0):(params?.offset?.toInteger() ?: 0)
                    )
                    jobLogsResponse.sEcho = params.sEcho
                    render jobLogsResponse as JSON
                }
            }
        }           
    }
    def getJobRuleTimings() {
        withFormat {
            html {   
                return (jobService.getJobRuleTimings(
                    params.name,
                    params.iDisplayLength?(Math.min( params.iDisplayLength ? params.iDisplayLength.toInteger() : 20,  100) ):(Math.min( params.records ? params.records.toInteger() : 20,  100) ),
                    params.iDisplayStart?(params?.iDisplayStart?.toInteger() ?: 0):(params?.offset?.toInteger() ?: 0)
                ) << [ sEcho: params.sEcho ])
            }
            xml {
                render jobService.getJobRuleTimings(
                    params.name,
                    params.iDisplayLength?(Math.min( params.iDisplayLength ? params.iDisplayLength.toInteger() : 20,  100) ):(Math.min( params.records ? params.records.toInteger() : 20,  100) ),
                    params.iDisplayStart?(params?.iDisplayStart?.toInteger() ?: 0):(params?.offset?.toInteger() ?: 0)
                ) as XML
            }
            json {
                JSON.use("deep") { 
                    def jobRuleTimingsResponse = jobService.getJobRuleTimings(
                        params.name,
                        params.iDisplayLength?(Math.min( params.iDisplayLength ? params.iDisplayLength.toInteger() : 20,  100) ):(Math.min( params.records ? params.records.toInteger() : 20,  100) ),
                        params.iDisplayStart?(params?.iDisplayStart?.toInteger() ?: 0):(params?.offset?.toInteger() ?: 0)
                    )
                    jobRuleTimingsResponse.sEcho = params.sEcho
                    render jobRuleTimingsResponse as JSON
                }
            }
        }           
    }
    def getJobHistories() {
        withFormat {
            html {
                return jobService.getJobHistories()
            }
            xml {
                render jobService.getJobHistories() as XML
            }
            json {
                JSON.use("deep") { render jobService.getJobHistories() as JSON }
            }
        }   
    }
    def deleteJobHistory() {
        withFormat {
            html {
                return jobService.deleteJobHistory(params.name)
            }
            xml {
                render jobService.deleteJobHistory(params.name) as XML
            }
            json {
                JSON.use("deep") { render jobService.deleteJobHistory(params.name) as JSON }
            }
        }
    }
}
