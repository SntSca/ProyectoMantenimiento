Pasos para montar los proyectos (Se presupone que ya tienes el código subido a github):
Este proyecto usa los siguientes servicios:
- sendgrid para el envío de correos tanto en desarrollo como en producción.
- clamAV, docker y ngrok para el antivirus y su redirección a este.
- Angular con typescript. Para ver las URLs a las que llama y sus endpoints, mirar el archivo environment.ts
- Backend con las carpetas separadas en features. También tenemos una carpeta shared donde almacenamos aquellos programas que son estáticos y son como librerías internas que hemos tenido que crear.

Tenemos las siguientes pipelines:
- CI en frontend.
- CI en backend.
- CD en frontend (Firebase).

Todas las ejecutamos mediante un agente local conectado al azure pipelines.

El resto de contenido estará disponible en la wiki del Azure DevOps, desde configuraciones como servicios.