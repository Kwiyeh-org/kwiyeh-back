<center><h1>AUTHENTICATION</h1></center>


<h2>Signup</h2>

<ul>
  <li>Use <strong>/signup (http://localhost:8080/signup)</strong> with body in the form:</li>
  <li><strong>{"fullName": "YourFullName","email": "YourEmail","phoneNumber":"+273612345678","password": "YourPassword"}</strong>   </li>
  <li>Returns</li>
</ul>

<h2>Login</h2>

<ul>
  <li>Use <strong>/login</strong>  with body in the form:</li>
  <li><strong>{"email": "YourEmail","password": "YourPassword"}</strong></li>
  <li>Returns </li>
</ul>

<h2>Google Auth</h2>

<ul>
  <li>Use <strong>/google-login</strong>  with body in the form:</li>
  <li><strong>{"request": "YourGoogleToken"}</strong></li>
  <li>Returns </li>
<ul/>
