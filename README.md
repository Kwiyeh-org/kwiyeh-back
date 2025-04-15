<header>AUTHENTICATION</header><br/><br/>
<ul>
<li>For signup use /signup (http://localhost:8080/signup) with body in the form:</li><br/>
{ "fullName": "YourFullName","email": "YourEmail", "phoneNumber":"YourPhoneNumber (dont forget the + and contry code)","password": "YourPassword" }

<li>For login use /login with body in the form:</li><br/>
{ "email": "YourEmail", "password": "YourPassword" }

<li>For GoogleAuth(signup & login) use /google-login with body in the form:</li><br/>
{ "request": "YourGoogleToken" }
</ul>
