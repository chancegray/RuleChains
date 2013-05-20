package edu.usf.RuleChains

import grails.converters.*

class ConfigController {
    def configService
    
    
    def downloadChainData() {
        JSON.use("deep") { 
            response.setHeader "Content-disposition", "attachment; filename=RCBackup.json"
            response.contentType = 'application/json'
            response.outputStream << (configService.downloadChainData() as JSON).toString(true)
            response.outputStream.flush()        
        }
    }
    def uploadChainData() {
        // println (params as JSON)
        println params.upload
        withFormat {
            html {
                return configService.uploadChainData(JSON.parse(params.upload))
            }
            xml {
                render configService.uploadChainData(JSON.parse(params.upload)) as XML
            }
            json {
                JSON.use("deep") { render configService.uploadChainData(JSON.parse(params.upload)) as JSON }
            }
        }         
    }
}
