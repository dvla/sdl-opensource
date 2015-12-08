
function opsAddReason() {
    var opsSubmit = document.getElementById('submitDln');
    if(opsSubmit && /ops/gim.test(location.href)) {
        opsSubmit.onclick = null;
        addListener(opsSubmit,'click',function(e){
            var reasons = "";
            for(i=0; i<document.getElementsByTagName('input').length;i++) {
                if(/enquiryreasons/gim.test(document.getElementsByTagName('input')[i].name) && document.getElementsByTagName('input')[i].checked) {
                    reasons += document.getElementsByTagName('input')[i].getAttribute('data-text') + ", "
                }
            }
            var from = location.href;
            var actionName = this.value||e.srcElement.value + " - " + reasons;
            var to = getGaCookie() + document.forms[0].action;
            sendAnalyticsEvent(from,actionName,to);
        })
    }
}
opsAddReason();