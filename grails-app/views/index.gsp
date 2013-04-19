<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Welcome to Grails</title>
	</head>
	<body>
          <div id="tabs">
            <ul>
              <li><a href="#monitor">Monitor</a></li>
              <li><a href="#schedule">Schedule</a></li>
              <li><a href="#chains">Chains</a></li>
              <li><a href="#ruleSets">RuleSets</a></li>
            </ul>  
            <div id="monitor" role="main">
            </div>
            <div id="schedule" role="main">              
              <div id="jobsButtonSet" style="display: inline-block;font-size:70%;">
                <button id="refreshScheduledJobs">Refresh Scheduled Jobs</button> 
                <button id="addScheduledJob">Schedule new Job</button> 
                <button id="scheduleMergeButton">Merge Common Chain Schedules</button> 
                <button id="scheduleDeleteButton">Delete Scheduled Job</button> 
              </div>
              <table id="scheduleTable" style="font-size:70%;">
                <thead>
                  <!-- <tr>
                    <th colspan="3">
                      <div id="chainButtonSet">
                        <button id="addLink">Add new Link</button> 
                        <button id="moveLink">Move Link</button> 
                        <button id="deleteLink">Delete Link</button> 
                      </div>
                    </th>
                  </tr> -->
                  <tr>
                    <!-- <th>&nbsp;</th> -->
                    <th>Job</th>
                    <th>Chain</th>
                    <th>Triggers</th>
                    <th>Job&nbsp;Code</th>
                  </tr>
                </thead>
              </table>
            </div>
            <div id="chains" role="main">
              <chain:chainSelect />
              <div id="chainButtonSet" style="display: inline-block;font-size:70%;">
                <button id="refreshChain">Refresh Chain</button> 
                <button id="addChain">Add new Chain</button> 
                <button id="modifyChain">Modify Chain</button> 
                <button id="deleteChain">Delete Chain</button> 
              </div>
              <table id="chainTable" style="font-size:70%;">
                <thead>
                  <tr>
                    <th colspan="7">
                      <div id="chainButtonSet">
                        <button id="addLink">Add new Link</button> 
                        <button id="moveLink">Move Link</button> 
                        <button id="deleteLink">Delete Link</button> 
                      </div>
                    </th>
                  </tr>
                  <tr>
                    <th>&nbsp;</th>
                    <th>Id</th>
                    <th>Type</th>
                    <th>SourceName</th>
                    <th>Execute Action</th>
                    <th>Result Action</th>
                    <th>Link Action</th>                    
                  </tr>
                </thead>
              </table>
            </div>
            <div id="ruleSets" role="main">
              <ruleSet:ruleSetSelect />
              <div id="ruleSetButtonSet" style="display: inline-block;font-size:70%;">
                <button id="refreshSet">Refresh Set</button> 
                <button id="addSet">Add new Set</button> 
                <button id="modifySet">Modify Set</button> 
                <button id="deleteSet">Delete Set</button> 
              </div>
              <table id="ruleTable" style="font-size:70%;">
                <thead>
                  <tr>
                    <th colspan="4">
                      <div id="ruleButtonSet">
                        <button id="addRule">Add new Rule</button> 
                        <button id="moveRule">Move Rule</button> 
                        <button id="deleteRule">Delete Rule</button> 
                      </div>
                    </th>
                  </tr>
                  <tr>
                    <th>&nbsp;</th>
                    <th>Id</th>
                    <th>Name</th>
                    <th>Type</th>
                  </tr>
                </thead>
              </table>
            </div>
          </div>
	</body>
</html>
