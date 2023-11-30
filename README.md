# dai-lab-SMTP

Description of the project
----------
This project allows you to send a manually defined number of emails to a group of people.

The sender and the receivers are randomly chosen from a given file containing adresses.

The content of the message is randomly defined from a file containing a list of messages (object + body).


Instructions for setting up MailDev (mock SMTP server)
----------
If you want to test email's sending before to do it for real, you can use [MailDev](https://github.com/maildev/maildev). This link bring you to the git repository of the tool. You can find all instructions there.
If you know the process, you can run this command to start the server (Web interface on localhost:1080 and SMTP server on localhost:1025) :

    docker run -d -p 1080:1080 -p 1025:1025 maildev/maildev