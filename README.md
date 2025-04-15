<center><h1>AUTHENTICATION</h1></center>
<u>

<h2>Signup</h2>
<p>For signup use <strong>/signup (http://localhost:8080/signup)</strong> with body in the form:<br/>
<strong>{"fullName": "YourFullName","email": "YourEmail","phoneNumber":"YourPhoneNumber" (dont forget the + and contry code),"password": "YourPassword"}</strong> </p>

<h2>Login</h2>
<p>For login use <strong>/login</strong>  with body in the form:<br/>
<strong>{"email": "YourEmail","password": "YourPassword"}</strong> </p>

<h2>Google Auth</h2>
<p>For GoogleAuth(signup & login) use <strong>/google-login</strong>  with body in the form:<br/>
<strong>{"request": "YourGoogleToken"}</strong></p>

