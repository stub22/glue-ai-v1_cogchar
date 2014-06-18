
console.log("screenLoadCoverFunctions executing")

function makeWrapper( oldFunc ) { return function() {showCover(); oldFunc(); } }

/* Loop through lifter buttons adding a wrapper that makes the cover show up before calling the buttons original function */
function embedWrapperOnButtons() {
    console.log("checking for buttons")
    var i = 1;
    var t = 12;
    for (i = 1; i <= t; i++) {
        console.log("checking for at position:" + i)
        var buttonSelection = document.getElementsByName('pushbutton' + i);
        if (buttonSelection != null) {
            var button = buttonSelection[0];
            if (button != null) {
/*                var func = button.onclick
                button.onclick = function() {
                    showCover();
                    func();
                }*/
		
		var oldFunc = button.onclick
		button.onclick = makeWrapper( oldFunc )


		console.log("Added showCover to a button")
            }
        }
    }
}

/*this makes the cover visisble */
function showCover() {
document.getElementById("screenLoadCover").style.visibility = "visible";
}

/* this makes the cover hidden, and embeds the code to show it on any buttons */
function hideCover() {
    document.getElementById("screenLoadCover").style.visibility = "hidden";
}

function lifterInit() {
    console.log("init fired")
    embedWrapperOnButtons();
    hideCover()
}

window.load = lifterInit;

