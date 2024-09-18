curl --location 'http://localhost:8080/solver?secret=cy9tYXZlbi9dCltXQVJOSU5HXSBGYWlsZWQgdG8gZG93bmxvYWQgdG9tY2F0LWVtYmVkLXdlYnNvY2tldC0xMC4xLjI4LmphciBbaHR0cHM6Ly9naXRsYWIucm5nLWFkbS5jb20vYXBpL3Y0L3Byb2plY3RzLzE2L3BhY2thZ2VzL21hdmVuL10KCg%3D%3D' \
--header 'Content-Type: application/json' \
--data '{
    "eq1": "",
    "eq2": "",
    "w1": 10
}' &

sleep 1

PID=$(lsof -t -i:8080 | head -n 1)

if [ -z "$PID" ]; then
    echo "Application is not running"
else
    echo "Terminating application with PID ${PID}"
    kill -15 "$PID"
fi
