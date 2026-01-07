@echo off
echo Testing server connectivity...
echo.
echo Testing localhost:5000...
curl -s http://localhost:5000/health
echo.
echo.
echo Testing 192.168.1.8:5000...
curl -s http://192.168.1.8:5000/health
echo.
echo.
echo Testing 10.0.2.2:5000 (emulator)...
curl -s http://10.0.2.2:5000/health
echo.
pause