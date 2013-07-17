class UrlMappings {

	static mappings = {
                "/ruleSet"(controller:"ruleSet",parseRequest: true){ 
                    action = [GET:"listRuleSets", PUT:"addRuleSet", DELETE:"error", POST:"error"] 
                } 
                "/ruleSet/$name"(controller:"ruleSet",parseRequest: true){ 
                    action = [GET:"getRuleSet", PUT:"error", DELETE:"deleteRuleSet", POST:"modifyRuleSet"] 
                } 
                "/ruleSet/$name/$id"(controller:"ruleSet",parseRequest: true){ 
                    action = [GET:"getRule", PUT:"addRule", DELETE:"deleteRule", POST:"updateRule"] 
                } 
                "/ruleSet/$name/$id/$nameUpdate"(controller:"ruleSet",parseRequest: true){ 
                    action = [GET:"error", PUT:"moveRule", DELETE:"error", POST:"updateRuleName"] 
                } 
                "/chain"(controller:"chain",parseRequest: true){ 
                    action = [GET:"listChains", PUT:"addChain", DELETE:"error", POST:"error"] 
                } 
                "/chain/$name"(controller:"chain",parseRequest: true){ 
                    action = [GET:"getChain", PUT:"addChainLink", DELETE:"deleteChain", POST:"modifyChain"] 
                } 
                "/chain/$name/$sequenceNumber"(controller:"chain",parseRequest: true){ 
                    action = [GET:"getChainLink", PUT:"error", DELETE:"deleteChainLink", POST:"modifyChainLink"] 
                } 
                "/source"(controller:"chain",parseRequest: true){ 
                    action = [GET:"getSources", PUT:"error", DELETE:"error", POST:"error"] 
                } 
                "/job"(controller:"job",parseRequest: true){ 
                    action = [GET:"listChainJobs", PUT:"error", DELETE:"error", POST:"mergescheduleChainJob"] 
                }
                "/job/$name"(controller:"job",parseRequest: true){ 
                    action = [GET:"error", PUT:"createChainJob", DELETE:"removeChainJob", POST:"updateChainJob"] 
                }
                "/job/$name/$cronExpression"(controller:"job",parseRequest: true){ 
                    action = [GET:"error", PUT:"addscheduleChainJob", DELETE:"unscheduleChainJob", POST:"rescheduleChainJob"] 
                }
                "/chainServiceHandler"(controller:"chainServiceHandler",parseRequest: true){ 
                    action = [GET:"listChainServiceHandlers", PUT:"addChainServiceHandler", DELETE:"error", POST:"error"] 
                }
                "/chainServiceHandler/$name"(controller:"chainServiceHandler",parseRequest: true){ 
                    action = [GET:"error", PUT:"error", DELETE:"deleteChainServiceHandler", POST:"modifyChainServiceHandler" ] 
                }
                "/backup/download"(controller:"config",action: "downloadChainData",parseRequest: true)
                "/backup/upload"(controller:"config",parseRequest: true){ 
                    action = [GET:"error", PUT:"error", DELETE:"error", POST:"uploadChainData"] 
                }
                "/service/$handler"(controller:"chainServiceHandler",action: "handleChainService",parseRequest: true)
		"/"(view:"/index",parseRequest: true)
		"/login/denied"(view:"/login/denied",parseRequest: true)
                "/logout/index"(controller:"logout",parseRequest: true) {
                    action = "index"
                }
		"500"(view:'/error')
	}
}
