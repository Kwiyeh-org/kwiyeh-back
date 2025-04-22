<center><h1>AUTHENTICATION</h1></center>


<h2>Signup</h2>

<ul>
  <li>Use <strong>[POST] /signup (http://localhost:8080/signup)</strong></li>
  <li>Parameters: </li>
  <li>Body :<strong>{"fullName": "YourFullName","email": "YourEmail","phoneNumber":"+273612345678","password": "YourPassword"}</strong>   </li>
  <li>Returns code 200, response { User created: UserUID }</li>
  <li>Returns code 409, response { Email already in use }</li>
  <li>Returns something, try it</li>
</ul>

<h2>Login</h2>

<ul>
  <li>Use <strong>[POST] /login</strong></li>
  <li>Parameters: </li>
  <li>Body :<strong>{"email": "YourEmail","password": "YourPassword"}</strong></li>
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
  <li>Use <strong>[POST] /google-login</strong></li>
  <li>Parameters: </li>
  <li>Body :<strong>{"request": "YourGoogleToken"}</strong></li>
  <li>Returns code 200, response { UId: UserUID, email : UserEmail }</li>
  <li>Returns something, try it</li>
</ul>

<h2>Password Reset</h2>

<ul>
  <li>Firstly, Use <strong>[GET] /forgetPassword</strong></li>
  <li>Parameters: email = UserEmail</li>
  <li>Body :</li>
  <li>Returns code 200, response { check your mail }</li>
  <li>Returns something, try it, could not</li><br/>

  <li>Finaly, Use <strong>[POST] /resetPassword</strong></li>
  <li>Parameters:</li>
  <li>Body : {
    "email":"UserEmail",
    "forgetPasswordCode":"XXXX",
    "password":"YourPassword"
}</li>
  <li>Returns code 200, response { Password reset done }</li>
  <li>Returns something, try it</li>
</ul>

