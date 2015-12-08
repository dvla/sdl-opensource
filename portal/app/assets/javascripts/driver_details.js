 $(document).ready(function(){
    $('#EntitlementsWell').click(function (e) {
     $('#entitlementsTab').tab('show');
    });
    $('#EndorsementsWell').click(function (e) {
      $('#endorsementsTab').tab('show');
    });
    $('.tab-pane h2.h2.no-js-header').remove();
    $('div.show-more.hidden').removeClass('hidden');
  });