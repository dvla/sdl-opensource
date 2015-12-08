function toggleHelper(whereToFindHolderId) {
    var parentObject = whereToFindHolderId.parentElement.parentElement;
    var whereToFindInstructions = parentObject.getElementsByTagName('div')[1];
    var whereToFindArrow = parentObject.getElementsByTagName('span')[0];
    var whereToFindText = parentObject.getElementsByTagName('span')[1];
    if(whereToFindInstructions.style.display == 'none') {
        whereToFindInstructions.style.display = 'block';
        whereToFindArrow.innerHTML = "▼";
        whereToFindText.className = parentObject.getElementsByTagName('span')[1].className.replace(/\bclosed\b/,'open');
    } else {
        whereToFindInstructions.style.display = 'none';
        whereToFindArrow.innerHTML = "►";
        whereToFindText.className = parentObject.getElementsByTagName('span')[1].className.replace(/\bopen\b/,'closed');
    }
}

function closeWhereToFind(obj) {
    var whereToFind = document.getElementById(obj);
    whereToFind.getElementsByTagName('div')[1].style.display="none";
    var classToChange = whereToFind.getElementsByTagName('span');
    classToChange[0].innerHTML = "►";
    classToChange[1].className = classToChange[1].className.replace(/\bopen\b/,'closed');
}