if (typeof jQuery !== 'undefined') {
    (function($) {
        var spinner = $('#spinner'),        
        waitDialog = $('<div />');
        waitDialog.addClass('ui-state-default ui-widget-content')
        .css({
            "background": 'url("images/ajax-loader.gif") no-repeat scroll 50% 50% rgba(0, 0, 0, 0)',
            "background-position": "center center"
        })
        .append(
            $('<p />')
            .css({
                "text-align": "center",
                "margin-top": "0px",
                "margin-bottom": "0px",
                "padding": "0px"
            })
        )
        .dialog({            
            autoOpen: false,
            draggable: false,
            resizable: false,
            show: {
                effect: 'fade',
                duration: 200
            },
            hide: {
                effect: 'fade',
                duration: 200
            },
            open: function(){
            },
            close: function(){
            },            
            bgiframe: true,
            title: 'Please Wait....',
            height:180,
            width:220,
            modal: true,
            zIndex: 3999,
            overlay: {
                backgroundColor: '#000',
                opacity: 0.5
            }
        });        
        waitDialog.ajaxStart(function() { 
            $(this).dialog("open");
        }).ajaxStop(function() {
            $(this).dialog("close");
        });
        
        $('div#tabs').ruleChains();
    })(jQuery);
}
