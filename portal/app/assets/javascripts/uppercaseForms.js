(function( $ ) {
  $.fn.uppercaseForms = function() {
  	var el = $(this);
  	el.keyup(function(){
  		this.value = this.value.toLocaleUpperCase();
  	})
  };
})( jQuery );