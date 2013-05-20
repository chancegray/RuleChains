/**
 * Testing Plugin for displaying MessageService Queue REST Methods visually
 * 
 * Depends on: jquery.messageService.js (contains all the AJAX methods used in this plugin)
 *             JQuery UI, JQuery
 * 
 */
(function($) {
    $.widget("ui.ruleChains", {
        options: {
            
        },
        _create: function() {
            var self = this,
                o = self.options,
                el = self.element;
            $.ajaxSetup({
                dataType : "json",
                type: "POST",
                beforeSend: function (XMLHttpRequest, settings) {
                    XMLHttpRequest.setRequestHeader("Content-Type", "application/json");
                    XMLHttpRequest.setRequestHeader("Accept", "application/json");
                },
                complete: function () {
                },
                async: true,
                error: function (jqXHR,  textStatus, errorThrown) {
                    if (jqXHR.status === 0) {
                        // Session has probably expired and needs to reload and let CAS take care of the rest
                        alert('Your session has expired, the page will need to reload and you may be asked to log back in');
                        // reload entire page - this leads to login page
                        window.location.reload();
                    }
                }
            });
            $.editable.addInputType('text-ui', {
                element: function (settings, original) {
                    var txtInput = $('<input />', {
                                'id': 'txtInput'
                            })
                            .appendTo($(this));
                    return (txtInput);
                },
                plugin:  function (settings, original) {
                    $('button', this)
                    .button({
                        text: false,
                        icons: {
                            primary: 'ui-icon-disk'
                        },
                        label: $('button', this).html()
                    })
                    .find('span.ui-button-text')
                    .empty();
                }
            });
            self.buildTabContent();
            
        },      
        buildTabContent: function() {
            var self = this,
                o = self.options,
                el = self.element,
                tabs = $(self.tabContents = {
                    monitor: $('div#monitor',el),
                    schedule: $('div#schedule',el),
                    chains: $('div#chains',el),
                    ruleSets: $('div#ruleSets',el),
                    backup: $('div#backup',el)
                });
            $.ruleChains.chain.GETgetSources({},function(sources) {
                self.sources = sources.sources;
                self.actions = sources.actions;
                self.jobGroups = sources.jobGroups;
                $(el).tabs();
                self.buildChainsContent();
                self.buildRuleSetsContent();
                self.buildMonitorContent();
                self.buildScheduleContent();
                self.buildBackupContent();
            });            
        },
        buildMonitorContent: function() {
            var self = this,
                o = self.options,
                el = self.element;
            
        },        
        buildBackupContent: function() {
            var self = this,
                o = self.options,
                el = self.element,
                tabs = self.tabContents,
                backupButton = $(self.backupButton = $('a#backupButton',tabs.backup)).button({
                    text: true,
                    icons: {
                        primary: "ui-icon-arrowthick-1-s"
                    }            
                }).click(function(e) {
                    e.preventDefault();  //stop the browser from following
                    window.location.href = 'backup/download';
                }),
                restoreButton = $(self.restoreButton = $('button#restoreButton',tabs.backup)).button({
                    text: true,
                    icons: {
                        primary: "ui-icon-arrowthick-1-n"
                    }            
                }).click(function(){
                    restoreFile.click();
                }),
                restoreFile = $(self.restoreFile = $('input#restore')).change(function() { 
                    if(restoreFile.val() !== "") {
                        // alert(JSON.stringify(restoreFile.get(0).files));
                        var reader  = new FileReader();
                        // Closure to capture the file information.
                        reader.onload = (function(e) {
                            // var content = e.target.result;
                            // alert(content);
                            // var json = $.parseJSON(e.target.result);
                            var json = JSON.parse(e.target.result);
//                            alert(json);
//                            $.each(json.ruleSets[0].rules,function(index,ruleSet) {
//                                alert(JSON.stringify(ruleSet));
//                            });
//                            alert(JSON.stringify(json));
                            $.ruleChains.config.POSTuploadChainData(e.target.result,function(response) {
                                restoreFile.val("").trigger("change");
                                if("error" in response) {
                                    alert(response.error);
                                }
                                self.chainRefreshButton.trigger("click");
                                self.ruleSetRefreshButton.trigger("click");
                            });
                        });
                        reader.readAsText(restoreFile.get(0).files[0]);
                    }
                }),
                backupButtonSet = $('div#backupButtonSet',tabs.backup).buttonset();
            
        },        
        buildScheduleContent: function() {
            var self = this,
                o = self.options,
                el = self.element,
                tabs = self.tabContents,
                scheduledJobsRefreshButton = $(self.scheduledJobsRefreshButton = $('button#refreshScheduledJobs',tabs.schedule)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-refresh"
                    }            
                }).click(function() {
                    $.ruleChains.job.GETlistChainJobs({},function(response) {
                        if("jobGroups" in response) {
                            self.jobGroups=response.jobGroups;
                            self.scheduleDataTable.fnClearTable();
                            self.scheduleDataTable.fnAddData($.grep(self.jobGroups, function(n, i){ 
                                return n.name === "DEFAULT"; 
                            })[0].jobs);
                        } else {
                            alert(response.error);
                        }
                    });
                }),            
                addScheduledJobButton = $(self.addScheduledJobButton = $('button#addScheduledJob',tabs.schedule)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-plusthick"
                    }            
                }).click(function() {
                    if(self.chainSelect.children().length > 1) {
                        $('<div />')
                        .data({
                            cronExpression: $('<input />',{
                                id: "cronExpression"
                            }),
                            chainSelect: self.chainSelect.clone().find('option:first-child').remove().end().css({"float":"right"}),
                            quartzCronDocumentation: $('<a />',{
                                'href': 'http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html',
                                'target': '_blank'
                            })
                            .css({
                                'display': 'inline-block',
                                // 'width':'80%',
                                'margin': '0pt 4px 0pt 0pt',
                                "color": "rgb(255, 255, 255)"
                            })
                            .html('Click for Quartz CRON Tutorial')
                            .button({
                                text: false,
                                icons: {
                                    primary: "ui-icon-extlink"
                                }
                            }),
                            cronToolTipIcon: $('<span />',{
                                "id": "cronToolTipIcon"
                            })
                            .addClass('ui-icon ui-icon-help')
                            .css({
                                'display': 'inline-block',
                                // 'width':'80%',
                                'margin': '0pt 4px 0pt 0pt'
                            }),   
                            tooltip: $('<div />')
                            .append(
                                $('<p />')
                                .append(
                                    $('<span />')
                                    .css({
                                        'display':'block',
                                        "font-size":"11px",
                                        'width':'90%'
                                    })
                                    .html('Default CRON expression based on current Date and Time(24hr format): ')
                                    .each(function() {
                                        var currentDate = new Date();
                                        $(this).append(
                                            [
                                                [
                                                    [
                                                        currentDate.getFullYear(),
                                                        (currentDate.getMonth()+1),
                                                        currentDate.getDate()
                                                    ].join('/'),
                                                    [
                                                        currentDate.getHours(),
                                                        currentDate.getMinutes(),
                                                        currentDate.getSeconds()
                                                    ].join(':')
                                                ].join(' '),
                                                [
                                                    currentDate.getSeconds(),
                                                    currentDate.getMinutes(),
                                                    currentDate.getHours(),
                                                    currentDate.getDate(),
                                                    (currentDate.getMonth()+1),
                                                    "?",
                                                    currentDate.getFullYear()
                                                ].join(' ')                                        
                                            ].join(' -> Cron Expression: ')
                                        );
                                    })
                                )
                            ).hide()
                        })
                        .addClass('ui-state-default ui-widget-content')
                        .append(
                            $('<p />')
                            .css({
                                "text-align": "center",
                                "margin-top": "0px",
                                "margin-bottom": "0px",
                                "padding": "0px"
                            })
                        ).dialog({                    
                            autoOpen: true,
                            bgiframe: true,
                            resizable: false,
                            title: 'Submit New Job for Scheduling',
                            height:280,
                            width:560,
                            modal: true,
                            zIndex: 3999,
                            overlay: {
                                backgroundColor: '#000',
                                opacity: 0.5
                            },
                            open: function() {
                                var dialog = $(this);
                                $(this).append(
                                    $('<table />')
                                    .width("100%")
                                    .append(
                                        $('<tr />')
                                        .append(
                                            $('<td />')
                                            .append(
                                                $('<label />',{
                                                    "for": "chain"
                                                })
                                                .html('Target Chain:')
                                            )
                                        )
                                        .append(
                                            $('<td />').append($(this).data().chainSelect)                                        
                                        )
                                    )
                                    .append(
                                        $('<tr />')
                                        .append(
                                            $('<td />')
                                            .append(
                                                $('<label />',{
                                                    "for": "cronExpression"
                                                })
                                                .html('Cron Expression:')
                                            )
                                        )
                                        .append(
                                            $('<td />').css({"float":"right"}).append(
                                                dialog.data().cronExpression
                                            ).append(
                                                dialog.data().cronToolTipIcon
                                                .mouseover(function(){
                                                    dialog.data().tooltip.css({opacity:0.8, display:"none"}).fadeIn(400);
                                                })
                                                .mousemove(function(kmouse){
                                                    var x = kmouse.pageX - this.offsetLeft,
                                                        y = kmouse.pageY - this.offsetTop;
                                                    // $(this).data('toolTip').css({left:kmouse.pageX+15, top:kmouse.pageY+15});
                                                    // $(this).data('toolTip').css({left:x-150, top:y+100});
                                                    dialog.data().tooltip.css({left:15, top:430});
                                                })
                                                .mouseout(function(){
                                                    dialog.data().tooltip.fadeOut(1100);
                                                })
                                            ).append(
                                                $('<small />').append(dialog.data().quartzCronDocumentation)                                                
                                            )                                            
                                        )
                                    )
                                ).append(dialog.data().tooltip);                                     
                            },
                            buttons: {
                                "Schedule New Job": function() {
                                    var dialog = $(this),
                                        json = {
                                            name: dialog.data().chainSelect.find('option:selected').text(),
                                            cronExpression: $.trim(dialog.data().cronExpression.val())
                                        };
                                    if(json.cronExpression.length > 1) {
                                        $.ruleChains.job.PUTcreateChainJob(json,function(response) {
                                            if("date" in response) {
                                                self.scheduledJobsRefreshButton.trigger('click');                                                
                                                dialog.dialog('close');
                                                dialog.dialog('destroy');
                                                dialog.remove();                                                                        
                                            } else {
                                                alert(response.error);
                                            }
                                        });                                    
                                    } else {
                                        alert("You must supply a valid cron expression!");
                                    }
                                },
                                "Cancel": function() {
                                    $(this).dialog('close');
                                    $(this).dialog('destroy');
                                    $(this).remove();                        
                                }
                            }                    
                        });
                        
                    } else {
                        alert("You MUST have an existing rule chain to schedule a job!");
                    }
                }),
                scheduleMergeButton = $(self.scheduleMergeButton = $('button#scheduleMergeButton',tabs.schedule)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-wrench"
                    },
                    disabled: true
                }).click(function() {
                    var aData = self.scheduleDataTable.fnGetData($.grep(self.scheduleDataTable.fnGetNodes(),function(tr) {
                        return $(tr).hasClass('ui-widget-shadow');
                    })[0]),
                    jobNames = $.map($.grep(self.scheduleDataTable.fnGetData(),function(obj,index) {
                        return ((obj.name.split(":")[0] === aData.name.split(":")[0]) && (obj.name !== aData.name));
                    }),function(val,index) {
                        return val.name;
                    });
                    if(jobNames.length < 1) {
                        alert("ERROR: No matching jobs that can be merged");
                    } else {
                        $('<div />')
                        .data({
                            jobSelect: $('<select />',{ "id": "merge"}).each(function() {
                                var select = $(this);
                                $.each(jobNames,function(index,jobName) {
                                    $('<option />').val(jobName).html(jobName).appendTo(select);
                                });
                            })
                        })
                        .addClass('ui-state-default ui-widget-content')
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
                            autoOpen: true,
                            bgiframe: true,
                            resizable: false,
                            title: 'Merge Schedule',
                            height:280,
                            width:450,
                            modal: true,
                            zIndex: 3999,
                            overlay: {
                                backgroundColor: '#000',
                                opacity: 0.5
                            },
                            open: function() {
                                $(this).append(
                                    $('<label />',{
                                        "for": "merge"
                                    })
                                    .html('Job to Merge:')
                                    .css({ "padding-right":"15px"})
                                ).append($(this).data().jobSelect);
                            },
                            buttons: {
                                "Merge into Single Job": function() {
                                    var dialog = $(this);
                                    $.ruleChains.job.POSTmergescheduleChainJob({
                                        name: aData.name,
                                        mergeName: dialog.data().jobSelect.val()
                                    },function(status) {
                                        if("delete" in status) {
                                            self.scheduleDataTable.fnClearTable();
                                            self.scheduledJobsRefreshButton.trigger('click');
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                    
                                        } else {
                                            alert(status.error);
                                        }                                        
                                    });
                                },
                                "Cancel": function() {
                                    $(this).dialog('close');
                                    $(this).dialog('destroy');
                                    $(this).remove();                                                
                                }                    
                            }
                        });
                    }
                }),
                scheduleDeleteButton = $(self.scheduleDeleteButton = $('button#scheduleDeleteButton',tabs.schedule)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-trash"
                    },
                    disabled: true
                }).click(function() {
                    $('<div />')
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Delete Schedule',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<span />').html("Are you SURE you want the selected schedule?")
                            );
                        },
                        buttons: {
                            "Confirm": function() {
                                var dialog = $(this);
                                var aData = self.scheduleDataTable.fnGetData($.grep(self.scheduleDataTable.fnGetNodes(),function(tr) {
                                    return $(tr).hasClass('ui-widget-shadow');
                                })[0]),
                                json = {
                                    name: self.chainSelect.find('option:selected').text(),
                                    sequenceNumber: aData.sequenceNumber
                                };                                
                                $.ruleChains.job.DELETEremoveChainJob({
                                        name: aData.name
                                    },function(status) {
                                        if("status" in status) {
                                            self.scheduleDataTable.fnClearTable();
                                            self.scheduledJobsRefreshButton.trigger('click');
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                    
                                        } else {
                                            alert(status.error);
                                        }
                                    }
                                );
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                                                
                            }
                        }
                    });                                                        
                }),
                jobsButtonSet = $('div#jobsButtonSet',tabs.schedule).buttonset(),
                scheduleTable = $(self.scheduleTable = $('table#scheduleTable',tabs.schedule).width('100%')),
                scheduleDataTable = $(self.scheduleDataTable = scheduleTable.dataTable({
                    "aoColumns": [
//                        { "bSortable": false,"bVisible": true,"mDataProp": null, "fnRender":
//                            function(oObj) {
//                                return "<button type='button' id='details' />";
//                            }
//                        },                        
                        { "bVisible": true,"mDataProp": "name","sDefaultContent":"","aDataSort": [ 1 ],"asSorting": [ "asc" ] },
                        { "bVisible": true,"mDataProp": null,"sDefaultContent":"","fnRender": 
                            function(oObj) {
                                var div = $('<div />'),
                                    chainSelect = self.chainSelect.clone().find('option:first-child').remove().end().appendTo(div);
                                return div.html();
                            }                             
                        },
                        { "bVisible": true,"mDataProp": null,"sDefaultContent":"","fnRender": 
                            function(oObj) {
                                return $('<div />').append(
                                    $('<span />',{ "id": "triggerButtonSet" }).append(
                                        $('<select />',{ "id": "trigger" })
                                    ).append(
                                        $('<button />',{ "id": "triggerAdd" })
                                    ).append(
                                        $('<button />',{ "id": "triggerDelete" })
                                    ).append(
                                        $('<button />',{ "id": "triggerEdit" })
                                    )
                                ).remove().html();
                            }
                        },
                        { "bVisible": true,"mDataProp": null,"sDefaultContent":"","fnRender": 
                            function(oObj) {
                                var div = $('<div />'),
                                    jobCodeButton = $('<button />',{
                                        "id": "jobCodeButton"
                                    }).html("Extract Job Entry Code").appendTo(div);
                                return div.html();
                            }                             
                        }
                    ],
                    "bJQueryUI": true,
                    "asStripeClasses": [ 'ui-priority-primary', 'ui-priority-secondary' ],
                    "aaData": $.grep(self.jobGroups, function(n, i){ 
                        return n.name === "DEFAULT"; 
                    })[0].jobs,
                    "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                        return $(nRow)
                        .unbind('click')
                        .click(function(event) {
                            if($(nRow).hasClass('ui-widget-shadow')) {
                                $(nRow).removeClass('ui-widget-shadow');
                                self.scheduleMergeButton.button("option","disabled",true);
                                self.scheduleDeleteButton.button("option","disabled",true);
                            } else {
                                $(self.chainDataTable.fnGetNodes( )).each(function() {
                                    $(this).removeClass('ui-widget-shadow');
                                });
                                $(nRow).addClass('ui-widget-shadow');                                    
                                self.scheduleMergeButton.button("option","disabled",false);
                                self.scheduleDeleteButton.button("option","disabled",false);
                            }
                        })
                        .find('button#details',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .find('select#chain',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .find('button#jobCodeButton',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .find('select#trigger',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .find('button#triggerAdd',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .find('button#triggerDelete',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .find('button#triggerEdit',nRow).click(function(event) { event.stopPropagation(); }).end()
                        .each(function() {
                            var nRowData = $(nRow).data(),
//                                detailsButton = $(nRowData.detailsButton = $(this).find('button#details'))
//                                .button({
//                                    text: false,
//                                    icons: {
//                                        primary: 'ui-icon-circle-triangle-e'
//                                    }
//                                })
//                                .unbind('toggle')
//                                .toggle(
//                                    function() {
//                                        nRowData.detailsButton.button( "option", "icons", {
//                                            primary: 'ui-icon-circle-triangle-s'
//                                        });
//                                        $(self.scheduleDataTable.fnGetNodes( )).each(function (index,r) {
//                                            if ( self.scheduleDataTable.fnIsOpen(r) ) {
//                                                $(r).find('button#details').click();
//                                            }                                                
//                                        });     
//                                        $(nRowData.detailsRow = $(self.scheduleDataTable.fnOpen(
//                                            nRow,
//                                            "<fieldset id='scheduleDetails' />",
//                                            'ui-widget-header'))
//                                        )
//                                        .find('fieldset#scheduleDetails')
//                                        .addClass('ui-widget-content')
//                                        .append(
//                                            $('<legend />')
//                                            .html('Schedule Details')
//                                            .addClass('ui-widget-header ui-corner-all')
//                                        )
//                                        .each(function() {
//                                            $(nRowData.detailsFieldset = $(this))
//                                            .append(
//                                                $(nRowData.triggerSelect = $('<select />'))
//                                                .each(function() {
//                                                    var select = $(this);
//                                                    $.each(aData.triggers,function(index,cron) {
//                                                        $('<option />').html(cron).val(cron).appendTo(select);
//                                                    });
//                                                }).change(function() {
//                                                    var select = $(this);
//                                                    nRowData.triggerEdit.val(nRowData.triggerSelect.val());
//                                                })
//                                            ).append(
//                                                $('<span />').append(
//                                                    $(nRowData.triggerAdd = $('<button />')).button({
//                                                        text: false,
//                                                        icons: {
//                                                            primary: "ui-icon-plusthick"
//                                                        }            
//                                                    })
//                                                ).append(
//                                                    $(nRowData.triggerDelete = $('<button />')).button({
//                                                        text: false,
//                                                        icons: {
//                                                            primary: "ui-icon-trash"
//                                                        },
//                                                        disabled: true
//                                                    })
//                                                ).buttonset()
//                                            ).append(
//                                                $('<span />').append(
//                                                    $(nRowData.triggerEdit = $('<input />',{
//                                                        "type": "text"
//                                                    })).css({'padding-left':'5px'})
//                                                ).append(
//                                                    $(nRowData.triggerUpdate = $('<button />')).button({
//                                                        text: false,
//                                                        icons: {
//                                                            primary: "ui-icon-check"
//                                                        }            
//                                                    })
//                                                ).buttonset()
//                                            ).each(function() {
//                                                nRowData.triggerSelect.trigger('change');
//                                            });
//
//                                        });
//                                    },
//                                    function() {
//                                        // Close the details
//                                        nRowData.detailsButton.button( "option", "icons", {
//                                            primary: 'ui-icon-circle-triangle-e'
//                                        });
//                                        self.scheduleDataTable.fnClose(nRow);                                                                
//                                    }
//                                ),
                                chainSelect = $(nRowData.chainSelect = $("select#chain",nRow)).val(aData.name.split(':')[0])
                                    .each(function() {
                                        $(this).val($($(this).children().filter(function() {
                                            return $(this).text() === aData.name.split(':')[0];
                                        })[0]).val());
                                    })
                                    .change(function() {
                                        var select = $(this),
                                            chainName = select.find('option:selected').text();
                                        $.ruleChains.job.POSTupdateChainJob(
                                            {
                                                name: aData.name,
                                                newName: chainName
                                            },function(response) {
                                                if("updated" in response) {
                                                    self.scheduledJobsRefreshButton.trigger('click');
                                                    // alert("Select value was updated");
                                                } else {
                                                    alert(response.error);
                                                }                                                                                                                
                                            }
                                        );
                                    }
                                ),
                                triggerSelect = $(nRowData.triggerSelect = $('select#trigger',nRow)).each(function() {
                                    var select = $(this);
                                    $.each(aData.triggers.sort(),function(index,cron) {
                                        $('<option />').html(cron).val(cron).appendTo(select);
                                    });
                                }),
                                triggerAdd = $(nRowData.triggerAdd = $('button#triggerAdd',nRow)).button({
                                    text: false,
                                    icons: {
                                        primary: "ui-icon-plusthick"
                                    }            
                                }).click(function() {
                                    $('<div />')
                                    .data({
                                        cron: $('<input />',{
                                            "type": "text",
                                            "name": "cron",
                                            "id": "cron"
                                        })
                                    })
                                    .addClass('ui-state-default ui-widget-content')
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
                                        autoOpen: true,
                                        bgiframe: true,
                                        resizable: false,
                                        title: 'Add New CRON Trigger',
                                        height:280,
                                        width:400,
                                        modal: true,
                                        zIndex: 3999,
                                        overlay: {
                                            backgroundColor: '#000',
                                            opacity: 0.5
                                        },
                                        open: function() {
                                            $(this).append(
                                                $('<label />',{
                                                    "for": "cron"
                                                })
                                                .html('CRON:')
                                                .css({ "padding-right":"15px"})
                                            ).append($(this).data().cron);
                                        },                                    
                                        buttons: {
                                            "Add CRON Schedule": function() {
                                                var dialog = $(this);
                                                $.ruleChains.job.PUTaddscheduleChainJob({
                                                    name: aData.name,
                                                    cronExpression: dialog.data().cron.val()                                                    
                                                },function(status) {
                                                    if("date" in status) {
                                                        self.scheduledJobsRefreshButton.trigger('click');
                                                        dialog.dialog('close');
                                                        dialog.dialog('destroy');
                                                        dialog.remove();                                                 
                                                    } else {
                                                        alert(status.error);
                                                    }                                                    
                                                });
                                            },
                                            "Cancel": function() {
                                                $(this).dialog('close');
                                                $(this).dialog('destroy');
                                                $(this).remove();                        
                                            }
                                        }
                                    });
                                }),
                                triggerDelete = $(nRowData.triggerDelete = $('button#triggerDelete',nRow)).button({
                                    text: false,
                                    icons: {
                                        primary: "ui-icon-trash"
                                    },
                                    disabled: false
                                }).click(function() {
                                    $('<div />')
                                    .data({
                                        cron: $('<input />',{
                                            "type": "text",
                                            "name": "cron",
                                            "id": "cron"
                                        }).val(nRowData.triggerSelect.val())
                                    })
                                    .addClass('ui-state-default ui-widget-content')
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
                                        autoOpen: true,
                                        bgiframe: true,
                                        resizable: false,
                                        title: 'Delete Existing CRON Trigger',
                                        height:280,
                                        width:400,
                                        modal: true,
                                        zIndex: 3999,
                                        overlay: {
                                            backgroundColor: '#000',
                                            opacity: 0.5
                                        },
                                        open: function() {
                                            $(this).append(
                                                $('<span />')
                                                .html("Are you sure you want to delete this trigger?")
                                            );
                                        },                                    
                                        buttons: {
                                            "Delete CRON Trigger": function() {
                                                var dialog = $(this);
                                                $.ruleChains.job.DELETEunscheduleChainJob({
                                                    name: aData.name,
                                                    cronExpression: nRowData.triggerSelect.val()                                                    
                                                },function(status) {
                                                    if("status" in status) {
                                                        self.scheduledJobsRefreshButton.trigger('click');
                                                        dialog.dialog('close');
                                                        dialog.dialog('destroy');
                                                        dialog.remove();                                                 
                                                    } else {
                                                        alert(status.error);
                                                    }
                                                });
                                            },
                                            "Cancel": function() {
                                                $(this).dialog('close');
                                                $(this).dialog('destroy');
                                                $(this).remove();                        
                                            }
                                        }
                                    });
                                }),
                                triggerEdit = $(nRowData.triggerEdit = $('button#triggerEdit',nRow)).button({
                                    text: false,
                                    icons: {
                                        primary: "ui-icon-wrench"
                                    }            
                                }).click(function() {
                                    $('<div />')
                                    .data({
                                        cron: $('<input />',{
                                            "type": "text",
                                            "name": "cron",
                                            "id": "cron"
                                        }).val(nRowData.triggerSelect.val())
                                    })
                                    .addClass('ui-state-default ui-widget-content')
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
                                        autoOpen: true,
                                        bgiframe: true,
                                        resizable: false,
                                        title: 'Update Existing CRON Trigger',
                                        height:280,
                                        width:400,
                                        modal: true,
                                        zIndex: 3999,
                                        overlay: {
                                            backgroundColor: '#000',
                                            opacity: 0.5
                                        },
                                        open: function() {
                                            $(this).append(
                                                $('<label />',{
                                                    "for": "cron"
                                                })
                                                .html('CRON:')
                                                .css({ "padding-right":"15px"})
                                            ).append($(this).data().cron);
                                        },                                    
                                        buttons: {
                                            "Update CRON Schedule": function() {
                                                var dialog = $(this);
                                                $.ruleChains.job.POSTrescheduleChainJob({
                                                    name: aData.name,
                                                    cronExpression: nRowData.triggerSelect.val(),
                                                    cron: $(this).data().cron.val()
                                                },function(status) {
                                                    if("status" in status) {
                                                        self.scheduledJobsRefreshButton.trigger('click');
                                                        dialog.dialog('close');
                                                        dialog.dialog('destroy');
                                                        dialog.remove();                                                 
                                                    } else {
                                                        alert(status.error);
                                                    }
                                                });                                                
                                            },
                                            "Cancel": function() {
                                                $(this).dialog('close');
                                                $(this).dialog('destroy');
                                                $(this).remove();                        
                                            }
                                        }
                                    
                                    });
                                }),
                                triggerButtonSet = $(nRowData.triggerButtonSet = $('span#triggerButtonSet',nRow)).buttonset(),
                                jobCodeButton = $(nRowData.jobCodeButton = $("button#jobCodeButton",nRow)).button({
                                    text: false,
                                    icons: {
                                        primary: 'ui-icon-script'
                                    }
                                }).click(function() {
                                    alert('Not implemented yet!');
                                });
                                
                        });
                    }
                
                }));                   
        },        
        buildChainsContent: function() {
            var self = this,
                o = self.options,
                el = self.element,
                tabs = self.tabContents,
                chainRefreshButton = $(self.chainRefreshButton = $('button#refreshChain',tabs.chains)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-refresh"
                    }            
                }).click(function() {
                    if(self.chainSelect.val() === "") {
                        $.ruleChains.chain.GETlistChains({},function(chains) {
                            self.chainSelect.each(function() {
                                var firstOption = self.chainSelect.find('option:first-child').detach();
                                self.chainSelect.empty();
                                $.each(chains.chains.sort(function(a,b) {
                                    return a.name === b.name ? 0 : a.name < b.name ? -1 : 1;
                                }),function(index,chain) {
                                    self.chainSelect.append(
                                        $('<option />').val(chain.id).html(chain.name)
                                    );
                                });
                                self.chainSelect.prepend(firstOption).val("").change();
                            });                            
                        });
                    } else {
                        // refresh only the current chain
                        self.chainSelect.change();
                    }                    
                }),
                chainAddButton = $(self.chainAddButton = $('button#addChain',tabs.chains)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-plusthick"
                    }            
                }).click(function() {
                    $('<div />')
                    .data({
                        name: $('<input />',{
                            id: "name"
                        })
                    })
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Add New Chain',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<label />',{
                                    "for": "name"
                                })
                                .html('Name:')
                                .css({ "padding-right":"15px"})
                            ).append($(this).data().name);
                        },
                        buttons: {
                            "Create New Chain": function() {
                                var dialog = $(this);
                                $.ruleChains.chain.PUTaddChain({ 
                                        name: dialog.data().name.val().trim() 
                                    },
                                    function(chain) {
                                        if("chain" in chain) {                                            
                                            self.chainSelect.append(
                                                $('<option />').html(chain.chain.name).val(chain.chain.id)
                                            ).each(function() {
                                                var firstOption = self.chainSelect.find('option:first-child').detach();
                                                var options = self.chainSelect.children().detach();
                                                self.chainSelect.append(
                                                    options.sort(function(a,b) {
                                                        return a.text === b.text ? 0 : a.text < b.text ? -1 : 1;
                                                    })
                                                ).prepend(firstOption).val(chain.chain.id);
                                            }).change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                        
                                        } else {
                                            alert("Error: "+chain.error);
                                        }
                                    }
                                );
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                        
                            }
                        }
                    });
                
                }),   
                chainModifyButton = $(self.chainModifyButton = $('button#modifyChain',tabs.chains)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-wrench"
                    },
                    disabled: true
                }).click(function() {
                    $('<div />')
                    .data({
                        name: $('<input />',{
                            id: "name"
                        })
                    })
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Modify Chain Name',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<label />',{
                                    "for": "name"
                                })
                                .html('Name:')
                                .css({ "padding-right":"15px"})
                            ).append($(this).data().name.val(self.chainSelect.find('option:selected').text()));
                        },
                        buttons: {
                            "Rename Chain": function() {
                                var dialog = $(this);
                                $.ruleChains.chain.POSTmodifyChain({ 
                                        name: self.chainSelect.find('option:selected').text(),
                                        ruleSet: {
                                            name: dialog.data().name.val().trim()
                                        }
                                    },function(chain) {
                                        if("chain" in chain) {
                                            self.chainSelect.find('option:selected').html(chain.chain.name).end().each(function() {
                                                var firstOption = self.chainSelect.find('option:first-child').detach();
                                                var options = self.chainSelect.children().detach();
                                                self.chainSelect.append(
                                                    options.sort(function(a,b) {
                                                        return a.text === b.text ? 0 : a.text < b.text ? -1 : 1;
                                                    })
                                                ).prepend(firstOption).val(chain.chain.id);
                                            }).change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();
                                        } else {
                                            alert("Error: "+chain.error);
                                        }
                                    }
                                );                                
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                                                
                            }
                        }
                    });                                        
                }), 
                chainDeleteButton = $(self.chainDeleteButton = $('button#deleteChain',tabs.chains)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-trash"
                    },
                    disabled: true
                }).click(function() {
                    $('<div />')
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Delete Chain',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<span />').html("Are you SURE you want to delete Chain: '"+self.chainSelect.find('option:selected').text()+"'?")
                            );
                        },
                        buttons: {
                            "Confirm": function() {
                                var dialog = $(this);
                                $.ruleChains.chain.DELETEdeleteChain({
                                        name: self.chainSelect.find('option:selected').text()
                                    },function(status) {
                                        if("success" in status) {
                                            self.chainDataTable.fnClearTable();
                                            self.chainSelect.find('option:selected').remove().end().val("").removeData("chain").change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                    
                                        } else {
                                            alert(status.error);
                                        }
                                    }
                                );
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                                                
                            }
                        }
                    });                                    
                }),
                chainButtonSet = $('div#chainButtonSet',tabs.chains).buttonset(),
                chainSelect = $(self.chainSelect = $('select#chain',tabs.chains)).change(function() {
                    var select = $(this),
                        chainId = select.val(),
                        chainName = select.find('option:selected').text();
                    chainModifyButton.button("option","disabled",(chainId === ""));
                    chainDeleteButton.button("option","disabled",(chainId === ""));
                    // ruleSetRefreshButton.button("option","disabled",(ruleSetId === ""));
                    if(chainId !== "") {
                        self.chainTableButtonHeader.fadeIn();
                        $.ruleChains.chain.GETgetChain({
                                name: chainName
                            },
                            function(chain) {
                                if("chain" in chain) {
                                    self.chainSelect.data(chain);
                                    self.chainDataTable.fnClearTable();
                                    self.chainDataTable.fnAddData(chain.chain.links);
                                } else {
                                    alert(chain.error);
                                }
                            }
                        );
                    } else {
                        self.chainDataTable.fnClearTable();
                        self.chainTableButtonHeader.fadeOut();
                        self.chainSelect.removeData("chain");
                    }
                }),
                chainTable = $(self.chainTable = $('table#chainTable',tabs.chains).width('100%')),
                chainTableButtonHeader = $(self.chainTableButtonHeader = self.chainTable.find('thead').find('tr:first')).hide(),
                linkAddButton = $(self.linkAddButton = $('button#addLink',chainTable)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-plusthick"
                    }            
                }).click(function() {
                    $.ruleChains.ruleSet.GETlistRuleSets({},function(ruleSets) {
                        $('<div />')
                        .data({
                            targetSequenceNumber: $('<select />',{
                                name: "targetSequenceNumber",
                                id: "targetSequenceNumber"
                            }).each(function(index,select) {
                                var sequence = $.each($.map(self.chainDataTable.fnGetData(),function(val,index) {
                                    return val.sequenceNumber;
                                }).sort(function(a,b) {
                                    return a === b ? 0 : a < b ? -1 : 1;
                                }),function(index,sequenceNumber) {
                                    $(select).append($('<option />').val(sequenceNumber).html(sequenceNumber));
                                });
                                $(select).append($('<option />').val((sequence.length)?Math.max.apply( null, sequence)+1:1).html((sequence.length)?Math.max.apply( null, sequence)+1:1));
                            }),
                            ruleSelect: $('<select />',{
                                name: "ruleSelect",
                                id: "ruleSelect"
                            }).each(function(index,select) {
                                $.each(ruleSets.ruleSets.sort(function(a,b) {
                                    return (a.name > b.name) ? 1 : (a.name < b.name) ? -1 : 0;
                                }),function(rsi,ruleSet) {
                                    var optgroup = $('<optgroup />',{
                                        "label": ruleSet.name
                                    }).appendTo($(select));
                                    $.each(ruleSet.rules.sort(function(a,b) {
                                        return (a.name > b.name) ? 1 : (a.name < b.name) ? -1 : 0;
                                    }),function(ri,rule) {
                                        $('<option />').html(rule.name).val(rule.id).appendTo(optgroup);
                                    });
                                });
                            }),
                            sourceSelect: $('<select />',{
                                name: "sourceSelect",
                                id: "sourceSelect"
                            }).each(function(index,select) {
                                $.each(self.sources.sort(function(a,b) {
                                    return (a > b) ? 1 : (a < b) ? -1 : 0;
                                }),function(si,source) {
                                    $('<option />').val(source).html(source).appendTo($(select));
                                });
                            })
                        })
                        .addClass('ui-state-default ui-widget-content')
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
                            autoOpen: true,
                            bgiframe: true,
                            resizable: false,
                            title: 'Add New Link',
                            height:280,
                            width:400,
                            modal: true,
                            zIndex: 3999,
                            overlay: {
                                backgroundColor: '#000',
                                opacity: 0.5
                            },
                            open: function() {
                                $(this).append(
                                    $('<p />')
                                    .append(
                                        $('<label />',{
                                            "for": "targetSequenceNumber"
                                        })
                                        .html('Target Sequence Number:')
                                        .css({ "padding-right":"15px"})
                                    ).append($(this).data().targetSequenceNumber.css({ "float": "right" }))
                                )
                                .append(
                                    $('<p />')
                                    .append(
                                        $('<label />',{
                                            "for": "ruleSelect"
                                        })
                                        .html('Rule:')
                                        .css({ "padding-right":"15px"})
                                    ).append($(this).data().ruleSelect.css({ "float": "right" }))
                                )
                                .append(
                                    $('<p />')
                                    .append(
                                        $('<label />',{
                                            "for": "sourceSelect"
                                        })
                                        .html('Source:')
                                        .css({ "padding-right":"15px"})
                                    ).append($(this).data().sourceSelect.css({ "float": "right" }))                            
                                )
                                ;
                            },
                            buttons: {
                                "Add Link": function() {
                                    var dialog = $(this),
                                        json = {                                            
                                            name : self.chainSelect.find('option:selected').text(),
                                            link: {
                                                sequenceNumber: dialog.data().targetSequenceNumber.val().trim(),
                                                rule: {
                                                    name: dialog.data().ruleSelect.find('option:selected').text()
                                                },
                                                sourceName: dialog.data().sourceSelect.val().trim(),
                                                executeEnum: 'NORMAL',
                                                linkEnum: "NONE",
                                                resultEnum: "NONE"
                                            }
                                        };
                                    $.ruleChains.chain.PUTaddChainLink(json,function(chain) {
                                        if("chain" in chain) {
//                                            $(self.chainDataTable.fnGetNodes( )).each(function (index,r) {
//                                                if ( self.chainDataTable.fnIsOpen(r) ) {
//                                                    $(r).find('button#details').click();
//                                                }                                                
//                                            });
                                            // alert(JSON.stringify(chain.chain.links));
                                            self.chainDataTable.fnClearTable();
                                            self.chainDataTable.fnAddData(chain.chain.links);
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                    
                                        } else {
                                            alert(chain.error);
                                        }
                                    });
                                },
                                "Cancel": function() {
                                    $(this).dialog('close');
                                    $(this).dialog('destroy');
                                    $(this).remove();                                                                        
                                }
                            }
                        });                    

                    });
                }),
                linkMoveButton = $(self.linkMoveButton = $('button#moveLink',chainTable)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-transferthick-e-w"
                    },
                    disabled: true
                }).click(function() {
                    $.ruleChains.chain.GETlistChains({},function(chains) {
                        $('<div />')
                        .data({
                            targetSequenceNumber: $('<select />'),
                            chainDestinationSequenceNumberSelect: $('<select />',{
                                name: "chainSelect",
                                id: "chainSelect"
                            }).each(function(index,select) {
                                $.each(chains.chains.sort(function(a,b) {
                                    return (a.name > b.name) ? 1 : (a.name < b.name) ? -1 : 0;
                                }),function(rsi,chain) {
                                    var aData = self.chainDataTable.fnGetData($.grep(self.chainDataTable.fnGetNodes(),function(tr) {
                                            return $(tr).hasClass('ui-widget-shadow');
                                        })[0]),
                                        optgroup = $('<optgroup />',{
                                            "label": chain.name
                                        }).appendTo($(select));
                                    $.each(
                                        {
                                            buildOptionLinks: function(links) {
                                                if(chain.name === self.chainSelect.find('option:selected').text()) {
                                                    if(links.length < 2) {
                                                        return $.grep(chain.links,function(link,index) { return link.sequenceNumber !== aData.sequenceNumber; });
                                                    } else {
                                                        var filteredLinks = $.grep(chain.links,function(link,index) { return link.sequenceNumber !== aData.sequenceNumber; });
                                                        filteredLinks.push({
                                                            sequenceNumber: Math.max.apply( null, $.map(links,function(val,index) { return val.sequenceNumber; }))+1
                                                        });
                                                        return filteredLinks;
                                                    }
                                                } else {
                                                    // Add last possible value
                                                    links.push({
                                                        sequenceNumber: (links.length < 1)?1:(Math.max.apply( null, $.map(links,function(val,index) { return val.sequenceNumber; }))+1)
                                                    });
                                                    return links;
                                                }
                                            }
                                        }.buildOptionLinks(chain.links).sort(
                                            function(a,b) {
                                                return (a.sequenceNumber > b.sequenceNumber) ? 1 : (a.sequenceNumber < b.sequenceNumber) ? -1 : 0;
                                            }
                                        ),
                                        function(ri,link) {
                                            $('<option />').html(link.sequenceNumber).val(link.sequenceNumber).appendTo(optgroup);
                                        }
                                    );
                                });
                            })
                        })                        
                        .addClass('ui-state-default ui-widget-content')
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
                            autoOpen: true,
                            bgiframe: true,
                            resizable: false,
                            title: 'Move Link',
                            height:280,
                            width:400,
                            modal: true,
                            zIndex: 3999,
                            overlay: {
                                backgroundColor: '#000',
                                opacity: 0.5
                            },
                            open: function() {
                                $(this)
                                .append(
                                    $('<label />',{
                                        "for": "chainSelect"
                                    }).html("Select Destination Sequence Number")
                                )
                                .append($(this).data().chainDestinationSequenceNumberSelect);
                            }
                        });
                            
                    });
                }),
                linkDeleteButton = $(self.linkDeleteButton = $('button#deleteLink',chainTable)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-trash"
                    },
                    disabled: true
                }).click(function() {
                    var aData = self.chainDataTable.fnGetData($.grep(self.chainDataTable.fnGetNodes(),function(tr) {
                            return $(tr).hasClass('ui-widget-shadow');
                        })[0]),
                        json = {
                            name: self.chainSelect.find('option:selected').text(),
                            sequenceNumber: aData.sequenceNumber
                        };
                    $.ruleChains.chain.DELETEdeleteChainLink(json,function(chain) {
                        if("chain" in chain) {
                            self.chainDataTable.fnClearTable();
                            self.chainDataTable.fnAddData(chain.chain.links);  
                            self.linkDeleteButton.button("option","disabled",true);
                            self.linkMoveButton.button("option","disabled",true);
                        } else {
                            alert(chain.error);
                        }
                    });
                }),
                linkButtonSet = $('div#linkButtonSet',chainTable).buttonset(),
                chainDataTable = $(self.chainDataTable = chainTable.dataTable({
                    "aoColumns": [
                        { "bSortable": false,"bVisible": true,"mDataProp": null, "fnRender":
                            function(oObj) {
                                return "<button type='button' id='details' />";
                            }
                        },                        
                        { "bVisible": true,"mDataProp": "sequenceNumber","sDefaultContent":"","aDataSort": [ 1 ],"asSorting": [ "asc" ] },
                        { "bVisible": true,"mDataProp": null,"sDefaultContent":"","fnRender": 
                            function(oObj) {
                                return oObj.aData.rule.class.split("\.").pop();
                            }                             
                        },
                        { "bVisible": true,"mDataProp": null,"fnRender": 
                            function(oObj) {
                                var div = $('<div />'),
                                    select = $('<select />',{
                                        id: "sourceName",
                                        name: "sourceName"
                                    }).appendTo(div);
                                $.each(self.sources.sort(function(a,b) {
                                    return (a > b) ? 1 : (a < b) ? -1 : 0;
                                }),function(si,name) {                                
                                    $('<option />').val(name).html(name).appendTo(select).prop("selected", (name === oObj.aData.sourceName));
                                });                            
                                return div.html();
                            } 
                        },
                        { "bVisible": true,"mDataProp": null,"fnRender": 
                            function(oObj) {
                                var div = $('<div />'),
                                    select = $('<select />',{
                                        id: "executeAction",
                                        name: "executeAction"
                                    }).appendTo(div);
                                $.each(self.actions.execute.sort(function(a,b) {
                                    return (a > b) ? 1 : (a < b) ? -1 : 0;
                                }),function(si,name) {                                
                                    $('<option />').val(name).html(name).appendTo(select).prop("selected", (name === oObj.aData.sourceName));
                                });                            
                                return div.html();
                            } 
                        },
                        { "bVisible": true,"mDataProp": null,"fnRender": 
                            function(oObj) {
                                var div = $('<div />'),
                                    select = $('<select />',{
                                        id: "resultAction",
                                        name: "resultAction"
                                    }).appendTo(div);
                                $.each(self.actions.result.sort(function(a,b) {
                                    return (a > b) ? 1 : (a < b) ? -1 : 0;
                                }),function(si,name) {                                
                                    $('<option />').val(name).html(name).appendTo(select).prop("selected", (name === oObj.aData.sourceName));
                                });                            
                                return div.html();
                            } 
                        },
                        { "bVisible": true,"mDataProp": null,"fnRender": 
                            function(oObj) {
                                var div = $('<div />'),
                                    select = $('<select />',{
                                        id: "linkAction",
                                        name: "linkAction"
                                    }).appendTo(div);
                                $.each(self.actions.link.sort(function(a,b) {
                                    return (a > b) ? 1 : (a < b) ? -1 : 0;
                                }),function(si,name) {                                
                                    $('<option />').val(name).html(name).appendTo(select).prop("selected", (name === oObj.aData.sourceName));
                                });                            
                                return div.html();
                            } 
                        }
                    ],
                    "bJQueryUI": true,
                    "asStripeClasses": [ 'ui-priority-primary', 'ui-priority-secondary' ],
                    "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                        return $(nRow)
                        .unbind('click')
                        .click(function(event) {
                            if($(nRow).hasClass('ui-widget-shadow')) {
                                $(nRow).removeClass('ui-widget-shadow');
                                self.linkMoveButton.button("option","disabled",true);
                                self.linkDeleteButton.button("option","disabled",true);
                            } else {
                                $(self.chainDataTable.fnGetNodes( )).each(function() {
                                    $(this).removeClass('ui-widget-shadow');
                                });
                                $(nRow).addClass('ui-widget-shadow');                                    
                                self.linkMoveButton.button("option","disabled",false);
                                self.linkDeleteButton.button("option","disabled",false);
                            }
                        })
                        .find('button#details').click(function(event) { event.stopPropagation(); }).end()
                        .find('select#sourceName').click(function(event) { event.stopPropagation(); }).end()
                        .find('select#executeAction').click(function(event) { event.stopPropagation(); }).end()
                        .find('select#resultAction').click(function(event) { event.stopPropagation(); }).end()
                        .find('select#linkAction').click(function(event) { event.stopPropagation(); }).end()
                        .each(function() {
                            var nRowData = $(nRow).data(),
                                detailsButton = $(nRowData.detailsButton = $(this).find('button#details'))
                                .button({
                                    text: false,
                                    icons: {
                                        primary: 'ui-icon-circle-triangle-e'
                                    }
                                })
                                .unbind('toggle')
                                .toggle(
                                    function() {
                                        nRowData.detailsButton.button( "option", "icons", {
                                            primary: 'ui-icon-circle-triangle-s'
                                        });
                                        $(self.chainDataTable.fnGetNodes( )).each(function (index,r) {
                                            if ( self.chainDataTable.fnIsOpen(r) ) {
                                                $(r).find('button#details').click();
                                            }                                                
                                        });             
                                        $.ruleChains.ruleSet.GETlistRuleSets({},function(ruleSets) {
                                            $(nRowData.detailsRow = $(self.chainDataTable.fnOpen(
                                                nRow,
                                                "<fieldset id='chainDetails' />",
                                                'ui-widget-header'))
                                            )
                                            .find('fieldset#chainDetails')
                                            .addClass('ui-widget-content')
                                            .append(
                                                $('<legend />')
                                                .html('Chain Details')
                                                .addClass('ui-widget-header ui-corner-all')
                                            )
                                            .each(function() {
                                                $(nRowData.detailsFieldset = $(this))
                                                .append(
                                                    $('<fieldset />')
                                                    .addClass('ui-widget-content')
                                                    .append(
                                                        $('<legend />')
                                                        .html('Target Rule')
                                                        .addClass('ui-widget-header ui-corner-all')
                                                    )
                                                    .append(
                                                        $(nRowData.selectRule = $('<select />')).each(function(index,select) {
                                                            $.each(ruleSets.ruleSets.sort(function(a,b) {
                                                                return (a.name > b.name) ? 1 : (a.name < b.name) ? -1 : 0;
                                                            }),function(rsi,ruleSet) {
                                                                var optgroup = $('<optgroup />',{
                                                                    "label": ruleSet.name
                                                                }).appendTo($(select));
                                                                $.each(ruleSet.rules.sort(function(a,b) {
                                                                    return (a.name > b.name) ? 1 : (a.name < b.name) ? -1 : 0;
                                                                }),function(ri,rule) {
                                                                    $('<option />').html(rule.name).val(rule.id).appendTo(optgroup)
                                                                    .prop('selected', (rule.id === aData.rule.id) && (ruleSet.name === aData.rule.ruleSet.name));
                                                                });
                                                            });
                                                        }).change(function() {
                                                            $.ruleChains.ruleSet.GETgetRule({
                                                                name: $(this).find('option:selected').text(),
                                                                ruleSetName: $(this).find('option:selected').parent().attr("label")
                                                            },function(rule) {
                                                                if("rule" in rule) {
                                                                    // Update the link with a new rule
                                                                    aData.rule = rule.rule;
                                                                    $.ruleChains.chain.POSTmodifyChainLink({
                                                                        name: self.chainSelect.find('option:selected').text(),
                                                                        link: aData                                                                            
                                                                    },function(link) {
                                                                        if("link" in link) {
                                                                            if(self.chainDataTable.fnIsOpen(nRow)) {
                                                                                nRowData.detailsButton.click();
                                                                                self.chainDataTable.fnUpdate(link.link,nRow);
                                                                                nRowData.detailsButton.click();
                                                                            } else {
                                                                                self.chainDataTable.fnUpdate(link.link,nRow);
                                                                            }
                                                                        } else {
                                                                            alert(link.error);
                                                                        }
                                                                    });
                                                                } else {
                                                                    alert(rule.error);
                                                                }
                                                            });
                                                        })
                                                    )
                                                    .append(
                                                        $(nRowData.targetRule = $('<pre />')).addClass('ui-widget-content').text(aData.rule.rule)
                                                    )
                                                )
                                                .append('<br />')
                                                .append(
                                                    $(nRowData.inputReorderSide = $('<fieldset />'))
                                                    .css({
                                                        'float': 'left',
                                                        'width': '48%'
                                                    })
                                                    .addClass('ui-widget-content')
                                                    .append(
                                                        $('<legend />')
                                                        .html('Input Reorder')
                                                        .addClass('ui-widget-header ui-corner-all')
                                                    )
                                                    .append(
                                                        $(nRowData.inputReorder = $('<textarea />')).html(aData.inputReorder)
                                                    )
                                                )
                                                .append(
                                                    $(nRowData.outputReorderSide = $('<fieldset />'))
                                                    .css({
                                                        'float': 'right',
                                                        'width': '48%'
                                                    })
                                                    .addClass('ui-widget-content')
                                                    .append(
                                                        $('<legend />')
                                                        .html('Output Reorder')
                                                        .addClass('ui-widget-header ui-corner-all')
                                                    )
                                                    .append(
                                                        $(nRowData.outputReorder = $('<textarea />')).html(aData.outputReorder)
                                                    )
                                                );
                                                nRowData.inputEditor = new CodeMirrorUI(nRowData.inputReorder.get(0),{ 
                                                    path : 'js/codemirror-ui/js', 
                                                    imagePath: 'js/codemirror-ui/images/silk', 
                                                    searchMode : 'inline',
                                                    buttons : ['save','undo','redo','jump','reindent','about'],
                                                    saveCallback: function() {    
                                                        $.ruleChains.chain.POSTmodifyChainLink({ 
                                                            name: self.chainSelect.find('option:selected').text(),
                                                            link: $.extend(aData,{
                                                                inputReorder: nRowData.inputEditor.mirror.getValue()
                                                            })
                                                        },function(link) {
                                                            if("link" in link) {
                                                                if(self.chainDataTable.fnIsOpen(nRow)) {
                                                                    nRowData.detailsButton.click();
                                                                    self.chainDataTable.fnUpdate(link.link,nRow);
                                                                    nRowData.detailsButton.click();
                                                                } else {
                                                                    self.chainDataTable.fnUpdate(link.link,nRow);
                                                                }
                                                            } else {
                                                                alert(link.error);
                                                            }                                                                
                                                        });                                    
                                                    }
                                                },{ 
                                                    mode: "text/x-groovy",
                                                    tabMode: "indent",
                                                    lineNumbers: true,
                                                    matchBrackets: true,
                                                    theme: 'ambiance'
                                                });
                                                nRowData.outputEditor = new CodeMirrorUI(nRowData.outputReorder.get(0),{ 
                                                    path : 'js/codemirror-ui/js', 
                                                    imagePath: 'js/codemirror-ui/images/silk', 
                                                    searchMode : 'inline',
                                                    buttons : ['save','undo','redo','jump','reindent','about'],
                                                    saveCallback: function() {    
                                                        $.ruleChains.chain.POSTmodifyChainLink({ 
                                                            name: self.chainSelect.find('option:selected').text(),
                                                            link: $.extend(aData,{
                                                                outputReorder: nRowData.outputEditor.mirror.getValue()
                                                            })
                                                        },function(link) {
                                                            if("link" in link) {
                                                                if(self.chainDataTable.fnIsOpen(nRow)) {
                                                                    nRowData.detailsButton.click();
                                                                    self.chainDataTable.fnUpdate(link.link,nRow);
                                                                    nRowData.detailsButton.click();
                                                                } else {
                                                                    self.chainDataTable.fnUpdate(link.link,nRow);
                                                                }
                                                            } else {
                                                                alert(link.error);
                                                            }                                                                
                                                        });                                    
                                                    }
                                                },{ 
                                                    mode: "text/x-groovy",
                                                    tabMode: "indent",
                                                    lineNumbers: true,
                                                    matchBrackets: true,
                                                    theme: 'ambiance'
                                                });
                                            });                                
                                        });
                                    },
                                    function() {    
                                        // Close the details
                                        nRowData.detailsButton.button( "option", "icons", {
                                            primary: 'ui-icon-circle-triangle-e'
                                        });
                                        self.chainDataTable.fnClose(nRow);                                
                                    }           
                                ),
                                sourceNameSelect = $(nRowData.sourceNameSelect = $("select#sourceName",nRow)).val(aData.sourceName).change(function() {
                                    var select = $(this),
                                        sourceName = select.val();
                                    $.ruleChains.chain.POSTmodifyChainLink({ 
                                        name: self.chainSelect.find('option:selected').text(),
                                        link: $.extend(aData,{
                                            sourceName: sourceName
                                        })
                                    },function(link) {
                                        if("link" in link) {
                                            // alert("Select value was updated");
                                        } else {
                                            alert(link.error);
                                        }                                                                
                                    });                                    
                                }),
                                executeActionSelect = $(nRowData.executeActionSelect = $("select#executeAction",nRow)).val(aData.executeEnum.name).change(function() {
                                    var select = $(this),
                                        executeAction = select.val();
                                    $.ruleChains.chain.POSTmodifyChainLink({ 
                                        name: self.chainSelect.find('option:selected').text(),
                                        link: $.extend(aData,{
                                            executeEnum: executeAction
                                        })
                                    },function(link) {
                                        if("link" in link) {
                                            // alert("Select value was updated");
                                        } else {
                                            alert(link.error);
                                        }                        
                                    });
                                }),
                                resultActionSelect = $(nRowData.resultActionSelect = $("select#resultAction",nRow)).val(aData.resultEnum.name).change(function() {
                                    var select = $(this),
                                        resultAction = select.val();
                                    $.ruleChains.chain.POSTmodifyChainLink({ 
                                        name: self.chainSelect.find('option:selected').text(),
                                        link: $.extend(aData,{
                                            resultEnum: resultAction
                                        })
                                    },function(link) {
                                        if("link" in link) {
                                            // alert("Select value was updated");
                                        } else {
                                            alert(link.error);
                                        }                        
                                    });
                                }),
                                linkActionSelect = $(nRowData.linkActionSelect = $("select#linkAction",nRow)).val(aData.linkEnum.name).change(function() {
                                    var select = $(this),
                                        linkAction = select.val();
                                    $.ruleChains.chain.POSTmodifyChainLink({ 
                                        name: self.chainSelect.find('option:selected').text(),
                                        link: $.extend(aData,{
                                            linkEnum: linkAction
                                        })
                                    },function(link) {
                                        if("link" in link) {
                                            // alert("Select value was updated");
                                        } else {
                                            alert(link.error);
                                        }                        
                                    });
                                });
                        });
                    }
                }));
            
        },        
        buildRuleSetsContent: function() {
            var self = this,
                o = self.options,
                el = self.element,
                tabs = self.tabContents,
                ruleSetRefreshButton = $(self.ruleSetRefreshButton = $('button#refreshSet',tabs.ruleSets)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-refresh"
                    }            
                }).click(function() {
                    if(self.ruleSetSelect.val() === "") {
                        $.ruleChains.ruleSet.GETlistRuleSets({},function(ruleSets) {
                            self.ruleSetSelect.each(function() {
                                var firstOption = self.ruleSetSelect.find('option:first-child').detach();
                                self.ruleSetSelect.empty();
                                $.each(ruleSets.ruleSets.sort(function(a,b) {
                                    return a.name === b.name ? 0 : a.name < b.name ? -1 : 1;
                                }),function(index,ruleSet) {
                                    self.ruleSetSelect.append(
                                        $('<option />').val(ruleSet.id).html(ruleSet.name)
                                    );
                                });
                                self.ruleSetSelect.prepend(firstOption).val("").change();
                            });
                        });                                                
                    } else {
                        // refresh only the current set
                        self.ruleSetSelect.change();
                    }
                }),
                ruleSetAddButton = $(self.ruleSetAddButton = $('button#addSet',tabs.ruleSets)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-plusthick"
                    }            
                }).click(function() {
                    $('<div />')
                    .data({
                        name: $('<input />',{
                            id: "name"
                        })
                    })
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Add New Rule Set',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<label />',{
                                    "for": "name"
                                })
                                .html('Name:')
                                .css({ "padding-right":"15px"})
                            ).append($(this).data().name);
                        },
                        buttons: {
                            "Create New Rule Set": function() {
                                var dialog = $(this);
                                $.ruleChains.ruleSet.PUTaddRuleSet({ 
                                        name: dialog.data().name.val().trim() 
                                    },
                                    function(ruleSet) {
                                        if("ruleSet" in ruleSet) {                                            
                                            self.ruleSetSelect.append(
                                                $('<option />').html(ruleSet.ruleSet.name).val(ruleSet.ruleSet.id)
                                            ).each(function() {
                                                var firstOption = self.ruleSetSelect.find('option:first-child').detach();
                                                var options = self.ruleSetSelect.children().detach();
                                                self.ruleSetSelect.append(
                                                    options.sort(function(a,b) {
                                                        return a.text === b.text ? 0 : a.text < b.text ? -1 : 1;
                                                    })
                                                ).prepend(firstOption).val(ruleSet.ruleSet.id);
                                            }).change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                        
                                        } else {
                                            alert("Error: "+ruleSet.error);
                                        }
                                    }
                                );
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                        
                            }
                        }
                    });
                        
                
                }),
                ruleSetModifyButton = $(self.ruleSetModifyButton = $('button#modifySet',tabs.ruleSets)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-wrench"
                    },
                    disabled: true
                }).click(function() {
                    $('<div />')
                    .data({
                        name: $('<input />',{
                            id: "name"
                        })
                    })
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Modify Rule Set Name',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<label />',{
                                    "for": "name"
                                })
                                .html('Name:')
                                .css({ "padding-right":"15px"})
                            ).append($(this).data().name.val(self.ruleSetSelect.find('option:selected').text()));
                        },
                        buttons: {
                            "Rename RuleSet": function() {
                                var dialog = $(this);
                                $.ruleChains.ruleSet.POSTmodifyRuleSet({ 
                                        name: self.ruleSetSelect.find('option:selected').text(),
                                        ruleSet: {
                                            name: dialog.data().name.val().trim()
                                        }
                                    },function(ruleSet) {
                                        if("ruleSet" in ruleSet) {
                                            self.ruleSetSelect.find('option:selected').html(ruleSet.ruleSet.name).end().each(function() {
                                                var firstOption = self.ruleSetSelect.find('option:first-child').detach();
                                                var options = self.ruleSetSelect.children().detach();
                                                self.ruleSetSelect.append(
                                                    options.sort(function(a,b) {
                                                        return a.text === b.text ? 0 : a.text < b.text ? -1 : 1;
                                                    })
                                                ).prepend(firstOption).val(ruleSet.ruleSet.id);
                                            }).change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();
                                        } else {
                                            alert("Error: "+ruleSet.error);
                                        }
                                    }
                                );                                
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                                                
                            }
                        }
                    });                    
                }),
                ruleSetDeleteButton = $(self.ruleSetDeleteButton = $('button#deleteSet',tabs.ruleSets)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-trash"
                    },
                    disabled: true
                }).click(function() {
                    $('<div />')
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Delete Rule Set',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<span />').html("Are you SURE you want to delete RuleSet: '"+self.ruleSetSelect.find('option:selected').text()+"'?")
                            );
                        },
                        buttons: {
                            "Confirm": function() {
                                var dialog = $(this);
                                $.ruleChains.ruleSet.DELETEdeleteRuleSet({
                                        name: self.ruleSetSelect.find('option:selected').text()
                                    },function(status) {
                                        if("success" in status) {
                                            self.ruleDataTable.fnClearTable();
                                            self.ruleSetSelect.find('option:selected').remove().end().val("").removeData("ruleSet").change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                    
                                        } else {
                                            alert(status.error);
                                        }
                                    }
                                );
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                                                
                            }
                        }
                    });                    
                }),
                ruleSetButtonSet = $('div#ruleSetButtonSet',tabs.ruleSets).buttonset(),
                ruleSetSelect = $(self.ruleSetSelect = $('select#ruleSet',tabs.ruleSets)).change(function() {
                    var select = $(this),
                        ruleSetId = select.val(),
                        ruleSetName = select.find('option:selected').text();
                    ruleSetModifyButton.button("option","disabled",(ruleSetId === ""));
                    ruleSetDeleteButton.button("option","disabled",(ruleSetId === ""));
                    // ruleSetRefreshButton.button("option","disabled",(ruleSetId === ""));
                    if(ruleSetId !== "") {
                        self.ruleTableButtonHeader.fadeIn();
                        $.ruleChains.ruleSet.GETgetRuleSet({
                                name: ruleSetName
                            },
                            function(ruleSet) {
                                if("ruleSet" in ruleSet) {
                                    self.ruleSetSelect.data(ruleSet);
                                    self.ruleDataTable.fnClearTable();
                                    self.ruleDataTable.fnAddData(ruleSet.ruleSet.rules);
                                } else {
                                    alert(ruleSet.error);
                                }
                            }
                        );
                    } else {
                        self.ruleDataTable.fnClearTable();
                        self.ruleTableButtonHeader.fadeOut();
                        self.ruleSetSelect.removeData("ruleSet");
                    }
                }),
                ruleTable = $(self.ruleTable = $('table#ruleTable').width('100%')),
                ruleTableButtonHeader = $(self.ruleTableButtonHeader = self.ruleTable.find('thead').find('tr:first')).hide(),
                ruleAddButton = $(self.ruleAddButton = $('button#addRule',ruleTable)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-plusthick"
                    }            
                }).click(function() {
                    $('<div />')
                    .data({
                        name: $('<input />',{
                            id: "name"
                        }),
                        serviceType: $('<select />').each(function(index,select) {
                            $.each([ 
                                { id: 'SQLQUERY',name:"SQL Query" }, 
                                { id: 'NAMEDQUERY',name:"Named Query" }, 
                                { id: 'GROOVY',name:"Groovy Script" }, 
                                { id: 'DEFINEDSERVICE', name: "Defined Service" },
                                { id: 'SNIPPET', name: "Snippet"} 
                            ],function(index,type) {
                                $(select).append($('<option />').val(type.id).html(type.name));
                            });
                        })
                    })
                    .addClass('ui-state-default ui-widget-content')
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
                        autoOpen: true,
                        bgiframe: true,
                        resizable: false,
                        title: 'Add New Rule',
                        height:280,
                        width:400,
                        modal: true,
                        zIndex: 3999,
                        overlay: {
                            backgroundColor: '#000',
                            opacity: 0.5
                        },
                        open: function() {
                            $(this).append(
                                $('<label />',{
                                    "for": "name"
                                })
                                .html('Name:')
                                .css({ "padding-right":"15px"})
                            ).append($(this).data().name)
                            .append(
                                $('<label />',{
                                    "for": "serviceType"
                                })
                                .html('Service Type:')
                                .css({ "padding-right":"15px"})
                            ).append($(this).data().serviceType);
                            
                        },
                        buttons: {
                            "Add Rule": function() {
                                var dialog = $(this),
                                    json = {
                                        name : dialog.data().name.val().trim(),
                                        serviceType : dialog.data().serviceType.val().trim(),
                                        ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                    };
                                if(json.name.length > 2) {
                                    $.ruleChains.ruleSet.PUTaddRule(json,function(rule) {
                                        if("rule" in rule) {
                                            $(self.ruleDataTable.fnGetNodes( )).each(function (index,r) {
                                                if ( self.ruleDataTable.fnIsOpen(r) ) {
                                                    $(r).find('button#details').click();
                                                }                                                
                                            });
                                            self.ruleDataTable.fnAddData(rule.rule);
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                                                                
                                        } else {
                                            alert(rule.error);
                                        }
                                    });
                                } else {
                                    alert("You must provide a rule name longer than 2 charaters");
                                }
                            },
                            "Cancel": function() {
                                $(this).dialog('close');
                                $(this).dialog('destroy');
                                $(this).remove();                                                                        
                            }
                        }
                    });                    
                }),
                ruleMoveButton = $(self.ruleMoveButton = $('button#moveRule',ruleTable)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-transferthick-e-w"
                    },
                    disabled: true
                }).click(function() {
                    if($('option',self.ruleSetSelect).length < 3) {
                        alert("You must have more than 1 rule set to perform a move");
                    } else {
                        $('<div />')
                        .data({
                            targetRuleSet: $('<select />',{ "id": "targetRuleSet" }).append($.grep(self.ruleSetSelect.clone().children(),function(opt,idx) {
                                return $.inArray($(opt).val(), ["",self.ruleSetSelect.val()]) === -1;
                            }))
                        })
                        .addClass('ui-state-default ui-widget-content')
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
                            autoOpen: true,
                            bgiframe: true,
                            resizable: false,
                            title: 'Move Rule',
                            height:280,
                            width:400,
                            modal: true,
                            zIndex: 3999,
                            overlay: {
                                backgroundColor: '#000',
                                opacity: 0.5
                            },
                            open: function() {
                                $(this).append(
                                    $('<label />',{
                                        "for": "targetRuleSet"
                                    })
                                    .html('Target Rule Set:')
                                    .css({ "padding-right":"15px"})
                                ).append($(this).data().targetRuleSet);
                            },
                            buttons: {
                                "Move Rule to Another Set": function() {
                                    var dialog = $(this),
                                        json = {
                                            name: $('td:eq(2)',$.grep(self.ruleDataTable.fnGetNodes(),function(tr) {
                                                return $(tr).hasClass('ui-widget-shadow');
                                            })[0]).html(),
                                            newName : dialog.data().targetRuleSet.find('option:selected').text(),
                                            ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                        };                                    
                                    $.ruleChains.ruleSet.PUTmoveRule(json,function(rule) {
                                        if("rule" in rule) {
                                            self.ruleSetSelect.val(rule.rule.ruleSet.id).change();
                                            dialog.dialog('close');
                                            dialog.dialog('destroy');
                                            dialog.remove();                                                                                                                                                                                                            
                                        } else {
                                            alert(rule.error);
                                        }                        
                                    });
                                },
                                "Cancel": function() {
                                    $(this).dialog('close');
                                    $(this).dialog('destroy');
                                    $(this).remove();                                                                        
                                }
                            }
                        });                    
                    }
                }),
                ruleDeleteButton = $(self.ruleDeleteButton = $('button#deleteRule',ruleTable)).button({
                    text: false,
                    icons: {
                        primary: "ui-icon-trash"
                    },
                    disabled: true
                }).click(function() {
                    $.ruleChains.ruleSet.DELETEdeleteRule({
                        name: $('td:eq(2)',$.grep(self.ruleDataTable.fnGetNodes(),function(tr) {
                            return $(tr).hasClass('ui-widget-shadow');
                        })[0]).html(),
                        ruleSetName: self.ruleSetSelect.find('option:selected').text()                        
                    },function(status) {
                        if("status" in status) {
                            $(self.ruleDataTable.fnGetNodes( )).each(function (index,r) {
                                if ( self.ruleDataTable.fnIsOpen(r) ) {
                                    $(r).find('button#details').click();
                                }                                                
                            });
                            self.ruleDataTable.fnDeleteRow($.grep(self.ruleDataTable.fnGetNodes(),function(tr) {
                                return $(tr).hasClass('ui-widget-shadow');
                            })[0]);
                            dialog.dialog('close');
                            dialog.dialog('destroy');
                            dialog.remove();                                                                                                                                                                
                        } else {
                            alert(status.error);
                        }                        
                    });
                }),                
                ruleButtonSet = $('div#ruleButtonSet',ruleTable).buttonset(),
                ruleDataTable = $(self.ruleDataTable = ruleTable.dataTable({
                    "aoColumns": [
                        { "bSortable": false,"bVisible": true,"mDataProp": null, "fnRender":
                            function(oObj) {
                                return "<button type='button' id='details' />";
                            }
                        },                        
                        { "bVisible": true,"mDataProp": "id","sDefaultContent":"","aDataSort": [ 0 ],"asSorting": [ "asc" ] },
                        { "bVisible": true,"mDataProp": "name","sDefaultContent":"" },
                        { "bVisible": true,"mDataProp": null,"sDefaultContent":"","fnRender": 
                            function(oObj) {
                                return oObj.aData.class.split("\.").pop();
                            }                             
                        }
                    ],
                    "bJQueryUI": true,
                    "asStripeClasses": [ 'ui-priority-primary', 'ui-priority-secondary' ],
                    "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                        return $(nRow)
                        .unbind('click')
                        .click(function(event) {
                            if($(nRow).hasClass('ui-widget-shadow')) {
                                $(nRow).removeClass('ui-widget-shadow');
                                self.ruleMoveButton.button("option","disabled",true);
                                self.ruleDeleteButton.button("option","disabled",true);
                            } else {
                                $(self.ruleDataTable.fnGetNodes( )).each(function() {
                                    $(this).removeClass('ui-widget-shadow');
                                });
                                $(nRow).addClass('ui-widget-shadow');                                    
                                self.ruleMoveButton.button("option","disabled",false);
                                self.ruleDeleteButton.button("option","disabled",false);
                            }
                        }) 
                        .find('button#details').click(function(event) { event.stopPropagation(); }).end()
                        .find('td:eq(2)').click(function(event) { event.stopPropagation(); }).end()
                        .each(function() {
                            var nRowData = $(nRow).data(),
                                detailsButton = $(nRowData.detailsButton = $(this).find('button#details'))
                                .button({
                                    text: false,
                                    icons: {
                                        primary: 'ui-icon-circle-triangle-e'
                                    }
                                })
                                .unbind('toggle')
                                .toggle(
                                    function() {
                                        nRowData.detailsButton.button( "option", "icons", {
                                            primary: 'ui-icon-circle-triangle-s'
                                        });
                                        $(self.ruleDataTable.fnGetNodes( )).each(function (index,r) {
                                            if ( self.ruleDataTable.fnIsOpen(r) ) {
                                                $(r).find('button#details').click();
                                            }                                                
                                        });                                        
                                        $(nRowData.detailsRow = $(self.ruleDataTable.fnOpen(
                                            nRow,
                                            "<fieldset id='ruleDetails' />",
                                            'ui-widget-header'))
                                        )
                                        .find('fieldset#ruleDetails')
                                        .addClass('ui-widget-content')
                                        .append(
                                            $('<legend />')
                                            .html('Rule Details')
                                            .addClass('ui-widget-header ui-corner-all')
                                        )
                                        .each(function() {
                                            var fieldset = $(this);
                                            switch(aData.class) {
                                                case "edu.usf.RuleChains.SQLQuery":
                                                    fieldset.find('legend:first').html('SQL Query Details');
                                                    $(nRowData.rule = $('<textarea />')).appendTo(fieldset).html(aData.rule);
                                                    nRowData.editor = new CodeMirrorUI(nRowData.rule.get(0),{ 
                                                        path : 'js/codemirror-ui/js', 
                                                        imagePath: 'js/codemirror-ui/images/silk', 
                                                        searchMode : 'inline',
                                                        buttons : ['save','undo','redo','jump','reindent','about'],
                                                        saveCallback: function() {                                                            
                                                            var ajax = $.extend({},aData,{
                                                                rule: {
                                                                    rule: nRowData.editor.mirror.getValue()
                                                                },
                                                                ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                                            });
                                                            $.ruleChains.ruleSet.POSTupdateRule(ajax,function(rule) {
                                                                if("rule" in rule) {                                                                    
                                                                    aData.rule = nRowData.editor.mirror.getValue();
                                                                } else {
                                                                    nRowData.editor.mirror.setValue(aData.rule);
                                                                    alert(rule.error);
                                                                }
                                                            });
                                                        }
                                                    },{ 
                                                        mode: "text/x-mysql",
                                                        tabMode: "indent",
                                                        lineNumbers: true,
                                                        matchBrackets: true,
                                                        theme: 'ambiance'
                                                    });
                                                    break;
                                                case "edu.usf.RuleChains.Groovy":
                                                    fieldset.find('legend:first').html('Groovy Script Details');
                                                    $(nRowData.rule = $('<textarea />')).appendTo(fieldset).html(aData.rule);
                                                    nRowData.editor = new CodeMirrorUI(nRowData.rule.get(0),{ 
                                                        path : 'js/codemirror-ui/js', 
                                                        imagePath: 'js/codemirror-ui/images/silk', 
                                                        searchMode : 'inline',
                                                        buttons : ['save','undo','redo','jump','reindent','about'],
                                                        saveCallback: function() {                                                            
                                                            var ajax = $.extend({},aData,{
                                                                rule: {
                                                                    rule: nRowData.editor.mirror.getValue()
                                                                },
                                                                ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                                            });
                                                            $.ruleChains.ruleSet.POSTupdateRule(ajax,function(rule) {
                                                                if("rule" in rule) {                                                                    
                                                                    aData.rule = nRowData.editor.mirror.getValue();
                                                                } else {
                                                                    nRowData.editor.mirror.setValue(aData.rule);
                                                                    alert(rule.error);
                                                                }
                                                            });
                                                        }
                                                    },{ 
                                                        mode: "text/x-groovy",
                                                        tabMode: "indent",
                                                        lineNumbers: true,
                                                        matchBrackets: true,
                                                        theme: 'ambiance'
                                                    });
                                                    break;
                                                case "edu.usf.RuleChains.NamedQuery":
                                                    fieldset.find('legend:first').html('Named Query Details');
                                                    $(nRowData.rule = $('<textarea />')).appendTo(fieldset).html(aData.rule);
                                                    nRowData.editor = new CodeMirrorUI(nRowData.rule.get(0),{ 
                                                        path : 'js/codemirror-ui/js', 
                                                        imagePath: 'js/codemirror-ui/images/silk', 
                                                        searchMode : 'inline',
                                                        buttons : ['save','undo','redo','jump','reindent','about'],
                                                        saveCallback: function() {                                                            
                                                            var ajax = $.extend({},aData,{
                                                                rule: {
                                                                    rule: nRowData.editor.mirror.getValue()
                                                                },
                                                                ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                                            });
                                                            $.ruleChains.ruleSet.POSTupdateRule(ajax,function(rule) {
                                                                if("rule" in rule) {                                                                    
                                                                    aData.rule = nRowData.editor.mirror.getValue();
                                                                } else {
                                                                    nRowData.editor.mirror.setValue(aData.rule);
                                                                    alert(rule.error);
                                                                }
                                                            });
                                                        }
                                                    },{ 
                                                        mode: "text/x-mysql",
                                                        tabMode: "indent",
                                                        lineNumbers: true,
                                                        matchBrackets: true,
                                                        theme: 'ambiance'
                                                    });
                                                    break;
                                                case "edu.usf.RuleChains.DefinedService":
                                                    fieldset.find('legend:first').html('Defined Service Details').end().each(function(index,fs) {
                                                        $('<table />').appendTo($(fs)).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "method" }).html("HTTP Method:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.method = $('<select />',{ id: "method" })).each(function(index,select) {
                                                                    $.each(['GET', 'POST', 'PUT', 'DELETE'],function(index,item) { $('<option />').val(item).html(item).appendTo($(select)); });
                                                                }).val(aData.method.name)).change(function() { nRowData.updateDefinedService(); })
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "parse" }).html("Output Type:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.parse = $('<select />',{ id: "parse" })).each(function(index,select) {
                                                                    $.each(['TEXT', 'XML', 'JSON'],function(index,item) { $('<option />').val(item).html(item).appendTo($(select)); });
                                                                }).val(aData.parse.name)).change(function() { nRowData.updateDefinedService(); })
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "authType" }).html("Authorization Type:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.authType = $('<select />',{ id: "authType" })).each(function(index,select) {
                                                                    $.each(['NONE','BASIC','DIGEST','CAS','CASSPRING'],function(index,item) { $('<option />').val(item).html(item).appendTo($(select)); });
                                                                }).val(aData.authType.name)).change(function() { nRowData.updateDefinedService(); })
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "url" }).html("Service URL:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.url = $('<input />',{ id: "url" })).val(aData.url).keyup(function() {
                                                                    var url=$(this).val();
                                                                    if(aData.url !== url) {
                                                                        nRowData.urlUpdate.button("option","disabled",false);
                                                                    } else {
                                                                        nRowData.urlUpdate.button("option","disabled",true);
                                                                    }
                                                                })).append($(nRowData.urlUpdate = $('<button />')).html("Update").button({
                                                                    text: false,
                                                                    icons: { 
                                                                        primary: 'ui-icon-disk' 
                                                                    },
                                                                    disabled: true
                                                                }).click(function() { 
                                                                    nRowData.updateDefinedService(); 
                                                                }))
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "springSecurityBaseURL" }).html("Spring Security Base URL:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.springSecurityBaseURL = $('<input />',{ id: "springSecurityBaseURL" })).val(aData.springSecurityBaseURL).keyup(function() {
                                                                    var springSecurityBaseURL=$(this).val();
                                                                    if(aData.springSecurityBaseURL !== springSecurityBaseURL) {
                                                                        nRowData.springSecurityBaseURLUpdate.button("option","disabled",false);
                                                                    } else {
                                                                        nRowData.springSecurityBaseURLUpdate.button("option","disabled",true);
                                                                    }
                                                                })).append($(nRowData.springSecurityBaseURLUpdate = $('<button />')).html("Update").button({
                                                                    text: false,
                                                                    icons: { 
                                                                        primary: 'ui-icon-disk' 
                                                                    },
                                                                    disabled: true
                                                                }).click(function() { 
                                                                    nRowData.updateDefinedService(); 
                                                                }))
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "user" }).html("Account Name:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.user = $('<input />',{ id: "user" })).val(aData.user).keyup(function() {
                                                                    var user=$(this).val();
                                                                    if(aData.user !== user) {
                                                                        nRowData.userUpdate.button("option","disabled",false);
                                                                    } else {
                                                                        nRowData.userUpdate.button("option","disabled",true);
                                                                    }
                                                                })).append($(nRowData.userUpdate = $('<button />')).html("Update").button({
                                                                    text: false,
                                                                    icons: { 
                                                                        primary: 'ui-icon-disk' 
                                                                    },
                                                                    disabled: true
                                                                }).click(function() { 
                                                                    nRowData.updateDefinedService(); 
                                                                }))
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<td />').append($('<label />',{ "for": "password" }).html("Account Password:"))
                                                            ).append(
                                                                $('<td />').append($(nRowData.password = $('<input />',{ id: "password" })).val(aData.password).keyup(function() {
                                                                    var password=$(this).val();
                                                                    if(aData.password !== password) {
                                                                        nRowData.passwordUpdate.button("option","disabled",false);
                                                                    } else {
                                                                        nRowData.passwordUpdate.button("option","disabled",true);
                                                                    }
                                                                })).append($(nRowData.passwordUpdate = $('<button />')).html("Update").button({
                                                                    text: false,
                                                                    icons: { 
                                                                        primary: 'ui-icon-disk' 
                                                                    },
                                                                    disabled: true
                                                                }).click(function() { 
                                                                    nRowData.updateDefinedService(); 
                                                                }))
                                                            )
                                                        );
                                                    });
                                                    nRowData.headerDataTable = $(nRowData.headerTable = $('<table />',{ id: "headerTable" })).appendTo(fieldset).width('100%').append(
                                                        $('<thead />').append(
                                                            $('<tr />').append(
                                                                $('<th />',{ "colspan": "2" }).append(
                                                                    $('<span />').append(
                                                                        $(nRowData.addHeaderButton = $('<button />')).html("Add").button({
                                                                            text: false,
                                                                            icons: {
                                                                                primary: "ui-icon-plusthick"
                                                                            }            
                                                                        }).click(function() {
                                                                            $('<div />')
                                                                            .data({
                                                                                name: $('<input />',{
                                                                                    id: "name"
                                                                                }),
                                                                                value: $('<input />',{
                                                                                    id: "name"
                                                                                })
                                                                            })
                                                                            .addClass('ui-state-default ui-widget-content')
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
                                                                                autoOpen: true,
                                                                                bgiframe: true,
                                                                                resizable: false,
                                                                                title: 'Add New Header',
                                                                                height:280,
                                                                                width:400,
                                                                                modal: true,
                                                                                zIndex: 3999,
                                                                                overlay: {
                                                                                    backgroundColor: '#000',
                                                                                    opacity: 0.5
                                                                                },
                                                                                open: function() {
                                                                                    $(this).append(
                                                                                        $('<label />',{
                                                                                            "for": "name"
                                                                                        })
                                                                                        .html('Name:')
                                                                                        .css({ "padding-right":"15px"})
                                                                                    ).append($(this).data().name)
                                                                                    .append('<br />')
                                                                                    .append(
                                                                                        $('<label />',{
                                                                                            "for": "value"
                                                                                        })
                                                                                        .html('Value:')
                                                                                        .css({ "padding-right":"17px"})
                                                                                    ).append($(this).data().value);
                                                                                },
                                                                                buttons: {
                                                                                    "Add New Header": function() {
                                                                                        var dialog = $(this),
                                                                                        name = $.trim(dialog.data().name.val()),
                                                                                        value = $.trim(dialog.data().value.val());
                                                                                        if($.grep(nRowData.headerDataTable.fnGetData(),function(header,index) { return name === header[0]; }).length) {
                                                                                            alert("This header already exists! Edit the existing one if you need to change it or provide a new unique one.");
                                                                                        } else {
                                                                                            nRowData.headerDataTable.fnAddData([name,value]);
                                                                                            nRowData.updateDefinedService();
                                                                                            dialog.dialog('close');
                                                                                            dialog.dialog('destroy');
                                                                                            dialog.remove();                                                                        
                                                                                        }
                                                                                    },
                                                                                    "Cancel": function() {
                                                                                        $(this).dialog('close');
                                                                                        $(this).dialog('destroy');
                                                                                        $(this).remove();                                                                        
                                                                                    }
                                                                                }
                                                                            });                   
                                                                        })
                                                                    ).append(
                                                                        $(nRowData.removeHeaderButton = $('<button />')).html("Remove").button({
                                                                            text: false,
                                                                            icons: {
                                                                                primary: "ui-icon-trash"
                                                                            },
                                                                            disabled: true
                                                                        }).click(function() {
                                                                            $('<div />')
                                                                            .addClass('ui-state-default ui-widget-content')
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
                                                                                autoOpen: true,
                                                                                bgiframe: true,
                                                                                resizable: false,
                                                                                title: 'Add New Header',
                                                                                height:280,
                                                                                width:400,
                                                                                modal: true,
                                                                                zIndex: 3999,
                                                                                overlay: {
                                                                                    backgroundColor: '#000',
                                                                                    opacity: 0.5
                                                                                },
                                                                                open: function() {
                                                                                    $(this).append("Are you SURE you want to delete this header?");                                                                                    
                                                                                },
                                                                                buttons: {
                                                                                    "Delete Header": function() {
                                                                                        var dialog = $(this),
                                                                                        hnRow = $.grep(nRowData.headerDataTable.fnGetNodes(),function(tr) {
                                                                                            return $(tr).hasClass('ui-widget-shadow');
                                                                                        })[0];
                                                                                        nRowData.headerDataTable.fnDeleteRow(hnRow);
                                                                                        nRowData.updateDefinedService();
                                                                                        dialog.dialog('close');
                                                                                        dialog.dialog('destroy');
                                                                                        dialog.remove();                                                                                                                                                                
                                                                                    },
                                                                                    "Cancel": function() {
                                                                                        $(this).dialog('close');
                                                                                        $(this).dialog('destroy');
                                                                                        $(this).remove();                                                                                                                                                        
                                                                                    }
                                                                                }
                                                                        
                                                                            });                                                                        
                                                                        })
                                                                    ).buttonset()
                                                                )
                                                            )
                                                        ).append(
                                                            $('<tr />').append(
                                                                $('<th />').html("Name")
                                                            ).append(
                                                                $('<th />').html("Value")
                                                            )
                                                        )
                                                    ).append($('<tbody />')).dataTable({
                                                        "aoColumns": [
                                                            { "bVisible": true },
                                                            { "bVisible": true }
                                                        ],
                                                        "bJQueryUI": true,
                                                        "asStripeClasses": [ 'ui-priority-primary', 'ui-priority-secondary' ],
                                                        "aaData": $.map(aData.headers,function(val,key) {
                                                            return [[key, val]];
                                                        }),
                                                        "fnRowCallback": function( hnRow, haData, hiDisplayIndex, hiDisplayIndexFull ) {
                                                            return $(hnRow)
                                                            .unbind('click')
                                                            .click(function(event) {
                                                                if($(hnRow).hasClass('ui-widget-shadow')) {
                                                                    $(hnRow).removeClass('ui-widget-shadow');
                                                                    nRowData.removeHeaderButton.button("option","disabled",true);
                                                                } else {
                                                                    $(nRowData.headerDataTable.fnGetNodes( )).each(function() {
                                                                        $(this).removeClass('ui-widget-shadow');
                                                                    });
                                                                    $(hnRow).addClass('ui-widget-shadow');                                    
                                                                    nRowData.removeHeaderButton.button("option","disabled",false);
                                                                }
                                                            });
                                                        }
                                                    });
                                                    nRowData.updateDefinedService = function() {
                                                        var ajax = $.extend({},aData,{
                                                            rule: {
                                                                headers: {
                                                                    buildHeaders: function(headersArray) {
                                                                        var map = new Object();
                                                                        $.each(headersArray,function(index, element) {
                                                                            map[element[0]] = element[1];
                                                                        });
                                                                        return map;
                                                                    }
                                                                }.buildHeaders(nRowData.headerDataTable.fnGetData()),
                                                                method: {
                                                                    name: nRowData.method.val()
                                                                },
                                                                parse: {
                                                                    name: nRowData.parse.val()
                                                                },
                                                                authType: {
                                                                    name: nRowData.authType.val()
                                                                },
                                                                url: $.trim(nRowData.url.val()),
                                                                springSecurityBaseURL: $.trim(nRowData.springSecurityBaseURL.val()),
                                                                user: $.trim(nRowData.user.val()),
                                                                password: $.trim(nRowData.password.val())
                                                            },
                                                            ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                                        });
                                                        $.ruleChains.ruleSet.POSTupdateRule(ajax,function(rule) {
                                                            if("rule" in rule) {  
                                                                if(self.ruleDataTable.fnIsOpen(nRow)) {
                                                                    nRowData.detailsButton.click();
                                                                    self.ruleDataTable.fnUpdate(rule.rule,nRow);
                                                                    nRowData.detailsButton.click();
                                                                } else {
                                                                    self.ruleDataTable.fnUpdate(rule.rule,nRow);
                                                                }
                                                            } else {
                                                                alert(rule.error);
                                                            }
                                                        });                                                    
                                                    };
                                                    break;
                                                case "edu.usf.RuleChains.Snippet":
                                                    fieldset.find('legend:first').html('Snippet Details');
                                                    $(nRowData.chain = self.chainSelect.clone().unbind()).children().each(function(i,opt) {
                                                        if(i < 1) {
                                                            $(opt).remove();
                                                        } else if ( $(opt).val() === self.ruleSetSelect.val() ) {
                                                            $(opt).remove();
                                                        }
                                                    }).end().appendTo(fieldset.append(
                                                        $('<label />',{
                                                            "for": "chain"
                                                        }).html("Select Chain for use in Snippet")
                                                        .css({
                                                            "padding-right":"15px"
                                                        })
                                                    ));
                                                    $(nRowData.updateButton = $('<button />')).html("Update Referenced Chain").appendTo(fieldset).button({
                                                        text: false,
                                                        icons: {
                                                            primary: "ui-icon-wrench"
                                                        }
                                                    }).click(function() {
                                                        var ajax = $.extend({},aData,{
                                                            chain: {
                                                                id: nRowData.nRowData.chain.val()
                                                            },
                                                            ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                                        });
                                                        $.ruleChains.ruleSet.POSTupdateRule(ajax,function(rule) {
                                                            if("rule" in rule) {                                                                    
                                                                aData.chain = rule.rule.chain;
                                                                aData.name = rule.rule.name;
                                                                $(nRow).find('td:nth-child(3)').html(aData.name);
                                                            } else {
                                                                nRowData.detailsButton.click().click();
                                                                alert(rule.error);
                                                            }
                                                        });
                                                        
                                                    });
                                                    break;
                                            }
                                        });
                                    },
                                    function() {
                                        // Close the details
                                        nRowData.detailsButton.button( "option", "icons", {
                                            primary: 'ui-icon-circle-triangle-e'
                                        });
                                        self.ruleDataTable.fnClose(nRow);                                
                                    }
                                ),
                                ruleNameEditable = $(nRowData.ruleNameEditable = $("td:eq(2)",nRow)).editable(function(value, settings) { 
                                    var result = value;
                                    $.ruleChains.ruleSet.POSTupdateRuleName({
                                        name: aData.name,
                                        newName: $.trim(result),
                                        ruleSetName: self.ruleSetSelect.find('option:selected').text()
                                    },function(rule) {
                                        if("rule" in rule) {  
                                            if(self.ruleDataTable.fnIsOpen(nRow)) {
                                                nRowData.detailsButton.click();
                                                self.ruleDataTable.fnUpdate(rule.rule,nRow);
                                                nRowData.detailsButton.click();
                                            } else {
                                                self.ruleDataTable.fnUpdate(rule.rule,nRow);
                                            }
                                            aData = rule.rule;
                                        } else {
                                            alert(rule.error);
                                        }                                        
                                    });
                                    console.log(this);
                                    console.log(value);
                                    console.log(settings);
                                    return(value);
                                 }, { 
                                    type    : 'text-ui',
                                    submit  : 'Update',
                                    event     : "dblclick",
                                    tooltip   : 'Doubleclick to edit...'
                                });

                        });                
                    }
                }));
            
        }        
    });
})(jQuery);        
        