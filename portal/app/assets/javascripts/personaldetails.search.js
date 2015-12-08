$(document).ready(function(){

    $("#nino").focusout(function() {
        validateMandatoryControlWithRegex("nino", ninoEmpty, ninoNotValid, ninoRegex);
    });

    $("#postcode").focusout(function() {
        validatePostcodeControl("postcode", postcodeEmpty, postcodeNotValid);
    });

    $("#forename").focusout(function() {
        validateMandatoryControlWithRegex("forename", forenameEmpty, forenameNotValid, nameRegex);
    });

    $("#forename").focus(function() {
        removeValidationError("forename");
     });

    $("#surname").focus(function() {
        removeValidationError("surname");
     });

    $("#surname").focusout(function() {
        validateMandatoryControlWithRegex("surname", surnameEmpty, surnameNotValid, nameRegex);
    });

    $("#gender").focusout(function() {
        validateMandatoryControl("gender", genderEmpty, "-1");
    });

    $("#dobDay").focusout(function() {
        validateDOBControl("dobDay","dobMonth","dobYear",dobNotValid);
    });

    $("#dobMonth").focusout(function() {
        validateDOBControl("dobDay","dobMonth","dobYear",dobNotValid);
    });

    $("#dobYear").focusout(function() { validateDOBControl("dobDay","dobMonth","dobYear",dobNotValid); });

    $("#dobDay").focus(function() {removeValidationError("dob"); });

    $("#dobMonth").focus(function() {removeValidationError("dob"); });

    $("#dobYear").focus(function() {removeValidationError("dob"); });

    $("#gender").focus(function() {removeValidationError("gender"); });

    $("#postcode").focus(function() {removeValidationError("postcode"); });

    $("#nino").focus(function() {removeValidationError("nino"); });

    $('body').on('mousedown','#submitButton',function(e){
        if(!validateMandatoryControlWithRegex("nino", ninoEmpty, ninoNotValid, ninoRegex)){
            e.preventDefault();
        }
        if(!validatePostcodeControl("postcode", postcodeEmpty, postcodeNotValid)){
            e.preventDefault();
        }
        if(!validateDOBControl("dobDay","dobMonth","dobYear",dobNotValid)){
            e.preventDefault();
        }
        if(!validateMandatoryControlWithRegex("forename", forenameEmpty, forenameNotValid, nameRegex)){
            e.preventDefault();
        }
        if(!validateMandatoryControlWithRegex("surname", surnameEmpty, surnameNotValid, nameRegex)){
            e.preventDefault();
        }
        if(!validateMandatoryControl("gender", genderEmpty, -1)){
            e.preventDefault();
        }
    });

    $("#submitButton").click(function(e){
        if(!validateMandatoryControlWithRegex("nino", ninoEmpty, ninoNotValid, ninoRegex)){
            e.preventDefault();
        }
        if(!validatePostcodeControl("postcode", postcodeEmpty, postcodeNotValid)){
            e.preventDefault();
        }
        if(!validateDOBControl("dobDay","dobMonth","dobYear",dobNotValid)){
            e.preventDefault();
        }
        if(!validateMandatoryControlWithRegex("forename", forenameEmpty, forenameNotValid, nameRegex)){
            e.preventDefault();
        }
        if(!validateMandatoryControlWithRegex("surname", surnameEmpty, surnameNotValid, nameRegex)){
            e.preventDefault();
        }
        if(!validateMandatoryControl("gender", genderEmpty, -1)){
            e.preventDefault();
        }
    });
});

function validateMandatoryControl(controlId, errorText, emptyValue) {
    var emptyValueToCheck = (emptyValue == undefined) ? "" : emptyValue;
    if($("#"+controlId).val() == emptyValueToCheck){
        $("#"+controlId+"-control-group").addClass("error");
        $("#"+controlId+"-help").text(errorText);
        return false;
    }
    else{
        $("#"+controlId+"-control-group").removeClass("error");
        $("#"+controlId+"-help").text("");
        return true;
    }
}

function validatePostcodeControl(controlId, postcodeEmpty, postcodeNotValid) {
    if($("#"+controlId).val() == ""){
        $("#postcode-control-group").addClass("error");
        $("#postcode-help").text(postcodeEmpty);
        return false;
    }
    else if(!validatePostcodeRegex($("#"+controlId).val())) {
        $("#postcode-control-group").addClass("error");
        $("#postcode-help").text(postcodeNotValid);
        return false;
    }
    else{
        $("#postcode-control-group").removeClass("error");
        $("#postcode-help").text("");
        return true;
    }
}

function removeValidationError(controlId){
    $("#" + controlId + "-control-group").removeClass("error");
    $("#" + controlId + "-help").text("");
}

function validateDOBControl(dayControlId, monthControlId, yearControlId, dobNotValid){
    var day = $("#" + dayControlId + " option:selected").val();
    var month = $("#" + monthControlId + " option:selected").val();
    var year = $("#" + yearControlId + " option:selected").val();

    try{
        $.datepicker.parseDate("yy-mm-dd",year + "-" + month + "-" + day);
        $("#dob-control-group").removeClass("error");
        $("#dob-help").text("");
        return true;
    }
    catch(e) {
     $("#dob-control-group").addClass("error");
     $("#dob-help").text(dobNotValid);
     return false
    }
}

function validatePostcodeRegex (postcode) {
    // See http://www.govtalk.gov.uk/gdsc/html/noframes/PostCode-2-1-Release.htm

    postcode = jQuery.trim(postcode);

    if (postcode.match(postcodeRegex1) ||
        postcode.match(postcodeRegex2) ||
        postcode.match(postcodeRegex3) ||
        postcode.match(postcodeRegex4) ||
        postcode.match(postcodeRegex5) ||
        postcode.match(postcodeRegex6)) {
        return true;
    }
    else {
        return false;
    }
}

function validateMandatoryControlWithRegex (controlId, emptyMessage, notValidMessage, regex){
    if($("#"+controlId).val() == "") {
        $("#"+controlId+"-control-group").addClass("error");
        $("#"+controlId+"-help").text(emptyMessage);
        return false;
    }
    else if($("#"+controlId).val().match(regex) != null) {
        $("#"+controlId+"-control-group").removeClass("error");
        $("#"+controlId+"-help").text("");
        return true;
    }
    else {
        $("#"+controlId+"-control-group").addClass("error");
        $("#"+controlId+"-help").text(notValidMessage);
        return false;
    }
}
