POC Chrome/Opera Native Messaging browser email SMIME signing extension.

**Windows only**

###Requirements:###
* python (django) 
* java 8
* maven
* local mailserver

###Usage:###

1. In folder `native-app/djasmime` compile app with `mvn clean build install` 
2. Run `native-app/install_host.bat`
3. Open Chrome. Set Extensions to Developer mode, drag the `extension` folder and drop in the Extensions page.
4. Run `python django/manage.py` and open http://127.0.0.1:8000/ in your browser
5. Fill in the form, click sign, follow popup dialog instructions, and submit!

Log file in `native-app/log.txt`.