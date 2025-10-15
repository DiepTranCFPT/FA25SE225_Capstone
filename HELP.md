
docker-compose up -d
http://localhost:8080/edcare/api/v1/swagger-ui/index.html

https://fa25se225capstone-production.up.railway.app/edcare/api/v1

frontend lấy oauth2 code từ url của google rồi gửi code đó cho backend (gửi vào endpoint outbound)
backend nhận code đó và tự liên lạc với google để nhân token của google rồi trả (token này dùng được). nhưng sẽ đổi ngang vớitoken của hệ thống để hoàn toàn kiểm soát


https://developers.google.com/identity/protocols/oauth2/web-server
POST /token HTTP/1.1
Host: oauth2.googleapis.com
Content-Type: application/x-www-form-urlencoded

code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7&
client_id=your_client_id&
client_secret=your_client_secret&
redirect_uri=https%3A//oauth2.example.com/code&
grant_type=authorization_code
