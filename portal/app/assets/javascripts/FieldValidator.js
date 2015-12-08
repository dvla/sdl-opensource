define(function() {
    var FieldValidator = function() {
        var returnedValue="";
        var emptyMessage, notValidMessage, regexes, control, controlGroup, controlHelp, removeSpaces, fieldRequired,

            init = function(controlId, notValidMsg, emptyMsg, regex,removeSpaceFn,required) {

                emptyMessage = emptyMsg;
                notValidMessage = notValidMsg;
                regexes = regex;
                removeSpaces = removeSpaceFn;

                //If required field not sent, set to false
                if (required == null) { fieldRequired = true; } else {fieldRequired = required}

                control = $("#" + controlId)
                controlGroup = $("#" + controlId + "-control-group")
                controlHelp = $("#" + controlId + "-help")

                /*
                    The timeout on the function below resolves the conflict between
                    focusout event on the input field and click event on the submit button
                    Without it the click event will not fire on the first user click (provided
                    there are validation errors)
                */
                control.on("focusout", function() { window.setTimeout(validate, 100) })
                       .on("focus", removeValidationError)
            },

            validate = function() {

                returnedValue = "";
                var value = control.val();
                if ($.isFunction(removeSpaces)) {
                    value = removeSpaces(value);
                    control.val(value);
                }
                returnedValue = value;

                if (fieldRequired == true) {
                if (value == "") {
                    controlGroup.addClass("validation error");
                    controlHelp.text(emptyMessage);
                    return false;
                }
                }

                if (someRegexMatches(value)) {
                    controlGroup.removeClass("validation error");
                    controlHelp.text("");
                    return true;
                }

                controlGroup.addClass("validation error");
                controlHelp.text(notValidMessage);
                return false;
            },

            removeValidationError = function() {
                controlGroup.removeClass("validation error");
                controlHelp.text("");
                if ($.isFunction(removeSpaces)) {
                    control.val(returnedValue);
                }
            },

            someRegexMatches = function(value) {
                //If no regex sent, just return true
                if (regexes.length == 0) return true;

                //If optional, ignore regex until something is entered
                if (value == "" &&  !fieldRequired) return true;
                for (var i = 0; i < regexes.length; i++) {
                    if (value.match(regexes[i]) != null) {
                        return true;
                    }
                }
                return false;
            };

        return {
            init: init,
            validate: validate,
            removeValidationError: removeValidationError
        }
    }

    return FieldValidator;
})