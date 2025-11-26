```.env
# Variables sensibles para ESIMedia

# MongoDB
MONGODB_USERNAME=<YOUR_MONGODB_USERNAME>
MONGODB_PASSWORD=<YOUR_MONGODB_PASSWORD>

# Email
EMAIL_PASSWORD=<YOUR_EMAIL_PASSWORD>
EMAIL_PASSWORD_APP=<YOUR_EMAIL_PASSWORD_APP>
EMAIL_CONFIRMATION_URL=<YOUR_EMAIL_CONFIRMATION_URL>
EMAIL_RESET_URL=<YOUR_EMAIL_RESET_URL>

# Token para jwt auth (generado seguramente)
TOKEN_SECRET=<YOUR_TOKEN_SECRET>

SENDGRID_API_KEY=<YOUR_SENDGRID_API_KEY>

# Pepper para hashing seguro de contraseñas (¡cambia este valor en producción!)
PEPER_VALUE=<YOUR_PEPPER_VALUE>

# Configuración de ngrok para ClamAV
# Reemplaza los valores con los que proporciona ngrok
# Ejecuta: ngrok tcp 3310
# Ejemplo output: "Forwarding tcp://0.tcp.ngrok.io:12345 -> localhost:3310"
# Entonces actualiza:
#   CLAMAV_HOST=0.tcp.ngrok.io
#   CLAMAV_PORT=12345

CLAMAV_HOST=<YOUR_CLAMAV_HOST>
CLAMAV_PORT=<YOUR_CLAMAV_PORT>
CLAMAV_TIMEOUT=30
```