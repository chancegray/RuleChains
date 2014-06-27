<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="RuleChains"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
                <r:require modules="core"/>
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
                <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<!--
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
                <link rel="stylesheet" href="${resource(dir: 'js/codemirror-ui/lib/CodeMirror-2.3/lib', file: 'codemirror.css')}" />
                <link rel="stylesheet" href="${resource(dir: 'js/codemirror-ui/css', file: 'codemirror-ui.css')}" type="text/css" media="screen" />                        
                -->
                <link rel="stylesheet" href="${resource(dir: 'js/codemirror-ui/lib/CodeMirror-2.3/lib', file: 'codemirror.css')}" />
                <link rel="stylesheet" href="${resource(dir: 'js/codemirror-ui/lib/CodeMirror-2.3/theme', file: 'ambiance.css')}" />
                <link rel="stylesheet" href="${resource(dir: 'js/codemirror-ui/css', file: 'codemirror-ui.css')}" type="text/css" media="screen" />                        
                <link type="text/css" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.13/themes/le-frog/jquery-ui.css" rel="stylesheet" />
                <link type="text/css" href="https://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/css/jquery.dataTables_themeroller.css" rel="stylesheet" />                 
		<g:layoutHead/>
		<r:layoutResources />
	</head>
	<body>
		<div id="ruleChainsLogo" class="ui-widget-header ui-corner-all" role="banner">
                  <a href="https://github.com/USF-IT/RuleChains/wiki" target="_blank"><img src="${resource(dir: 'images', file: 'chains.png')}" alt="RuleChains" width="200" height="85" style="padding-left: 5px;padding-top:0px"/></a>
                  <h1 style="display:inline;margin:0;font-weight:bold; font-size:5em;margin: auto;text-align: center;">RuleChains</h1>
                  <span style="display:none"><castag:casLogoutButton /></span>
                  <span style="float:right;display:none"><label for="authorities">Assigned Roles:</label><castag:casRoles /></span>
                </div>
		<g:layoutBody/>
		<div class="footer" class="ui-widget-header ui-corner-all" role="contentinfo"></div>
		<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
		<g:javascript library="application"/>
		<r:layoutResources />
	</body>
</html>
