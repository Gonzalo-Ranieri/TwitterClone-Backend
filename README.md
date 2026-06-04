# Twitter Clone - Arquitectura, Runbook y Especificación Técnica

> **⚠️ Aviso Crítico de Arquitectura:** Este proyecto implementa una arquitectura Full-Stack desacoplada. Este repositorio aloja el núcleo del servidor (Java/Spring Boot), la base de datos relacional y la orquestación Docker. El código fuente del cliente de interfaz de usuario (React/TypeScript) se encuentra en el repositorio complementario: [https://github.com/Gonzalo-Ranieri/TwitterClone-Frontend](https://github.com/Gonzalo-Ranieri/TwitterClone-Frontend).

Este repositorio aloja el servidor de la plataforma de microblogging Twitter Clone: una API RESTful de alta cohesión desarrollada en **Spring Boot (Java 21)**, respaldada por una base de datos relacional **PostgreSQL** y complementada por el archivo de orquestación Docker que levanta el ecosistema completo (servidor, cliente y base de datos) desde un único punto de control.

---

## 1. Título y Visión General

El proyecto "Twitter Clone" es una plataforma distribuida de publicación asíncrona de mensajes (tweets) e interacciones sociales en tiempo real. La arquitectura está estructurada en dos capas independientes que residen en repositorios separados:

- **Capa de Backend (este repositorio)**: API RESTful stateless que gestiona el grafo social (relaciones de seguimiento, me gusta), la persistencia del timeline, el procesamiento transaccional de hilos de respuestas, y la transmisión de eventos en tiempo real (Server-Sent Events).
- **Capa de Frontend ([repositorio complementario](https://github.com/Gonzalo-Ranieri/TwitterClone-Frontend))**: Aplicación web de página única (SPA) diseñada para garantizar un renderizado eficiente, optimización de red mediante debouncing, y actualizaciones optimistas de la interfaz para brindar una experiencia fluida al usuario final.

---

## 2. Runbook (Setup & Operación) - CRÍTICO

Este runbook describe detalladamente la configuración y despliegue del entorno para garantizar una puesta en marcha reproducible y libre de fallos.

### Prerrequisitos

Para ejecutar o desarrollar este sistema, es estrictamente obligatorio contar con el siguiente entorno preinstalado en la máquina host:

- **Java Development Kit (JDK) 21**: Versión mínima 21 (ej. Eclipse Temurin o OpenJDK).
- **Node.js**: Versión mínima 20.x y gestor de paquetes `npm`.
- **Apache Maven**: Versión mínima 3.8+ (o utilizar el envoltorio `./mvnw` incluido).
- **Docker**: Versión de motor 20.10+ o superior.
- **Docker Compose**: Versión 2.x o superior.

---

### Clonado e Instalación

Dado que la arquitectura está desacoplada en dos repositorios distintos, el archivo `docker-compose.yml` incluido en este repositorio define contextos de construcción relativos (`./backend` y `./frontend`) que asumen una **estructura de directorio padre compartida**. Es obligatorio clonar ambos repositorios como subdirectorios hermanos dentro de un mismo directorio raíz. Ejecute los siguientes comandos en el orden indicado:

```bash
# 1. Cree y acceda al directorio padre que contendrá ambos repositorios
mkdir twitterClone && cd twitterClone

# 2. Clone el repositorio del servidor (este repositorio) como subdirectorio "backend"
git clone https://github.com/Gonzalo-Ranieri/TwitterClone-Backend backend

# 3. Clone el repositorio del cliente como subdirectorio "frontend"
git clone https://github.com/Gonzalo-Ranieri/TwitterClone-Frontend frontend
```

Una vez completado el clonado, la estructura de directorios en la máquina del evaluador deberá ser la siguiente:

```
twitterClone/                  ← directorio padre (creado manualmente)
├── backend/                   ← repositorio TwitterClone-Backend (este repo)
│   ├── docker-compose.yml     ← orquestador Docker maestro
│   ├── README.md              ← este documento
│   ├── Dockerfile
│   └── src/
└── frontend/                  ← repositorio TwitterClone-Frontend
    ├── Dockerfile
    └── src/
```

> **Nota Técnica:** El archivo `docker-compose.yml` ubicado en la raíz de `backend/` referencia los contextos `./backend` y `./frontend` de forma relativa al directorio desde donde se invoca el comando. Por lo tanto, **el comando `docker-compose up` debe ejecutarse siempre desde el directorio `twitterClone/`** (el directorio padre), no desde dentro del subdirectorio `backend/`.

---

### Variables de Entorno

El backend y el frontend están parametrizados mediante variables de entorno configurables. En las respectivas raíces de `backend/` y `frontend/` se proveen archivos plantilla `.env.example` que deben ser copiados y adaptados.

1. **Configuración de Backend (`backend/.env`)**:
   Cree el archivo `.env` en la raíz del directorio `backend/`:
   ```bash
   cd backend
   cp .env.example .env
   ```
   *Variables Clave:*
   - `SPRING_DATASOURCE_URL`: URL JDBC de conexión a base de datos (ej. `jdbc:postgresql://localhost:5432/twitterclone`).
   - `SPRING_DATASOURCE_USERNAME`: Usuario de la base de datos (ej. `postgres`).
   - `SPRING_DATASOURCE_PASSWORD`: Contraseña de la base de datos (ej. `password`).

2. **Configuración de Frontend (`frontend/.env`)**:
   Cree el archivo `.env` en la raíz del directorio `frontend/`:
   ```bash
   cd ../frontend
   cp .env.example .env
   ```
   *Variables Clave:*
   - `VITE_API_URL`: Dirección base de la API del Backend (por defecto es `http://localhost:8080`).

---

### Ejecución Completa (Docker)

El despliegue integrado y automatizado de la base de datos, el backend y el frontend (servido bajo Nginx) se realiza simultáneamente mediante contenedores. Desde el **directorio padre `twitterClone/`**, ejecute:

```bash
docker-compose -f backend/docker-compose.yml up --build -d
```

*Puertos de Acceso expuestos:*
- **Aplicación Frontend**: [http://localhost:3000](http://localhost:3000) (servido por Nginx en modo producción).
- **Servicios Backend**: [http://localhost:8080](http://localhost:8080)
- **Base de Datos PostgreSQL**: `localhost:5432`

Para detener los servicios destruyendo los contenedores y redes asociadas:
```bash
docker-compose -f backend/docker-compose.yml down
```

---

### Seed Data

El backend cuenta con una clase de configuración automatizada (`DatabaseSeeder.java`) que actúa durante el arranque. Esta clase ejecuta las siguientes acciones:

1. Comprueba si la base de datos se encuentra vacía (conteo de usuarios igual a 0).
2. Si está vacía, inyecta automáticamente **10 usuarios iniciales**, **20 tweets realistas**, relaciones de seguimiento cruzadas (follows) y me gustas (likes) distribuidos al azar.
3. Asigna avatares visuales dinámicos de Dicebear y presets de gradientes premium para las portadas de los perfiles de todos los usuarios inyectados.

---

### Credenciales de Prueba

Para realizar pruebas de forma inmediata en la interfaz web de producción ([http://localhost:3000](http://localhost:3000)), puede iniciar sesión utilizando las siguientes credenciales pre-cargadas por el sembrador de base de datos:

- **Usuario / Email**: `fit_lucia` o `lucia@example.com`
- **Contraseña**: `password123`

*(Alternativamente, puede iniciar sesión como `maria_coder` / `maria@example.com` o registrar un usuario completamente nuevo desde el formulario de registro de la aplicación).*

---

### Modo Desarrollo (Opcional)

Si desea realizar modificaciones y ejecutar los servicios en modo de desarrollo local sin contenedores completos de aplicación, desde el directorio padre `twitterClone/`:

1. **Levantar únicamente PostgreSQL (Docker)**:
   ```bash
   docker-compose -f backend/docker-compose.yml up postgres -d
   ```
2. **Ejecutar Backend (Spring Boot)**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. **Ejecutar Frontend (Vite + React)**:
   ```bash
   cd ../frontend
   npm install
   npm run dev
   ```
   La aplicación en modo desarrollo estará disponible en [http://localhost:5173](http://localhost:5173).

---

### Suite de Pruebas

El proyecto mantiene estándares rigurosos de calidad de software y cobertura de código.

1. **Ejecución de Pruebas del Backend (JUnit/Mockito)**:
   Desde el directorio `backend/`, ejecute la fase de verificación para correr todos los tests de integración y unitarios generando reportes de cobertura (JaCoCo):
   ```bash
   cd backend
   ./mvnw clean verify
   ```
   *(El reporte de cobertura HTML final se generará en `backend/target/site/jacoco/index.html`)*.

2. **Ejecución de Pruebas del Frontend (Vitest/Testing Library)**:
   Desde el directorio `frontend/`, ejecute las pruebas automatizadas del cliente:
   ```bash
   cd frontend
   npm run test
   ```

---

## 3. Decisiones Técnicas y Arquitectura

### Justificación del Stack

- **Spring Boot (Java 21)**: Seleccionado como la base de servicios debido a su sólida madurez, soporte nativo de tipos primitivos eficientes, facilidad de integración transaccional con JPA/Hibernate y su idoneidad para estructurar APIs empresariales fuertemente tipadas y seguras.
- **React (TypeScript)**: Elegido en el frontend por su modelo declarativo de componentes y su ecosistema reactivo virtualizado, facilitando la creación de interfaces dinámicas altamente fluidas con tipado estático seguro que previene fallos en tiempo de ejecución.

### Modelado del Timeline y Follows

- **Mitigación del Problema de Consultas N+1**: En lugar de consultar entidades Lazy persistentes en memoria (lo cual desencadena ráfagas ineficientes de consultas SQL individuales), las agregaciones de timelines y listados sociales se resuelven directamente a nivel de base de datos. Se utilizan **Proyecciones JPQL directas a DTOs** (`TweetResponse`, `FollowUserResponse`) que consolidan el identificador, autor, contadores de interacciones y el estado de seguimiento del usuario autenticado en una única consulta join optimizada.
- **Indexación B-Tree**: Se han añadido índices B-Tree específicos en la base de datos PostgreSQL sobre las columnas críticas de ordenación y filtrado (`username`, `created_at`), optimizando el rendimiento de las búsquedas case-insensitive y la paginación secuencial de tweets.
- **Hilos de Respuestas**: Se incorporó una relación jerárquica reflexiva en la entidad `Tweet` mediante la columna `parent_id` (para estructurar respuestas) junto a una columna `reply_count` que se incrementa de manera transaccional al guardar cada respuesta, permitiendo renderizar la profundidad de los hilos de manera inmediata y sin cálculos pesados agregados.

### Autenticación

- **Seguridad Stateless (Spring Security + JWT)**: La autenticación se implementa de manera estrictamente stateless mediante un filtro interceptor personalizado (`JwtAuthenticationFilter`) que valida la firma del token firmado digitalmente en cada petición entrante. El estado de la sesión reside en el token que porta el cliente, eliminando la necesidad de persistir sesiones en el servidor backend (garantizando escalabilidad horizontal) y evitando depender de servicios BaaS de terceros.

### Notificaciones SSE (Server-Sent Events)

- **Ecosistema de Notificaciones Concurrente**: Las notificaciones instantáneas (como recibir un me gusta o un nuevo seguidor) se transmiten de forma unidireccional y reactiva mediante Server-Sent Events (SSE). El backend registra las conexiones activas en un `ConcurrentHashMap` de instancias de `SseEmitter` seguras para hilos. El sistema maneja de forma automática los timeouts de reconexión y realiza podas de emisores huérfanos ante errores de red o cierres abruptos de pestañas del navegador.

### Uso de IA (Agentic Coding)

- **Desarrollo Pair-Programming con Antigravity**: El diseño, codificación, refactorización y resolución de bugs del proyecto se llevaron a cabo utilizando el agente de Inteligencia Artificial **Antigravity**. Operando bajo flujos de trabajo estructurados:
  - **Directivas Atómicas**: Cada corrección e implementación se desglosó en hitos pequeños comprobables.
  - **Pruebas Continuas (TDD)**: La validación de nuevas características fue guiada por la suite de pruebas del backend y frontend antes de consolidar integraciones, logrando una cobertura robusta.
  - **Validación Humana Estricta**: Las decisiones críticas de diseño y de infraestructura de base de datos fueron presentadas al desarrollador humano mediante planes de implementación detallados antes de modificar el espacio de trabajo activo, logrando una simbiosis fluida y de alta velocidad entre el agente autónomo y el control del ingeniero humano.
