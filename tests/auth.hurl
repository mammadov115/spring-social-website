# 1. REGISTER
POST http://localhost:8080/api/auth/register
Content-Type: application/json
{
  "email": "test@example.com",
  "password": "Test1234!",
  "firstName": "Test",
  "lastName": "User"
}

HTTP 200
[Asserts]
jsonpath "$.message" exists


# 2. LOGIN
POST http://localhost:8080/api/auth/login
Content-Type: application/json
{
  "email": "test@example.com",
  "password": "Test1234!"
}

HTTP 200
[Asserts]
jsonpath "$.accessToken" exists
jsonpath "$.tokenType" exists

[Captures]
access_token: jsonpath "$.accessToken"


# 3. LOGOUT
POST http://localhost:8080/api/auth/logout

HTTP 200