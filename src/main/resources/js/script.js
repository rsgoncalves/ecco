/*******************************************************************************
* This file is part of ecco.
*
* ecco is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0.
*
* Copyright 2011-2014, The University of Manchester
*
* ecco is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
* General Public License as published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* ecco is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
* the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
* General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along with ecco.
* If not, see http://www.gnu.org/licenses/.
******************************************************************************/

// Change show/hide state
function toggleDiv() {
    for (var i = 0; i < arguments.length; i++) {
        if (document.getElementById(arguments[i]) != null) {
            if (document.getElementById(arguments[i]).style.display == 'none') {
                document.getElementById(arguments[i]).style.display = '';
            } else {
                document.getElementById(arguments[i]).style.display = 'none';
            }
        }
    }
}


// Open specific category
function open(divid) {
    document.getElementById(divid).style.display = '';
}


// Close specific category
function close(divid) {
    document.getElementById(divid).style.display = 'none';
}


// Toggle major groups (additions/removals)
function toggleGroup(change) {
    if (change == 'additions') {
        var effAdsTrigger = document.getElementsByName('effAddsTrigger');
        var ineffAdsTrigger = document.getElementsByName('ineffAddsTrigger');
        var additionsTrigger = document.getElementsByName('additions');
        if (additionsTrigger[0].checked) {
            effAdsTrigger[0].checked = true;
            ineffAdsTrigger[0].checked = true;
        } else {
            effAdsTrigger[0].checked = false;
            ineffAdsTrigger[0].checked = false;
        }
        toggleChanges('effAdds', 'effAddsTrigger');
        toggleChanges('ineffAdds', 'ineffAddsTrigger');
    } else if (change == 'removals') {
        var effRemsTrigger = document.getElementsByName('effRemsTrigger');
        var ineffRemsTrigger = document.getElementsByName('ineffRemsTrigger');
        var removalsTrigger = document.getElementsByName('removals');
        if (removalsTrigger[0].checked) {
            effRemsTrigger[0].checked = true;
            ineffRemsTrigger[0].checked = true;
        } else {
            effRemsTrigger[0].checked = false;
            ineffRemsTrigger[0].checked = false;
        }
        toggleChanges('effRems', 'effRemsTrigger');
        toggleChanges('ineffRems', 'ineffRemsTrigger');
    }
}


// Toggle groups of changes
function toggleChanges(field, change) {
    var array = document.getElementsByName(field);
    var trigger = document.getElementsByName(change);
    for (var i = 0; i < array.length; i++) {
        if (trigger[0].checked) {
            array[i].checked = true;
        } else {
            array[i].checked = false;
        }
    }
    if (field == 'effAdds') {
        showChanges(effAds, trigger);
    } else if (field == 'ineffAdds') {
        showChanges(inefAds, trigger);
    } else if (field == 'effRems') {
        showChanges(effRems, trigger);
    } else if (field == 'ineffRems') {
        showChanges(inefRems, trigger);
    }
}


// Show specific change type
function showChanges(array, trigger) {
    for (var i = 0; i < array.length; i++) {
        if (document.getElementById(array[i]) != null) {
            if (trigger[0].checked == true) {
                open(array[i]);
            } else {
                close(array[i]);
            }
        }
    }
}


// Show all categories
function showAll() {
    for (var i = 0; i < allDivs.length; i++) {
        if (document.getElementById(allDivs[i]) != null) {
            open(allDivs[i]);
        }
    }
    for (var i = 0; i < allTriggers.length; i++) {
        var t = document.getElementsByName(allTriggers[i]);
        
        for (var j = 0; j < t.length; j++) {
            t[j].checked = true;
        }
    }
}


// Hide all categories
function hideAll() {
    for (var i = 0; i < allDivs.length; i++) {
        if (document.getElementById(allDivs[i]) != null) {
            close(allDivs[i]);
        }
    }
    for (var i = 0; i < allTriggers.length; i++) {
        var t = document.getElementsByName(allTriggers[i]);
        
        for (var j = 0; j < t.length; j++) {
            t[j].checked = false;
        }
    }
}


// Check if specified element has associated class
function hasClass(ele, cls) {
    return ele.className.match(new RegExp('(\\s|^)' + cls + '(\\s|$)'));
}


// Add class to element
function addClass(ele, cls) {
    if (ele.id != 'top1' && ele.id != 'top2' && ele.id != 'top3') {
        if (! this.hasClass(ele, cls)) ele.className += " " + cls;
    }
}


// Remove class from element
function removeClass(ele, cls) {
    if (hasClass(ele, cls)) {
        var reg = new RegExp('(\\s|^)' + cls + '(\\s|$)');
        ele.className = ele.className.replace(reg, ' ');
    }
}


// Toggle class change for specified element
function changeClass(btn, cls) {
    if (! hasClass(btn, cls)) {
        addClass(btn, cls);
    } else {
        removeClass(btn, cls);
    }
}


// Toggle subtree & swap image
function toggleSubTree(id, img_id) {
    var e = document.getElementById(id);
    var img = document.getElementById(img_id);
    if (e.style.display == '') {
        e.style.display = 'none';
        img.src = 'images/button-closed.png';
    } else {
        e.style.display = '';
        img.src = 'images/button-open.png';
    }
}


// Set view-permalink value
function setPermalink() {
    var string = "#view=";
    for (var i = 0; i < allDivs.length; i++) {
        if (document.getElementById(allDivs[i]) != null) {
            if (document.getElementById(allDivs[i]).style.display == '') {
            	if(i == (allDivs.length-1))
            		string += allDivs[i];
            	else
            		string += allDivs[i] + "+";
            }
        }
    }
    if(string.endsWith("+")) 
    	string = string.substring(0, string.length-1);
    document.getElementById('view-plink').innerHTML=string;
}


// Select all text in a given text area
function select_all(id) {
    var ele = document.getElementById(id);
    var text_val = eval(ele);
    text_val.focus();
    text_val.select();
}


// Location hash change listeners
window.onhashchange = function(e) {
	processHashTag(getLocationHash());
};

window.onload = function() {
	processHashTag(getLocationHash());
};


function processHashTag(hash) {
	if(hash.startsWith('view=')) {
		hash = hash.substring(5);
		var tags = hash.split("+");
		hideAll();
		for(var i = 0; i < tags.length; i++) {
			if(document.getElementById(tags[i]) != null && document.getElementById(tags[i]).style.display == 'none') {
				open(tags[i]);
				var trigId = tags[i] + "trig"; 
				if(document.getElementById(trigId) != null) {
					checkAndExpand(trigId, 'tree1');
					checkAndExpand(trigId, 'tree2');
				}
			}
		}
	}
}

function checkAndExpand(trig, tree) {
	var li = document.getElementById(trig);
	$('#' + tree).checkboxTree('check', $( li ));
	var lipar = li.parentNode.parentNode;
	var ligpar = lipar.parentNode.parentNode;
	var liggpar = ligpar.parentNode.parentNode;
	$('#' + tree).checkboxTree('expand', $( lipar ));
	$('#' + tree).checkboxTree('expand', $( ligpar ));
	$('#' + tree).checkboxTree('expand', $( liggpar ));
	// yes, well, it works :)
}


//Get location hash
function getLocationHash() {
	return window.location.hash.substring(1);
}


// String.startsWith() function
if(typeof String.prototype.startsWith != 'function') {
	String.prototype.startsWith = function (str){
		return this.slice(0, str.length) == str;
	};
}


//String.endsWith() function
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}


// Popup info baloons
var tooltip = function () {
    var id = 'tt';
    var top = 3;
    var left = 3;
    var maxw = 320;
    var speed = 10;
    var timer = 20;
    var endalpha = 95;
    var alpha = 0;
    var tt, t, c, b, h;
    var ie = document.all ? true: false;
    return {
        show: function (v, w) {
            if (tt == null) {
                tt = document.createElement('div');
                tt.setAttribute('id', id);
                t = document.createElement('div');
                t.setAttribute('id', id + 'top');
                c = document.createElement('div');
                c.setAttribute('id', id + 'cont');
                b = document.createElement('div');
                b.setAttribute('id', id + 'bot');
                tt.appendChild(t);
                tt.appendChild(c);
                tt.appendChild(b);
                document.body.appendChild(tt);
                tt.style.opacity = 0;
                tt.style.filter = 'alpha(opacity=0)';
                document.onmousemove = this.pos;
            }
            tt.style.display = 'block';
            c.innerHTML = v;
            tt.style.width = w ? w + 'px': 'auto';
            if (! w && ie) {
                t.style.display = 'none';
                b.style.display = 'none';
                tt.style.width = tt.offsetWidth;
                t.style.display = 'block';
                b.style.display = 'block';
            }
            if (tt.offsetWidth > maxw) {
                tt.style.width = maxw + 'px'
            }
            h = parseInt(tt.offsetHeight) + top;
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(1)
            },
            timer);
        },
        pos: function (e) {
            var u = ie ? event.clientY + document.documentElement.scrollTop: e.pageY;
            var l = ie ? event.clientX + document.documentElement.scrollLeft: e.pageX;
            tt.style.top = (u - h) + 'px';
            tt.style.left = (l + left) + 'px';
        },
        fade: function (d) {
            var a = alpha;
            if ((a != endalpha && d == 1) || (a != 0 && d == - 1)) {
                var i = speed;
                if (endalpha - a < speed && d == 1) {
                    i = endalpha - a;
                } else if (alpha < speed && d == - 1) {
                    i = a;
                }
                alpha = a + (i * d);
                tt.style.opacity = alpha * .01;
                tt.style.filter = 'alpha(opacity=' + alpha + ')';
            } else {
                clearInterval(tt.timer);
                if (d == - 1) {
                    tt.style.display = 'none'
                }
            }
        },
        hide: function () {
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(- 1)
            },
            timer);
        }
    };
}
();


//Array of categories
var allDivs = new Array("st", "stnt", "add", "addnt", "newdesc", "stequiv", "stequivnt", "arewrite",
"aprw", "ared", "aavred", "anpred", "weak", "weakrt", "rem", "remrt", "retdesc", "wkequiv", "wkequivrt",
"rrewrite", "rprw", "rred", "ravred", "rnpred");

var effAds = new Array("st", "stnt", "add", "addnt", "newdesc", "stequiv", "stequivnt");
var inefAds = new Array("arewrite", "aprw", "ared", "aavred", "anpred");
var effRems = new Array("weak", "weakrt", "rem", "remrt", "retdesc", "wkequiv", "wkequivrt");
var inefRems = new Array("rrewrite", "rprw", "rred", "ravred", "rnpred");

var allTriggers = new Array('additions', 'effAddsTrigger', 'effAdds', 'ineffAddsTrigger',
'ineffAdds', 'removals', 'effRemsTrigger', 'effRems', 'ineffRemsTrigger', 'ineffRems');

var links = document.getElementsByTagName("a");