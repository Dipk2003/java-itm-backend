$headers = @{ 'Content-Type' = 'application/json' }
$body = @{
    email = 'taksha@gmail.com'
    phone = '1234567890'
    name = 'Taksha'
    password = 'password123'
} | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8080/auth/register' -Method POST -Headers $headers -Body $body
