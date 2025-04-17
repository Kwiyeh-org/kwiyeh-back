<center><h1>AUTHENTICATION</h1></center>


<h2>Signup</h2>

<ul>
  <li>Use <strong>[POST] /signup (http://localhost:8080/signup)</strong> with body in the form:</li>
  <li><strong>{"fullName": "YourFullName","email": "YourEmail","phoneNumber":"+273612345678","password": "YourPassword"}</strong>   </li>
  <li>Returns code 200, response { User created: UserUID }</li>
  <li>Returns code 409, response { Email already in use }</li>
  <li>Returns something, try it</li>
</ul>

<h2>Login</h2>

<ul>
  <li>Use <strong>[POST] /login</strong>  with body in the form:</li>
  <li><strong>{"email": "YourEmail","password": "YourPassword"}</strong></li>
  <li>Returns Returns code 200,response {
  "kind": "identitytoolkit#VerifyPasswordResponse",
  "localId": "UserUid",
  "email": "UserEmail",
  "displayName": "UserName",
  "idToken":"IdToken",
  "registered": true,
  "refreshToken": "RefreshToken",
  "expiresIn": "3600"
}</li>
  <li>Returns something, try it</li>
</ul>

<h2>Google Auth</h2>

<ul>
  <li>Use <strong>[POST] /google-login</strong>  with body in the form:</li>
  <li><strong>{"request": "YourGoogleToken"}</strong></li>
  <li>Returns something, try it, could not</li>
<ul/>
