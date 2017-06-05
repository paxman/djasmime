from django.shortcuts import render

from .forms import SignForm
import smtplib
from email.mime.base import MIMEBase

def index(request):

    sent = False
    
    if request.method == 'POST':
        
        form = SignForm(request.POST)

        if form.is_valid():
            
            payload = form.cleaned_data['signed_content']
            
            content = MIMEBase('application','pkcs7-mime')
            content.set_param(u'name',u'smime.p7m',u"Content-Type", True)
            content.set_param(u'smime-type',u'signed-data',u"Content-Type", True)
            content.set_payload('\n'.join(payload[pos:pos+76] for pos in xrange(0, len(payload), 76)))
            content['Content-Transfer-Encoding'] = "base64"
            content['Content-Disposition'] = """attachment; filename=\"smime.p7m\""""
            content['Content-Description'] = "S/MIME Cryptographic Signed Data"
            del content['MIME-Version']

            content['Subject'] = form.cleaned_data['subject']
            content['From'] = form.cleaned_data['sender']
            content['To'] = form.cleaned_data['reciever']
            
            smtp = smtplib.SMTP("localhost")
            smtp.sendmail(  form.cleaned_data['sender'], form.cleaned_data['reciever'], content.as_string(True) )
            smtp.quit()
            smtp.close()
            sent = True
            
    form = SignForm()

    return render(request, 'web/sign.html', {'form': form,'sent':sent})
