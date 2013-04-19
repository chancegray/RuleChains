modules = {
    core {
        resource url: 'https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.js', disposition:'head'        
        resource url: 'https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.js', disposition:'head'   
        resource url: 'https://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/jquery.dataTables.min.js', disposition:'head'
        // resource url: 'https://ajax.aspnetcdn.com/ajax/jquery.mobile/1.1.0/jquery.mobile-1.1.0.min.js', disposition:'head'
        // resource url: 'https://raw.github.com/LiosK/UUID.js/master/dist/uuid.core.js', disposition:'head'
        // resource url: 'js/jQuery-Timepicker-Addon/jquery-ui-timepicker-addon.js', disposition: 'head'
        resource url: 'js/codemirror-ui/lib/CodeMirror-2.3/lib/codemirror.js', disposition: 'head'
        resource url: 'js/codemirror-ui/lib/CodeMirror-2.3/lib/util/searchcursor.js', disposition: 'head'
        resource url: 'js/codemirror-ui/lib/CodeMirror-2.3/mode/mysql/mysql.js', disposition: 'head'
        resource url: 'js/codemirror-ui/lib/CodeMirror-2.3/mode/groovy/groovy.js', disposition: 'head'
        // resource url: 'js/codemirror-ui/js/codemirror-ui.js', disposition: 'head'
        resource url: 'js/codemirror-ui/js/codemirror-ui.js'
        resource url: 'js/jeditable-1.7.2-dev/jquery.jeditable.js', disposition: 'head'
        resource url: 'js/jquery.ruleChains.js', disposition: 'head'
        resource url: 'js/jquery.ui.ruleChains.js', disposition: 'head'
    }    
    application {
        resource url:'js/application.js'
    }
}