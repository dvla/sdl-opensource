define(function(require) {

	var accordionHelper = require('accordionHelper');
	$(".accordion-body").addClass("accordion-body collapse");
	accordionHelper($('#feMoreDiv'), $('#fe_accordion'));
	accordionHelper($('#peMoreDiv'), $('#pe_accordion'));
})