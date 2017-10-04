function changeInputValue(sel, id) {
	if (sel.value == 'basic_data_email') {
		alert('I see this code!')
		document.getElementsByName('value' + id)[0].setAttribute('type', 'email');
	}
//	if (document.getElementByName('property' + id).value == 'basic_data_email') {
//	    document.getElementByName('value' + id).setAttribute('type', 'email');
//	}
//	else if (document.getElementByName('property' + id).value == 'osd2017_speaker') {
//		document.getElementByName('value' + id).setAttribute('type', 'radio');
//	}
}