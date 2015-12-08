define(function() {
    var checkBoxValidator = function() {

        var emptyMessage, control, controlGroup, controlName,controlHelp,

        init = function(controlId, emptyMsg) {

            emptyMessage = emptyMsg;
            controlGroup = $("#" + controlId + "-control-group");
            controlHelp = $("#" + controlId + "-help");
            controlName = controlId
            control = controlGroup.find('input[type="checkbox"]');

            control
               .on("focusout", validate)
               .on("focus", removeValidationError)
               .on("change", removeValidationError)
        },

        validate = function() {
             if (controlGroup.find('input[type="checkbox"]:checked').length === 0) {
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
            controlGroup.removeClass("error");
            controlHelp.text("");
        };

        return   {
            init: init,
            validate: validate,
            removeValidationError: removeValidationError
        }
    }

    return checkBoxValidator;
})