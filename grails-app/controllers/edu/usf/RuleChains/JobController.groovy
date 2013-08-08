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
}
