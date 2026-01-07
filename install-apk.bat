@echo off
echo ========================================
echo Installation EduNova Mobile APK
echo ========================================
echo.

set ADB_PATH=C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

echo Verification des devices connectes...
"%ADB_PATH%" devices
echo.

echo Tentative d'installation de l'APK...
"%ADB_PATH%" install -r "%APK_PATH%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Installation reussie !
    echo L'application EduNova Mobile est maintenant installee.
) else (
    echo.
    echo ❌ Echec de l'installation
    echo.
    echo Solutions possibles:
    echo 1. Verifiez que votre telephone est connecte en USB
    echo 2. Activez le "Debogage USB" dans les options developpeur
    echo 3. Autorisez le debogage USB sur votre telephone
    echo 4. Ou installez manuellement l'APK: %APK_PATH%
)

echo.
pause