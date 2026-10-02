# GRINGO SEXY — Plataforma Web Full Stack

> Plataforma web completa y moderna enfocada en modificaciones de sistema, sensibilidades milimétricas, optimización extrema de FPS, calibración de batería y privacidad móvil para smartphones (iPhone, Samsung, Xiaomi, etc.).

---

## 🚀 Stack Tecnológico

- **Java 17 (Temurin / OpenJDK)**
- **Spring Boot 3.2.5**
- **Spring MVC**
- **Spring Security 6 (BCrypt 12)**
- **Spring Data MongoDB**
- **MongoDB 7.0+**
- **Thymeleaf 3 + Spring Security Dialect**
- **JavaScript Vanilla ES6+**
- **CSS3 Moderno con Glassmorphism & Cyber Dark Theme**
- **Maven**

---

## 🏛 Arquitectura del Proyecto

```text
com.gringosexy
├── config            # Configuración de Seguridad, Mongo, Auditoría, Seeds y Web
├── controller        # Controladores MVC Públicos, de Usuario y de Administración
│   └── admin         # Panel de Control Administrativo
├── dto               # Objetos de Transferencia de Datos validados con Bean Validation
├── enums             # Role, UserStatus, DeviceType, ContentCategory, TicketStatus
├── exception         # Manejador Global de Excepciones (400, 403, 404, 500)
├── model             # Documentos MongoDB (User, Content, Device, Category, etc.)
├── repository        # Repositorios Spring Data MongoDB con consultas y paginación
├── service           # Lógica de negocio e interfaces (UserService, EmailService, etc.)
│   └── impl          # Implementaciones con soporte asíncrono y auditoría
└── util              # Utilidades de seguridad, IP y tokens
```

---

## ⚙️ Configuración y Variables de Entorno

Copia el archivo `.env.example` a tu configuración local o exporta las siguientes variables:

```properties
# Servidor y Base de Datos
PORT=8080
MONGODB_URI=mongodb://localhost:27017/gringosexy_db
APP_BASE_URL=http://localhost:8080

# Envío Real de Correos (SMTP Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-correo@gmail.com
MAIL_PASSWORD=tu-contraseña-de-aplicacion-google
APP_MAIL_FROM=noreply@gringosexy.com

# Super Administrador Inicial (Semilla Segura)
SUPERADMIN_USERNAME=admin
SUPERADMIN_EMAIL=admin@gringosexy.com
SUPERADMIN_PASSWORD=Admin12345!
SUPERADMIN_FULLNAME=Super Admin GRINGO SEXY

# Redes Sociales Oficiales
SOCIAL_DISCORD=https://discord.gg/gringosexy
SOCIAL_TIKTOK=https://tiktok.com/@gringosexy
SOCIAL_INSTAGRAM=https://instagram.com/gringosexy
SOCIAL_YOUTUBE=https://youtube.com/@gringosexy
```

---

## 🛠 Cómo Ejecutar el Proyecto

### 1. Iniciar MongoDB Local
Asegúrate de tener un servidor MongoDB corriendo en `localhost:27017` o ejecuta con Docker:
```bash
docker run -d -p 27017:27017 --name mongo-gringo mongo:7.0
```

### 2. Compilar y Ejecutar con Maven
```bash
mvn spring-boot:run
```

O para compilar el paquete empaquetado:
```bash
mvn clean package
java -jar target/gringosexy-platform-1.0.0.jar
```

---

## 🔒 Credenciales del Administrador Inicial

Al arrancar la aplicación por primera vez, `DataInitializer` creará de manera automática y segura el Super Administrador y los contenidos iniciales curados:

- **Usuario:** `admin` (o `admin@gringosexy.com`)
- **Contraseña:** `Admin12345!`
- **Panel Administrativo:** `http://localhost:8080/admin`

---

## 📱 Funcionalidades Principales

1. **Autenticación & Seguridad Real:**
   - Registro con selección de modelo de teléfono.
   - Encriptación con **BCrypt** de 12 rondas.
   - Envío de token de verificación por correo con expiración de 24h.
   - Recuperación segura de contraseñas.
   - Control estricto de roles: `USER`, `ADMIN`, `SUPER_ADMIN`.
   - Control de estados: `PENDING`, `ACTIVE`, `SUSPENDED`, `BLOCKED`.
2. **Doble Capa de Autorización:**
   - Filtrado en frontend mediante Thymeleaf Security.
   - Comprobación en backend por Controller y Service impidiendo accesos directos por URL (403 Forbidden).
3. **Contenido Segmentado por Dispositivo:**
   - Filtrado automático para el usuario según su teléfono (`iPhone`, `Samsung`, `Xiaomi`, etc.).
4. **Panel de Control Administrativo (`/admin`):**
   - Métricas y estadísticas de usuarios por modelo y estado.
   - CRUD completo de Usuarios con paginación real de MongoDB, filtros y búsqueda.
   - Matriz de permisos individuales por módulo para cada usuario.
   - CRUD de Contenidos con soporte multidispositivo.
   - Gestión de marcas/dispositivos y categorías.
   - Bandeja de tickets de soporte con formulario de respuesta directa.
   - Registro de auditoría y trazabilidad completa (`ActivityLog`).
5. **Diseño Responsive de Alta Calidad:**
   - Adaptado a 320px, 375px, 390px, 430px, 768px, 1024px y 1920px sin scrolls horizontales accidentales.
   - Notificaciones Toast interactivas y microinteracciones fluidas.
