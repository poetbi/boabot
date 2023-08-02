window.boabot = (new (function(){
	
	this.input = function(obj, val){
		
	}

	function click(ele){
		var ev = document.createEvent('HTMLEvents');
		ev.isTrusted = true;
		ev.clientX = 625
		ev.clientY = 356
		ev.initEvent('click', true, false);
		ele.dispatchEvent(ev);
	}
})());