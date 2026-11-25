# Aplicación de adopciones

Frontend Angular y backend Spring Boot con Java 21, H2 y Docker Compose.

## Preparación

Se necesitan Docker Desktop, Java 21 y Node.js compatible con el proyecto.
En la carpeta principal, crear un archivo `.env` local (excluido de Git):

```dotenv
APP_ADMIN_PASSWORD=REEMPLAZAR_POR_UNA_CLAVE_PROPIA
```

La clave se utiliza para crear el administrador inicial; no cambia la contraseña de un usuario que ya exista en el volumen.

## Compilación y despliegue en PowerShell

```powershell
cd backend
.\mvnw.cmd clean package
docker build -t adopciones-backend .
cd ..\frontend
npm ci
npm run build
docker build -t adopciones-frontend .
cd ..
docker volume create adopciones-datos
docker compose up -d
docker compose ps
docker compose logs --tail 50 backend
```

Abrir http://localhost:4200 después de que Spring Boot termine de iniciar.
El volumen externo conserva la base de datos H2. Para detener los servicios:

```powershell
docker compose stop
```

El informe Word en `entregables` documenta el despliegue realizado. Para nuevas instalaciones, aplicar también la configuración de contraseña por variable de entorno descrita aquí.
