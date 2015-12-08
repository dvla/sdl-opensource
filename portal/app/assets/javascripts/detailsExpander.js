(function ($) {
    $('.col.pull-right.expander.hidden').removeClass('hidden');
    $('.col.pull-right.expando.hidden').removeClass('hidden');
})($)

$(".accordion-body").on("show", function (event) {
    var expandButton = $('.expander', $(this).prev());
    expandButton
    .prev()
    .find('i')
    .addClass('icon-minus-sign')
    .removeClass('icon-plus-sign');
    expandButton.text(expandButton.attr("data-message-less"));
});

$(".accordion-body").on("hide", function (event) {
    var expandButton = $('.expander', $(this).prev());
    expandButton
    .prev()
    .find('i')
    .addClass('icon-plus-sign')
    .removeClass('icon-minus-sign');

    expandButton.text(expandButton.attr("data-message-more"));
});