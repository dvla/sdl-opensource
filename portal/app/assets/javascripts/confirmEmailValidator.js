define(function() {
    var confirmEmailValidator = function() {

        var  missingMessage, noMatchMessage, controlGroup, controlHelp,firstEmailControl, secondEmailControl,

        init = function(controlOne, controlTwo, noMatchMsg, missingMsg) {

            noMatchMessage = noMatchMsg;
            missingMessage = missingMsg;

            controlGroup = $("#" + controlTwo + "-control-group")
            controlHelp = $("#" + controlTwo + "-help")
            firstEmailControl = $("#" + controlOne)
            secondEmailControl = $("#" + controlTwo)

            secondEmailControl
               .on("focusout", validate)
               .on("focus", removeValidationError)

            firstEmailControl
               .on("focusout", validate)
               .on("focus", removeValidationError)
        },

        validate = function() {
            var firstMail = firstEmailControl.val().toLowerCase();
            var secondMail = secondEmailControl.val().toLowerCase();

            if (secondMail == "") {
                controlGroup.addClass("validation");
                controlGroup.addClass("error");
                controlHelp.text(missingMessage);
                return false;
            }

            if (firstMail == secondMail) {
                removeValidationError
                return true;
            }
            else {
                controlGroup.addClass("validation");
                controlGroup.addClass("error");
                controlHelp.text(noMatchMessage);
                return false;
            }
        },

        removeValidationError = function() {
            controlGroup.removeClass("validation");
            controlGroup.removeClass("error");
            controlHelp.text("");
        };

        return   {
            init: init,
            validate: validate,
            removeValidationError: removeValidationError
        }
    }

    return confirmEmailValidator;
})