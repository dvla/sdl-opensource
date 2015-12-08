define(function () {

    var tabContainers = $('div.tabbed-view > div');

    var showTab = function (hash) {
        tabContainers.hide();
        tabContainers.filter(hash).show();
        $('div.tabbed-view ul.tabs a').removeClass('current');
        $('div.tabbed-view ul.tabs a[href=' + hash + ']').addClass('current');
    };

    var changeHistory = function (hash) {
        if (history.pushState) {
            history.pushState(null, null, hash);
        } else {
            location.hash = hash;
        }
    };

    $('div.tabbed-view ul.tabs a[href*=#]').click(function () {
        changeHistory(this.hash);
        $(window).scrollTop(0);
        showTab(this.hash);
        return false;
    });

    var initialTab = $('div.tabbed-view ul.tabs a[href=' + location.hash + ']');
    if (initialTab.length == 0) {
        initialTab = $('div.tabbed-view ul.tabs a[href*=#]:first')
    }
    initialTab.click();

    $('.accordion-content').hide();

    $('a.toggle').on('click', function (event) {

        if (!event.detail || event.detail == 1) {

            $(this).parent().parent().find('div.accordion-content:first').slideToggle('fast');

            $(this).find('span.more > span').toggleClass('up').toggleClass('down');
            if ($(this).find('span').is('.up')) {
                $(this).find('em').html($('#driverDetails').attr('data-accordian-show-text') + ' <img alt="" src="' + $('#driverDetails').attr('data-assets-img-root') + '/circle-plus@2x.png" width="16px">');
            } else {
                $(this).find('em').html($('#driverDetails').attr('data-accordian-hide-text') + ' <img alt="" src="' + $('#driverDetails').attr('data-assets-img-root') + '/circle-minus@2x.png" width="16px">');
            }
            return false;
        }
        return false;
    });

});