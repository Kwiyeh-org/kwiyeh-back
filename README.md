<header>AUTHENTICATION</header><centre/><br/><br/><u>
<ul>
<li>For signup use /signup (http://localhost:8080/signup) with body in the form:<br/>
{ <br/>
  "fullName": "YourFullName",<br/>
  "email": "YourEmail",<br/>
  "phoneNumber":"YourPhoneNumber (dont forget the + and contry code)",<br/>
  "password": "YourPassword"<br/>
}</li><br/>

<li>For login use /login with body in the form:<br/>
{<br/>
  "email": "YourEmail",<br/>
  "password": "YourPassword" <br/>
}</li><br/>

<li>For GoogleAuth(signup & login) use /google-login with body in the form:<br/>
{<br/>
 "request": "YourGoogleToken"<br/>
}</li><br/>
</ul>
