define(function() {

    $("#personalInfo").hide();

    $(".show-hide-link").on('click', function(event) {
        $("#personalInfo").toggle();
        $("#contactSubmitBtn").toggle();
        return false;
    });
});