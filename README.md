For signup use /signup (http://localhost:8080/signup) with body in the form:
{ "fullName": "YourFullName","email": "YourEmail", "phoneNumber":"YourPhoneNumber (dont forget the + and contry code)","password": "YourPassword" }

For login use /login with body in the form:
{ "email": "YourEmail", "password": "YourPassword" }

For GoogleAuth(signup & login) use /google-login with body in the form:
{ "request": "YourGoogleToken" }
