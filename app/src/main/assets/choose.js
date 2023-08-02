window.boabot = (new (function(){
	document.body.addEventListener("click", function(e){choose(e.target)});
	function choose(obj){
		var boa = {};
		var text = "";
		switch(obj.tagName){
			case "INPUT":
			case "TEXTAREA":
				text = obj.getAttribute("placeholder");
				break;
			default:
				if(obj.title){
					text = obj.title;
				}else{
					text = obj.innerHTML.replace(/<[^>]+>/g, "").trim();
					text = text.substring(0, 15);
				}
		}
		boa.text = obj.text;
		boa.tag = obj.tagName;
		if(obj.id){
			boa.id = obj.id;
		}else{
			boa.cls = obj.className.trim();
			boa.name = obj.name;
			boa.val = obj.value;
		}
		if(!text) text = boa.tag;
		app.choose(JSON.stringify(boa), text);
	}
})());