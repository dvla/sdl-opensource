define(function(require) {
	var accordionHelper = require('accordionHelper');
	$(".accordion-body").addClass("accordion-body collapse");
	accordionHelper($('#ppMoreDiv'), $('#pp_accordion'));
	accordionHelper($('#dqMoreDiv'), $('#dq_accordion'));
})