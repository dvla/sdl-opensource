define(function() {
    var radioButtonValidator = function() {

        var emptyMessage, control, controlGroup, controlName,controlHelp,

            init = function(controlId, emptyMsg) {

                emptyMessage = emptyMsg;
                control = $("input[name=" + controlId + ']')
                controlGroup = $("#" + controlId + "-control-group")
                controlHelp = $("#" + controlId + "-help")
                controlName = controlId

                control
                    .on("focusout", validate)
                    .on("focus", removeValidationError)
                    .on("change", removeValidationError)
            },

            validate = function() {
                 if ($('input[name=' + controlName + ']:checked').length == 0) {
                    controlGroup.addClass("validation");
                    controlGroup.addClass("error");
                    controlHelp.text(emptyMessage);
                    return false;
                }
                else
                {
                return true;
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

    return radioButtonValidator;
})