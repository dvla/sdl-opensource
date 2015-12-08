/**
This module is for populating and redirecting a form so after using refresh, back and forward
**/
define(["cookies"], function(cookies){

    $('form').on('submit', function(){
        //if a from is submitted by normal means, we want to set break cookie to let it remember
        cookies.set('naturalSubmit', true);
    });

    var naturalSubmit = cookies.get('naturalSubmit');
    function fillForm(){

       $('form').find('input').each(function(i, e){
            var element = $(e);
            if(formData[element.attr('name')] && element.attr('name') !== "gender" && !element.attr('disabled')){
               element.val(formData[element.attr('name')]);
            }
       });
       var sels = document.getElementsByTagName('form')[0].getElementsByTagName('select');
       for(var i=0;i<sels.length;i++){
            if(document.forms[0][sels[i].name] && !element.attr('disabled')) {
                sels[i].value=formData[sels[i].name];
            }
       };

       if(formData["gender"] && formData["gender"] == "1" && !element.attr('disabled')) {
            $('#male').attr("checked","checked");
       } else {
            $('#male').removeAttr("checked")
       };

       if(formData["gender"] && formData["gender"] == "2" && !element.attr('disabled')) {
            $('#female').attr("checked","checked");
       } else {
            $('#female').removeAttr("checked")
       };
    }

    //we operate only when the form data is available
    var formData = $('#formData').attr('data-form');
    if(typeof formData !== 'undefined'){
        var formDataJson = JSON.parse(formData);
        if(typeof formDataJson !== 'undefined'){
            //if it was a natural submit and flag block is not set
            if(naturalSubmit || cookies.get('formAction') !== 'dontFill'){
                if(naturalSubmit) cookies.set('naturalSubmit', undefined);
                fillForm();

                cookies.set('formAction', 'dontFill');
                $('#formData').remove();
            }
            else{
                cookies.set('formAction', undefined);
                document.location.href = document.location.href.replace('/error','');
            }
        }
    }
});