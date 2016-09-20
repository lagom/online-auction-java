var oldWindowOnLoad = window.onload;
window.onload = function() {

    var userSelect = document.getElementById("user-select");
    userSelect.onchange = function() {
        var user = userSelect.options[userSelect.selectedIndex].value;
        console.log(userSelect.selectedIndex);
        console.log(user);
        if (user) {
            var oReq = new XMLHttpRequest();
            oReq.onload = function (e) {
                window.location.reload();
            };
            oReq.open("POST", "/currentuser/" + user, true);
            oReq.send(null);
        }
    };


    if (oldWindowOnLoad) {
        oldWindowOnLoad();
    }
};
