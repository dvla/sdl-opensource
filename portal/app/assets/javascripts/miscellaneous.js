define(function() {

	$('#Entitlements, #Endorsements').removeClass('active');

	$('a#EntitlementsWell').on('click', function(e) {
		e.preventDefault();
		$('#Entitlements').tab('show');
		return false;
	});

	$('a#EndorsementsWell').on('click', function(e) {
		e.preventDefault();
		$('#Endorsements').tab('show');
		return false;
	});

});
