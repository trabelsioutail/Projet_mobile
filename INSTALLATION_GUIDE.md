# Guide d'Installation EduNova Mobile

## 📱 Installation sur Téléphone Physique

### Option 1: Installation Automatique (Recommandée)
1. **Connectez votre téléphone** en USB à l'ordinateur
2. **Activez le débogage USB** :
   - Allez dans `Paramètres > À propos du téléphone`
   - Tapez 7 fois sur "Numéro de build"
   - Retournez aux paramètres, allez dans `Options pour les développeurs`
   - Activez `Débogage USB`
3. **Exécutez** : `install-apk.bat`

### Option 2: Installation Manuelle
1. **Copiez l'APK** `app/build/outputs/apk/debug/app-debug.apk` sur votre téléphone
2. **Activez les sources inconnues** :
   - `Paramètres > Sécurité > Sources inconnues` (Android < 8)
   - `Paramètres > Applications > Accès spécial > Installer des apps inconnues` (Android 8+)
3. **Ouvrez l'APK** avec le gestionnaire de fichiers et installez

## 🌐 Configuration Réseau

### Adresses IP Configurées
- **Émulateur** : `http://10.0.2.2:5000/api/`
- **Téléphone physique** : `http://192.168.1.8:5000/api/`

### Vérification de la Connectivité
1. **Assurez-vous que le backend tourne** sur le port 5000
2. **Vérifiez que votre téléphone et PC sont sur le même WiFi**
3. **Testez la connexion** : `test-connection.bat`

## 🔧 Dépannage

### Si l'app ne se connecte pas :
1. Vérifiez que le serveur backend est démarré
2. Vérifiez l'IP de votre PC : `ipconfig`
3. Mettez à jour l'IP dans `build.gradle.kts` si nécessaire
4. Recompilez : `./gradlew assembleDebug`

### Si l'installation échoue :
1. Vérifiez les pilotes USB de votre téléphone
2. Essayez un autre câble USB
3. Redémarrez ADB : `adb kill-server && adb start-server`
4. Utilisez l'installation manuelle

## 📋 Comptes de Test

### Administrateur
- Email: `admin@edunova.com`
- Mot de passe: `admin123`

### Enseignant
- Email: `ghofrane@gmail.com`
- Mot de passe: `password123`

### Étudiant
- Email: `student1@edunova.com`
- Mot de passe: `student123`