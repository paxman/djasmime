from django import forms

class SignForm(forms.Form):
    sender = forms.EmailField(label='From address', max_length=100)
    reciever = forms.EmailField(label='To address', max_length=100)
    subject = forms.CharField(max_length=100)
    message = forms.CharField(widget=forms.Textarea)
    signed_content = forms.CharField(required=True,label='Signed content',widget=forms.Textarea)
    