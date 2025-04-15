<header>AUTHENTICATION</header><br/><br/>
<ul>
<li>For signup use /signup (http://localhost:8080/signup) with body in the form:</li>
{ 
  "fullName": "YourFullName",
  "email": "YourEmail",
  "phoneNumber":"YourPhoneNumber (dont forget the + and contry code)",
  "password": "YourPassword"
}<br/>

<li>For login use /login with body in the form:</li>
{
  "email": "YourEmail",
  "password": "YourPassword" 
}<br/>

<li>For GoogleAuth(signup & login) use /google-login with body in the form:</li>
{
 "request": "YourGoogleToken"
}<br/>
</ul>
