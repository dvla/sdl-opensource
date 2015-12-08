    var handleAppleExpirationLoop;
    var appleIdle;
    var appleUrl;
    var startTime = new Date();

    function TimerUtilities() {}
    TimerUtilities.prototype.setPageExpiration = function(element, idleTimeout, url) {
        var ua = navigator.userAgent;
                if(/(iPad|iPhone)/i.test(ua)){
                    if(handleAppleExpirationLoop){clearInterval(handleAppleExpirationLoop)};
                    handleAppleExpirationLoop = setInterval("handleAppleExpiration()",500);
                    appleIdle = idleTimeout;
                    appleUrl = url;
                };

        element.idleTimer(idleTimeout);
        element.on("idle.idleTimer", function() {
            window.location.href = url;
        });
    };

    TimerUtilities.prototype.checkForExpiration = function(body, element) {
        var d = new Date();
        d = d.getTime();
        if (element.val().length == 0) {
            element.val(d);
            body.show();
        } else {
            element.val('');
            location.reload();
        }
    }

    function handleAppleExpiration() {
        if(new Date() - startTime >= appleIdle && appleIdle != 0){
            window.location.href = appleUrl;
        }
    }