define(function() {
	var dvla = window.dvla = window.dvla || {};
	dvla.dropDownValidator = function() {

		var notValidMessage, year, month, day, controlGroup, controlHelp,

			removeValidationError = function() {
				controlGroup.removeClass("error");
				controlHelp.text("");
			},

			init = function(controlId, notValidMsg) {
				notValidMessage = notValidMsg;

				control = $("#" + controlId);
				controlGroup = $("#" + controlId + "-control-group");
				controlHelp = $("#" + controlId + "-help");

				control
					.on("focusout", validate)
					.on("focus", removeValidationError);
			},

			validate = function() {
				var value = control.val();

				if (value != "" && value > -1) {
					controlGroup.removeClass("error");
					controlHelp.text("");
					return true;
				}

				controlGroup.addClass("error");
				controlHelp.text(notValidMessage);
				return false;
			};

		return {
			init: init,
			validate: validate,
			removeValidationError: removeValidationError
		}
	}
})