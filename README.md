<center><h1>AUTHENTICATION</h1></center>



<h2>Signup</h2>

<ul>
  <li>Use <strong>[POST] /signup (http://localhost:8080/signup)</strong></li>
  <li>Parameters: </li>
  <li>Body :<strong>{"fullName": "YourFullName","email": "YourEmail","phoneNumber":"+273612345678","password": "YourPassword","role":"UserRole"}</strong>   </li>
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
  <li>Body :<strong>{"token": "YourGoogleToken","role":"UserRole"}</strong></li>
  <li>Returns code 200, response {Google login of UserEmail successful}</li>
  <li>Returns something, try it</li>
</ul>

<h2>Password Reset</h2>

<ul>
  <li>Firstly, Use <strong>[GET] /forgetPassword</strong></li>
  <li>Parameters: email = UserEmail</li>
  <li>Body :</li>
  <li>Returns code 200, response { check your mail }</li>
  <li>Returns something, try it, could not</li><br/>

  <li>Then, Use <strong>[Post] /verifyCode</strong></li>
  <li>Parameters:
  <li>Body : {
    "email":"UserEmail",
    "forgetPasswordCode":"XXXX"
  }</li>
  <li>Returns code 200, response { "passwordToken": "Token", "expiresIn": "600" }</li>
  <li>Returns something, try it, could not</li><br/>

  <li>Finally, Use <strong>[POST] /resetPassword</strong></li>
  <li>Header: Authorization = Bearer YourToken</li>
  <li>Parameters:</li>
  <li>Body : {
    "email":"UserEmail",
    "password":"NewPassword"
}</li>
  <li>Returns code 200, response { Password reset done }</li>
  <li>Returns something, try it</li>
</ul>

<center><h1>IN APP Requests</h1></center>
<p>There request require Authentication. To do so, add the header <br/><strong>Header: Authorization = Bearer YourToken<strong></p>

<h2>Get User Info</h2>

<ul>
  <li>Use <strong>[GET] /getUserInfo</strong></li>
  <li>Parameters: uid = UserUid </li>
  <li>Body : </li>
  <li>Returns code 200,response {
  "uid": "UserUid",
  "email": "UserEmail",
  "fullName": "UserName",
  "phoneNumber": "UserPhoneNumber",
  "role": "UserRole" (client, talent or admin)
  }</li>
  <li>Returns something, try it</li>
</ul>

<h2>Delete Account</h2>

<ul>
  <li>Use <strong>[DELETE] /deleteAccount</strong></li>
  <li>Parameters:</li>
  <li>Body : </li>
  <li>Returns Returns code 200,response {Account deleted successfully}</li>
  <li>Returns something, try it</li>
</ul>

<h2>Update User Info</h2>

<ul>
  <li>Use <strong>[POST] /updateUserInfo</strong></li>
  <li>Parameters:</li>
  For Clients <br>
  Body : {
    "uid":"UserUID",
    "email":"UserEmail",
    "fullName":"FullName",
    "password":"NewPassword",
    "phoneNumber": "UserPhoneNumber",
    "role": "UserRole" (client, talent or admin),
    "clientImageUrl": "ClientImageUrl"
  }<br>
  For Talents <br>
  Body : {
    "uid":"UserUID",
    "email":"UserEmail",
    "fullName":"FullName",
    "password":"NewPassword"
    "phoneNumber": "UserPhoneNumber",
    "role": "UserRole" (client, talent or admin),
    "talentName": "TalentName",
    "talentDescription": "TalentDescription",
    "talentCategory": "TalentCategory",
    "talentImageUrl": "ClientImageUrl",
    "pricing": "pricing",
    "availability":"availability"
  }</li>
  <li>Returns Returns code 200,response {User information updated successfully }</li>
  <li>Returns something, try it</li>
</ul>

<h2>Get Talents</h2>

<ul>
  <li>Use <strong>[GET] /getTalents</strong></li>
  <li>Parameters: uid = UserUid</li>
  <li>Body : </li>
  <li>Returns Returns code 200,response {No talents found}</li>
  <li>Returns Returns code 200,response {
    [
      {
        "uid":"UserUID",
        "email":"UserEmail",
        "fullName":"FullName",
        "password":"NewPassword"
        "phoneNumber": "UserPhoneNumber",
        "role": "UserRole" (client, talent or admin),
        "talentName": "TalentName",
        "talentDescription": "TalentDescription",
        "talentCategory": "TalentCategory",
        "talentImageUrl": "TalentImageUrl",
        "pricing": "pricing",
        "availability":"availability"
      },
      ...
    ]
    }</li>
  <li>Returns something, try it</li>
</ul>

<h2>Note</h2>

<ul>
  <li>Categories should be saved in the form "category1,category2,..."</li>
  <li>Rating is a real number between 1 and 5</li>
</ul>