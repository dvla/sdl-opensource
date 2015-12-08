define(function() {
	var dvla = window.dvla = window.dvla || {};

	dvla.dobValidator = function() {

		var notValidMessage,
			year,
			yearChanged = false,
			month,
			monthChanged = false,
			day,
			dayChanged = false,
			controlGroup,
			controlHelp,

			removeValidationError = function() {
				controlGroup.removeClass("validation");
				controlGroup.removeClass("group");
				controlGroup.removeClass("error");
				controlHelp.text("");
			},

			init = function(controlId, emptyMsg, notValidMsg, underAgeMsg) {
				emptyMessage = emptyMsg;
				notValidMessage = notValidMsg;
				underAgeMessage = underAgeMsg;

				controlGroup = $("#" + controlId + "-control-group")
				year = controlGroup.find('input#dob_year');
				month = controlGroup.find('input#dob_month');
				day = controlGroup.find('input#dob_day');

				controlHelp = $("#" + controlId + "-help");

				year
					.on("focusout", validate)
					.on("focus", removeValidationError);

				month
					.on("focusout", validate)
					.on("focus", removeValidationError);

				day
					.on("focusout", validate)
					.on("focus", removeValidationError);
			},

			validate = function(event, forceValidation) {

                function emptyDate() {
                    controlGroup.addClass("group validation error");
                    controlHelp.text(emptyMessage);
                    return false;
                }

				function invalidate() {
					controlGroup.addClass("group validation error");
					controlHelp.text(notValidMessage);
					return false;
				}

				function underMinAge() {
				    controlGroup.addClass("group validation error");
				    controlHelp.text(underAgeMessage);
				    return false;
				}

                if (year.val()=="" && month.val() <= 0 && day.val() <= 0) {
                    return emptyDate();
                }

				if (year.val() < 1900 || month.val() <= 0 || day.val() <= 0) {
					return invalidate();
				}

				var dobObj = new Date(year.val(), month.val() - 1, day.val());

				if ((dobObj.getMonth() + 1 != month.val()) || (dobObj.getDate() != day.val()) || (dobObj.getFullYear() != year.val())) {
					return invalidate();
				}

				var currentDate = new Date();
				currentDate.setHours(0);
				currentDate.setMinutes(0);
				currentDate.setSeconds(0);
				currentDate.setMilliseconds(0);
				if(dobObj > currentDate) {
					return invalidate();
				}
				
                var minAge = new Date();
				minAge.setFullYear(minAge.getFullYear()-15);
				minAge.setMonth(minAge.getMonth()-9);
				minAge.setHours(0);
				minAge.setMinutes(0);
				minAge.setSeconds(0);
				minAge.setMilliseconds(0);
				if(dobObj > minAge) {
					return underMinAge();
				}

				return true;
			};

		return {
			init: init,
			validate: validate,
			removeValidationError: removeValidationError
		}
	}
})