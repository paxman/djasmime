
function sendExtMessageSign() 
{
	var text = document.getElementById("id_message").value;

	if (text.length == 0)
	{
		alert('Cannot sign empty message!');
		return;
	}

    chrome.runtime.sendMessage("ldbpeldocafmlnjhcdlcdemlplgnnlpd", {content: document.getElementById('id_message').value},
      function(response) {
        
        if (!response.success)
          {
        	console.log(response.error);
        	return;
          }
          
        if (response.success)
        	alert('Sucessfully signed message');

        document.getElementById('id_signed_content').innerHTML = response.signedContent;
        document.getElementById('submit_button').disabled = false;
        document.getElementById('sign_button').disabled = true;
        
      });
}

document.addEventListener('DOMContentLoaded', function () {
  document.getElementById('sign_button').addEventListener(
      'click', sendExtMessageSign);
});
