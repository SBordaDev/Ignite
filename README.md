# 📋 REPORTE TÉCNICO DE ARQUITECTURA Y SEGURIDAD

## 1. INTRODUCCIÓN

### 1.1 Contexto Estratégico

Project Ignite es una plataforma backend de gestión de eventos y solicitudes de inscripción, construida con arquitectura hexagonal (Ports & Adapters) sobre Spring Boot 3.5.14. El proyecto ha evolucionado desde una arquitectura monolítica tradicional hacia un modelo que prioriza **seguridad por diseño (Shift-Left Security)** mediante la integración de análisis automático en todas las fases del ciclo de desarrollo.

### 1.2 Objetivos de Calidad y Seguridad

Este reporte documenta cómo Project Ignite implementa los siguientes objetivos arquitectónicos y de seguridad:

| **Objetivo** | **Métrica** | 
|---|---|
| Arquitectura desacoplada (Hexagonal) | Separación Dominio/Infraestructura | 
| Control dinámico de acceso | Rate Limiting basado en identidad | 
| Detección temprana de secretos | Gitleaks en CI/CD | 
| Análisis de vulnerabilidades (SCA) | OWASP Dependency-Check + NVD | 
| Análisis dinámico de seguridad (DAST) | OWASP ZAP + OpenAPI | 
| Visibilidad de vulnerabilidades | GitHub Code Scanning + SARIF | 
| Cobertura de código | JaCoCo + SonarQube | 

---

## 2. ARQUITECTURA BASE: MODELO HEXAGONAL CON SPRING BOOT

### 2.1 Principios Arquitectónicos Implementados

Project Ignite adopta la **Arquitectura Hexagonal (Ports & Adapters)** para lograr una separación clara entre la lógica de negocio y las detalles técnicos de implementación:

<img width="2816" height="1536" alt="Gemini_Generated_Image_nx5c5snx5c5snx5c" src="https://github.com/user-attachments/assets/2f86be67-14a5-4f3f-b545-e86fa7602e8a" />

### 2.2 Estructura de Directorios y Capas

```
src/main/java/org/bormun
├── dominio/                          # ⭐ NÚCLEO - Sin dependencias externas
│   ├── modelos/                      # Evento, Solicitud, Equipo, Categoria
│   └── excepciones/                  # SolicitudInvalidaException, ErrorDeportista
├── aplicacion/                       # 🔧 ORQUESTACIÓN - Sin detalles de implementación
│   ├── usecase/                      # EnviarSolicitud, ProcesarSolicitud, CrearEvento
│   ├── repositorios/                 # Interfaces (puertos de salida)
│   └── mapper/                       # Transformaciones Dominio ↔ Entidades
├── presentacion/                     # 📡 CONTROLADORES - Entry Points REST
│   ├── controladores/                # SolicitudController, EventoController
│   ├── dto/                          # DTOs de request/response
│   └── GlobalExceptionHandler        # Manejo centralizado de excepciones
└── infraestructura/                  # 🔌 IMPLEMENTACIÓN - Detalles técnicos
    ├── configs/                      # SecurityConfig, SwaggerConfig, DataInitializer
    ├── entidades/                    # Mapeo JPA a base de datos
    ├── repositorio/                  # Implementaciones de interfaces (JPARepository)
    └── seguridad/                    # TokenService, SecurityFilter
```

### 2.3 Patrón de Flujo en la Capa de Aplicación

**Ejemplo: Envío de Solicitud de Inscripción**



**Ventaja Clave:** La lógica de negocio (dominio) está completamente aislada de:
- Frameworks (Spring, JPA)
- Protocolos de comunicación (HTTP)
- Mecanismos de persistencia (Base de datos)
- Detalles de seguridad específicos

Esto permite **testar la lógica de negocio sin necesidad de contenedores o bases de datos**.

---


## 3. PIPELINE DE DEVSECOPS: AUTOMATIZACIÓN DE SEGURIDAD

### 3.1 Flujo General del Pipeline CI/CD

```
┌───────────────────────────────────────────────────────────────────────────┐
│                    GitHub Actions: Ignite DevSecOps CI/CD                 │
│                   (Ejecución Automática en cada Push/PR)                  │
└───────────────────────────────────────────────────────────────────────────┘

   ↓ [EVENTO: Push a main/master]

┌─ Paso 1: CONTROL DE VERSIONES ────────────────────────────────────┐
│ • Checkout del repositorio                                        │
│ • Fetch-depth=0 (Historial completo para SonarQube)              │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 1.5: ANÁLISIS ESTÁTICO - DETECCIÓN DE SECRETOS ────────────┐
│ Herramienta:    Gitleaks                                          │
│ Objetivo:       Escanear commits en busca de credenciales         │
│ Alcance:        Keys, tokens, passwords, API keys                │
│ Salida:         Reporte JSON (integrado en GitHub Annotations)   │
│ Falla Pipeline: ✅ SÍ (Si detecta secreto)                       │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 2: PREPARACIÓN DEL ENTORNO ────────────────────────────────┐
│ • Configurar Java 21 (Temurin)                                    │
│ • Habilitar caché Maven (Acelera ~30% del build)                │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 3: COMPILACIÓN + PRUEBAS + COBERTURA ──────────────────────┐
│ Comando:  mvn clean test                                          │
│ Plugins:                                                          │
│  • JUnit 5 (Ejecución de pruebas unitarias)                      │
│  • JaCoCo (Instrumentación de bytecode → cobertura)              │
│  • Allure (Generación de reportes HTML interactivos)             │
│ Métricas:                                                         │
│  • Cobertura de líneas                                           │
│  • Cobertura de ramas                                            │
│  • Complejidad ciclomática                                       │
│ Salida: target/site/jacoco/jacoco.xml (SARIF-compatible)         │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 4: ANÁLISIS DE DEPENDENCIAS (SCA) ─────────────────────────┐
│ Herramienta:    OWASP Dependency-Check                            │
│ Objetivo:       Escanear vulnerabilidades conocidas en deps       │
│ Base de Datos:  NVD (National Vulnerability Database)            │
│ API Key:        Inyectada vía secrets.NVD_API_KEY (GitHub)        │
│ Alcance:        Java, Maven, todas las transitividades           │
│ Severidades:    CRITICAL, HIGH                                    │
│ Salida:         target/dependency-check-report.html              │
│ Falla Pipeline: ✅ SÍ (Si detecta HIGH/CRITICAL)                │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 5: EMPAQUETAMIENTO ────────────────────────────────────────┐
│ mvn package -DskipTests                                           │
│ Genera:  target/Ignite-1.0-SNAPSHOT.jar                           │
│ Tamaño:  ~80 MB (Incluye todas las dependencias)                 │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 6: ARTEFACTOS DE SEGURIDAD ────────────────────────────────┐
│ GitHub Actions: actions/upload-artifact                           │
│ Almacena:                                                         │
│  • Ignite-1.0-SNAPSHOT.jar                                        │
│  • dependency-check-report.html                                   │
│  • jacoco.xml                                                     │
│ Retención: 90 días (Configurable)                                │
│ Descarga: Disponible en job summary                              │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 7: AUTENTICACIÓN EN GITHUB CONTAINER REGISTRY ──────────────┐
│ docker login ghcr.io                                              │
│ Credenciales: ${{ github.actor }} + ${{ secrets.GITHUB_TOKEN }}  │
│ Resultado:    ~/.docker/config.json (Autenticado)                │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 8: CONSTRUCCIÓN Y ALMACENAMIENTO DE IMAGEN DOCKER ─────────┐
│ Dockerfile:                                                       │
│  FROM eclipse-temurin:21-jre-alpine  (imagen base ligera)        │
│  COPY target/Ignite-*.jar app.jar                                │
│  ENTRYPOINT ["java", "-jar", "app.jar"]                          │
│                                                                   │
│ Construcción:                                                     │
│  docker build -t ghcr.io/sbordadev/ignite-backend:latest .       │
│                                                                   │
│ Resultado: Imagen Docker de ~150 MB en local                     │
│ Nota: NO se hace push en esta etapa (espera análisis DAST)       │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 8.5: ANÁLISIS DE VULNERABILIDADES DE CONTENEDOR ───────────┐
│ Herramienta:    Aqua Security Trivy                               │
│ Objetivo:       Escanear imagen Docker en busca de CVEs          │
│ Base de Datos:  Múltiples (GHSA, NVD, Trivy DB)                 │
│ Alcance:        SO (OS packages) + Librerías (library deps)      │
│ Severidades:    CRITICAL, HIGH (Detiene pipeline si aplica)     │
│ Salida:         Tabla de vulnerabilidades en logs               │
│ Falla Pipeline: ✅ SÍ (Si aplica exit-code=1)                   │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 9: INICIO DE CONTENEDOR (Pruebas Dinámicas) ───────────────┐
│ docker run -d -p 8080:8080 ghcr.io/sbordadev/ignite-backend      │
│ Espera: 15 segundos (Spring Boot startup)                        │
│ Verificación: curl http://localhost:8080/actuator/health         │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 10: ANÁLISIS DINÁMICO (DAST) ──────────────────────────────┐
│ Herramienta:    OWASP ZAP (API Scan)                              │
│ Target:         http://localhost:8080/v3/api-docs (OpenAPI)      │
│ Formato:        OpenAPI 3.0 (Swagger)                            │
│ Objetivo:       Exploración automática basada en especificación  │
│ Vulnerabilidades Detectadas:                                     │
│  • Injection (SQL, NoSQL, OS)                                     │
│  • Broken Authentication                                         │
│  • Sensitive Data Exposure                                       │
│  • XXE (XML External Entity)                                     │
│  • Missing Security Headers                                      │
│  • Misconfiguration                                              │
│ Salida:         results.sarif (GitHub Code Scanning format)      │
│ Falla Pipeline: ❌ NO (fail_action: false → permite continuar)   │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 11: DIAGNÓSTICO (Modo always - Ejecuta incluso si falla) ──┐
│ docker logs ignite-dast                                           │
│ Captura:  Logs de Spring Boot (errores, excepciones, traces)    │
│ Propósito: Debugging si DAST o startup falló                    │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 12: LIMPIEZA (Modo always) ────────────────────────────────┐
│ docker rm -f ignite-dast                                          │
│ Objetivo:  Eliminar contenedor de prueba (no persiste)           │
│ Efecto:    Evitar contaminación de pruebas futuras               │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 13: INTEGRACIÓN CON GITHUB CODE SCANNING ──────────────────┐
│ GitHub Action:  github/codeql-action/upload-sarif                │
│ Archivo:        results.sarif (Generado por OWASP ZAP)           │
│ Categoría:      DAST_OWASP_ZAP                                   │
│ Destinación:    GitHub Security Tab → Code Scanning Alerts       │
│ Visualización:  Pull Requests + Security Overview                │
│ Alcance:        Permite tracking y triage de vulnerabilidades   │
│ Ejecuta:        Solo si jobs anteriores completan (always)       │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─ Paso 14: PUBLICACIÓN (Solo si todo pasó) ───────────────────────┐
│ Condición:      Pruebas dinámicas superadas                       │
│ Acción:         docker push ghcr.io/sbordadev/ignite-backend      │
│ Destino:        GitHub Container Registry (GHCR)                 │
│ Tag:            latest                                            │
│ Disponibilidad: Publicada para pull en otros servicios           │
│ Control:        Solo versiones probadas llegan a registry         │
└───────────────────────────────────────────────────────────────────┘

   ↓

┌─────────────────────────────────────────────────────────────────────────┐
│ ✅ PIPELINE COMPLETADO - Artefacto seguro en GHCR                      │
│ 📊 Todos los reportes consolidados en GitHub Code Scanning             │
│ 🔍 Vulnerabilidades rastreables y asignables                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Detalles Críticos de cada Herramienta

#### **3.2.1 Gitleaks (Escaneo de Secretos)**

```yaml
Ejecución: gitleaks/gitleaks-action@v2
Objetivo:  Detectar credenciales hardcodeadas antes de que lleguen a main
Cobertura:
  ✅ API Keys (AWS, Azure, GCP)
  ✅ Private Keys (RSA, DSA, EC)
  ✅ Tokens (OAuth, JWT, Bearer)
  ✅ Database Passwords
  ✅ Stripe/Twilio/SendGrid Keys
  ✅ GitHub Personal Access Tokens

Ejemplo Detectado:
  ❌ aws_access_key_id = "AKIAIOSFODNN7EXAMPLE"
  ❌ password: "SuperSecretPassword123"
  ❌ api_key: "sk-1234567890abcdef"

Falla Pipeline: ✅ SÍ
```

#### **3.2.2 OWASP Dependency-Check (SCA - Software Composition Analysis)**

```yaml
Herramienta: Maven Plugin (org.owasp:dependency-check-maven)
Versión:     10.0.3
Base de Datos: NVD (National Vulnerability Database)
Actualización: Real-time via NVD API (8 segundos de delay configurado)

Proceso:
  1. Escanea pom.xml (dependencias directas)
  2. Resuelve transitividades (dependencias de dependencias)
  3. Extrae CPE (Common Platform Enumeration) de cada artefacto
  4. Consulta NVD API usando CPE
  5. Genera reporte con CVEs conocidos

Ejemplo Output:
  Dependency: spring-security-core (6.5.9)
    ├─ CVE-2024-XXXXX: Severity HIGH
    ├─ CVSS Score: 8.2
    ├─ Description: "Authentication bypass in form login"
    └─ Recommendation: "Upgrade to 6.5.10+"

API Key Usage:
  • Almacenada en GitHub Secrets
  • Inyectada vía env.NVD_API_KEY
  • Rate Limit: 10,000 requests/día
  • Delay: 8 segundos entre llamadas (para no saturar NVD)

Configuración Maven:
  <plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <configuration>
      <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
      <nvdApiDelay>8000</nvdApiDelay>
      <severity>CRITICAL,HIGH</severity>
    </configuration>
  </plugin>

Falla Pipeline: ✅ SÍ (Si HIGH/CRITICAL detectado)
```

#### **3.2.3 OWASP ZAP - Análisis Dinámico (DAST)**

```yaml
Herramienta:    Zaproxy (OWASP ZAP)
Tipo:           DAST (Dynamic Application Security Testing)
Acción:         zaproxy/action-api-scan@v0.10.0
Target:         http://localhost:8080/v3/api-docs
Format:         OpenAPI 3.0 (Swagger)

¿POR QUÉ OPENAPI/SWAGGER?
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Especificación Estructurada
   - ZAP entiende automáticamente TODOS los endpoints
   - No necesita enumeración manual de rutas
   - Reduce falsos negativos (endpoints omitidos)

2. Contrato Machine-Readable
   - Tipos de datos conocidos (string, int, object)
   - Parametrización automática (path, query, body)
   - Validación de respuestas esperadas vs reales

3. Inyección Inteligente
   - ZAP genera payloads contextualizados
   - Para string: payloads SQL, XXE, XSS
   - Para numbers: overflow, negativos, ceros

Ejemplo de Contrato OpenAPI:
{
  "openapi": "3.0.0",
  "paths": {
    "/api/eventos/{eventoId}/solicitudes": {
      "post": {
        "parameters": [
          {
            "name": "eventoId",
            "in": "path",
            "required": true,
            "schema": {"type": "integer"}
          }
        ],
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "nombreOrganizacion": {"type": "string"},
                  "idCategoria": {"type": "integer"}
                }
              }
            }
          }
        }
      }
    }
  }
}

ZAP Ejecuta Automáticamente:
  ✅ POST /api/eventos/1/solicitudes
     ├─ Payload: {"nombreOrganizacion": "' OR '1'='1"}  (SQL Injection)
     ├─ Payload: {"nombreOrganizacion": "<?xml..."}    (XXE)
     ├─ Payload: {"nombreOrganizacion": "<script>..."}  (XSS)
     └─ Payload: {"idCategoria": 999999999}             (Integer Overflow)

  ✅ PATCH /api/solicitudes/{solicitudId}/procesar
  ✅ GET /api/eventos/{id}
  ✅ Todos los endpoints sin excepción

Vulnerabilidades Detectadas:
  • Missing Security Headers (X-Frame-Options, CSP)
  • Weak Authentication (JWT sin firma)
  • Insecure Deserialization
  • Broken Access Control
  • Sensitive Data Exposure

Salida: results.sarif (Static Analysis Results Format)
Falla Pipeline: ❌ NO (fail_action: false, pero se reporta)
```

#### **3.2.4 JaCoCo (Cobertura de Código)**

```yaml
Plugin Maven:   jacoco-maven-plugin (0.8.11)
Ejecución:      Automática en fase test

Flujo:
  1. prepare-agent: Inyecta agente JaCoCo en JVM
     └─ Instrumenta bytecode para tracking de líneas ejecutadas
  
  2. test: Maven ejecuta @Test (JUnit 5)
     └─ Todos los tests se ejecutan con agente JaCoCo activo
  
  3. report: Genera reporte XML + HTML
     └─ target/site/jacoco/jacoco.xml
     └─ target/site/jacoco/index.html

Exclusiones Configuradas (No se cuentan):
  ❌ **/*Config.java (Configuraciones Spring)
  ❌ **/infraestructura/configs/**
  ❌ **/Swagger*.java (Generado automáticamente)
  ❌ **/Security*.java (Configuración de seguridad)
  ❌ **/DataInitializer.java (Carga inicial de datos)

Razón de exclusiones:
  • Son "boilerplate" inyectado por Spring
  • Difíciles de testear sin contexto de aplicación
  • No contienen lógica de negocio

Métricas Capturadas:
  • Line Coverage: % de líneas ejecutadas
  • Branch Coverage: % de bifurcaciones (if/else)
  • Complexity: Promedio de complejidad ciclomática

Integración SonarQube:
  sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
  └─ SonarQube consume jacoco.xml para análisis de cobertura
```

#### **3.2.5 Aqua Security Trivy (Escaneo de Contenedores)**

```yaml
Herramienta:    aquasecurity/trivy-action@master
Target:         Imagen Docker: ghcr.io/sbordadev/ignite-backend:latest
Bases de Datos:
  • GHSA (GitHub Security Advisory)
  • NVD (National Vulnerability Database)
  • Trivy DB (Vulnerabilidades conocidas)

Alcance:
  ✅ OS Packages (Alpine Linux base image)
  ✅ Java Libraries (spring-boot, resilience4j, etc.)
  ✅ System Libraries (OpenSSL, libc, etc.)
  ✅ Configuration Mismatches

Ejemplo Escaneo:
  
  Image Digest: sha256:abcd1234...
  
  Vulnerabilities Detected:
  
  OS (Alpine Linux)
    NAME                 SEVERITY  CVE-ID          VERSION
    ────────────────────────────────────────────────────
    openssl              CRITICAL  CVE-2024-0001   3.1.0
    curl                 HIGH      CVE-2024-0002   8.0.1
  
  Java Dependencies
    spring-security-core HIGH      CVE-2024-0003   6.5.9
    jackson-databind     MEDIUM    CVE-2024-0004   2.15.0

Falla Pipeline: ✅ SÍ (exit-code=1 si CRITICAL/HIGH)
```

---

## 4. DETALLES DE IMPLEMENTACIÓN DE SEGURIDAD

### 4.1 Arquitectura de Seguridad en Capas

```
┌─────────────────────────────────────────────────────────────┐
│                     CAPA 1: PERIMETRAL                      │
│ • GitHub Actions (Solo ejecuta en repositorio verificado)  │
│ • HTTPS requerido para GitHub APIs                          │
│ • Secrets almacenados encriptados (GitHub Vault)           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  CAPA 2: COMPILACIÓN                        │
│ • Gitleaks: Bloquea secretos antes de build                │
│ • Dependency-Check: Bloquea dependencias vulnerables        │
│ • SonarQube: Analiza código fuente en busca de fallas      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│               CAPA 3: EMPAQUETAMIENTO                       │
│ • Imagen Docker con base Alpine (minimales CVEs)           │
│ • Trivy: Escanea imagen antes de publicación                │
│ • Firma de imagen (Opcional con Cosign)                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  CAPA 4: RUNTIME                            │
│ • Spring Security: Autenticación JWT + autorización        │
│ • Rate Limiting Dinámico: Protección contra DoS            │
│ • Validación de entrada: Jakarta Bean Validation           │
│ • Exception Handling: Nunca expone stack traces             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  CAPA 5: OBSERVABILIDAD                     │
│ • Prometheus: Métricas de seguridad                        │
│ • ELK Stack: Logs centralizados en Elasticsearch           │
│ • GitHub Code Scanning: Visibilidad de vulnerabilidades    │
│ • Kibana: Dashboards de seguridad en tiempo real           │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Flujo de Autenticación y Autorización

```java
┌──────────────────────────────┐
│  Cliente HTTP (con JWT)      │
└────────────┬─────────────────┘
             │
             ↓ Authorization: Bearer eyJhbGc...
┌──────────────────────────────┐
│  SecurityFilter (Custom)     │
│  • Extrae token del header   │
│  • Valida firma JWT (HMAC256)│
│  • Decodifica payload        │
└────────────┬─────────────────┘
             │
             ↓ Usuario autenticado
┌──────────────────────────────┐
│  SecurityContextHolder       │
│  • Almacena Authentication   │
│  • Accesible en toda request │
└────────────┬─────────────────┘
             │
             ↓ @PostMapping("/api/eventos/{id}/solicitudes")
┌──────────────────────────────┐
│  SolicitudController         │
│  • SecurityContextHolder     │
│    .getContext()             │
│    .getAuthentication()      │
│    .getName()                │
│  = "juan@example.com"        │
└────────────┬─────────────────┘
             │
             ↓ Crear Rate Limiter por usuario
┌──────────────────────────────┐
│  Rate Limiting Dinámico      │
│  • Nombre: solicitudes_juan..│
│  • Cubeta: 5 requests/60s    │
│  • Verificación: SUCCESS/429 │
└──────────────────────────────┘
```

### 4.3 Protección Contra Ataques Comunes

| **Ataque OWASP Top 10** | **Mitigación en Ignite** |
|---|---|
| **A1: Broken Access Control** | SecurityConfig + @hasRole/@hasAnyRole + Dynamic Rate Limiting |
| **A2: Cryptographic Failures** | Spring Security (criptografía delegada a BCrypt), HTTPS en producción |
| **A3: Injection** | Prepared Statements (JPA), Input Validation (Jakarta Bean Validation) |
| **A4: Insecure Design** | Hexagonal Architecture (lógica de negocio aislada), Separation of Concerns |
| **A5: Security Misconfiguration** | SecurityConfig centralizado, Swagger sin exposer endpoints admin |
| **A6: Vulnerable Components** | OWASP Dependency-Check + NVD en cada build |
| **A7: Authentication Failures** | JWT con expiración 1 hora, TokenService con validación |
| **A8: Data Integrity Failures** | @Transactional en Use Cases, Rollback automático en excepciones |
| **A9: Logging & Monitoring** | Logstash + Elasticsearch, Prometheus metrics, GitHub Code Scanning |
| **A10: SSRF** | No URLs dinámicas desde entrada de usuario |

---

## 5. INFRAESTRUCTURA Y ORQUESTACIÓN LOCAL

### 5.1 Docker Compose: Stack Completo de Observabilidad

```yaml
services:
  
  ignite-app:
    # Aplicación Principal
    build: .  # Dockerfile local
    ports: ["8080:8080"]
    depends_on: [prometheus, elasticsearch]
    networks: [ignite-network]
  
  prometheus:
    # Scraping de Métricas (Micrometer)
    # Métrica: ratelimiter_solicitudes_*.denied_requests
    # Métrica: http_server_requests_seconds_bucket
    image: prom/prometheus:latest
    ports: ["9090:9090"]
  
  grafana:
    # Visualización de Métricas
    # Dashboard: Rate Limiting por usuario
    # Dashboard: Response times por endpoint
    image: grafana/grafana:latest
    ports: ["3000:3000"]
    depends_on: [prometheus]
  
  elasticsearch:
    # Almacenamiento de Logs (Logstash → ES)
    image: docker.elastic.co/elasticsearch/elasticsearch:8.13.0
    ports: ["9200:9200"]
    environment:
      - xpack.security.enabled=false  # Local only
  
  logstash:
    # Pipeline de Logs
    # Input: TCP puerto 50000
    # Output: Elasticsearch
    image: docker.elastic.co/logstash/logstash:8.13.0
    volumes:
      - ./logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on: [elasticsearch]
  
  kibana:
    # Visualización de Logs
    # Query: level:ERROR
    # Query: usuario:"juan@example.com"
    image: docker.elastic.co/kibana/kibana:8.13.0
    ports: ["5601:5601"]
    depends_on: [elasticsearch]
  
  sonarqube:
    # Análisis de Código
    # Métricas: Cobertura, Bugs, Code Smells
    image: sonarqube:lts-community
    ports: ["9000:9000"]

networks:
  ignite-network:
    driver: bridge
```

---

## 6 Ejemplo Real: Multiples vulnerabilidades detectadas

<img width="1082" height="891" alt="image" src="https://github.com/user-attachments/assets/461e91d5-a976-443d-ac7d-8f60d25d5401" />

```
Report Summary

┌─────────────────────────────────────────────────────────┬────────┬─────────────────┬─────────┐
│                         Target                          │  Type  │ Vulnerabilities │ Secrets │
├─────────────────────────────────────────────────────────┼────────┼─────────────────┼─────────┤
│ ghcr.io/sbordadev/ignite-backend:latest (alpine 3.23.4) │ alpine │        0        │    -    │
├─────────────────────────────────────────────────────────┼────────┼─────────────────┼─────────┤
│ app/app.jar                                             │  jar   │       29        │    -    │
└─────────────────────────────────────────────────────────┴────────┴─────────────────┴─────────┘
Legend:
- '-': Not scanned
- '0': Clean (no security findings detected)


For OSS Maintainers: VEX Notice
--------------------------------
If you're an OSS maintainer and Trivy has detected vulnerabilities in your project that you believe are not actually exploitable, consider issuing a VEX (Vulnerability Exploitability eXchange) statement.
VEX allows you to communicate the actual status of vulnerabilities in your project, improving security transparency and reducing false positives for your users.
Learn more and start using VEX: https://trivy.dev/docs/v0.70/guide/supply-chain/vex/repo#publishing-vex-documents

To disable this notice, set the TRIVY_DISABLE_VEX_NOTICE environment variable.


Java (jar)
==========
Total: 29 (HIGH: 23, CRITICAL: 6)

┌─────────────────────────────────────────────────────────────┬────────────────┬──────────┬────────┬───────────────────┬──────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────────┐
│                           Library                           │ Vulnerability  │ Severity │ Status │ Installed Version │                    Fixed Version                     │                            Title                             │
├─────────────────────────────────────────────────────────────┼────────────────┼──────────┼────────┼───────────────────┼──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.apache.tomcat.embed:tomcat-embed-core (app.jar)         │ CVE-2025-24813 │ CRITICAL │ fixed  │ 10.1.19           │ 11.0.3, 10.1.35, 9.0.99                              │ tomcat: Potential RCE and/or information disclosure and/or   │
│                                                             │                │          │        │                   │                                                      │ information corruption with partial PUT...                   │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-24813                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-41293 │          │        │                   │ 9.0.118, 10.1.55, 11.0.22                            │ Improper Input Validation vulnerability in Apache Tomcat.    │
│                                                             │                │          │        │                   │                                                      │ This issue ......                                            │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-41293                   │
│                                                             ├────────────────┤          │        │                   │                                                      ├──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-43512 │          │        │                   │                                                      │ DEPRECATED: Authentication Bypass Issues vulnerability in    │
│                                                             │                │          │        │                   │                                                      │ digest authe ...                                             │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-43512                   │
│                                                             ├────────────────┤          │        │                   │                                                      ├──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-43515 │          │        │                   │                                                      │ Improper Authorization vulnerability when multiple method    │
│                                                             │                │          │        │                   │                                                      │ constraints ...                                              │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-43515                   │
│                                                             ├────────────────┼──────────┤        │                   ├──────────────────────────────────────────────────────┼────────────────────────────────────────────────────���─────────┤
│                                                             │ CVE-2024-34750 │ HIGH     │        │                   │ 11.0.0-M21, 10.1.25, 9.0.90                          │ tomcat: Improper Handling of Exceptional Conditions          │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-34750                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2024-50379 │          │        │                   │ 11.0.2, 10.1.34, 9.0.98                              │ tomcat: RCE due to TOCTOU issue in JSP compilation           │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-50379                   │
│                                                             ├────────────────┤          │        │                   │                                                      ├──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2024-56337 │          │        │                   │                                                      │ tomcat: Incomplete fix for CVE-2024-50379 - RCE due to       │
│                                                             │                │          │        │                   │                                                      │ TOCTOU issue in...                                           │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-56337                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2025-48988 │          │        │                   │ 11.0.8, 10.1.42, 9.0.106                             │ tomcat: Apache Tomcat DoS in multipart upload                │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-48988                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2025-48989 │          │        │                   │ 11.0.10, 10.1.44, 9.0.108                            │ tomcat: http/2 "MadeYouReset" DoS attack through HTTP/2      │
│                                                             │                │          │        │                   │                                                      │ control frames                                               │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-48989                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2025-52520 │          │        │                   │ 11.0.9, 10.1.43, 9.0.107                             │ tomcat: Apache Tomcat denial of service                      │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-52520                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2025-53506 │          │        │                   │ 9.0.107, 10.1.43, 11.0.9                             │ tomcat: Apache Tomcat denial of service                      │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-53506                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2025-55752 │          │        │                   │ 11.0.11, 10.1.45, 9.0.109                            │ tomcat: org.apache.tomcat/tomcat-catalina: Apache Tomcat:    │
│                                                             │                │          │        │                   │                                                      │ Directory traversal via rewrite with possible RCE            │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-55752                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-24734 │          │        │                   │ 11.0.18, 10.1.52, 9.0.115                            │ tomcat: Apache Tomcat: Certificate revocation bypass due to  │
│                                                             │                │          │        │                   │                                                      │ improper OCSP response validation...                         │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-24734                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-24880 │          │        │                   │ 9.0.116, 10.1.52, 11.0.20                            │ Apache Tomcat: Apache Tomcat: HTTP Request/Response          │
│                                                             │                │          │        │                   │                                                      │ Smuggling via invalid chunk extension                        │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-24880                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-34483 │          │        │                   │ 9.0.116, 10.1.54, 11.0.21                            │ Apache Tomcat: Apache Tomcat: Information disclosure due to  │
│                                                             │                │          │        │                   │                                                      │ improper encoding in JsonAccessLogValve...                   │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-34483                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-34487 │          │        │                   │ 9.0.117, 10.1.54, 11.0.21                            │ Apache Tomcat: Apache Tomcat: Information disclosure via     │
│                                                             │                │          │        │                   │                                                      │ sensitive data in log files...                               │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-34487                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-41284 │          │        │                   │ 9.0.118, 10.1.55, 11.0.22                            │ Allocation of Resources Without Limits or Throttling         │
│                                                             │                │          │        │                   │                                                      │ vulnerability in ...                                         │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-41284                   │
│                                                             ├────────────────┤          │        │                   │                                                      ├──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-42498 │          │        │                   │                                                      │ Exposure of HTTP Authentication Header to unexpected hosts   │
│                                                             │                │          │        │                   │                                                      │ during WebS ...                                              │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-42498                   │
│                                                             ├────────────────┤          │        │                   │                                                      ├──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-43513 │          │        │                   │                                                      │ Improper Handling of Case Sensitivity vulnerability in       │
│                                                             │                │          │        │                   │                                                      │ LockOutRealm in ...                                          │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-43513                   │
├─────────────────────────────────────────────────────────────┼────────────────┤          │        ├───────────────────┼──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework.boot:spring-boot (app.jar)              │ CVE-2025-22235 │          │        │ 3.2.3             │ 3.3.11, 3.4.5                                        │ org.springframework.boot/spring-boot: Spring Boot            │
│                                                             │                │          │        │                   │                                                      │ EndpointRequest.to() creates wrong matcher if actuator       │
│                                                             │                │          │        │                   │                                                      │ endpoint is not...                                           │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-22235                   │
├─────────────────────────────────────────────────────────────┼────────────────┤          │        ├───────────────────┼──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework.security:spring-security-core (app.jar) │ CVE-2024-22257 │          │        │ 6.2.2             │ 5.7.12, 5.8.11, 6.1.8, 6.2.3                         │ spring-security: Broken Access Control With Direct Use of    │
│                                                             │                │          │        │                   │                                                      │ AuthenticatedVoter                                           │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-22257                   │
├─────────────────────────────────────────────────────────────┼────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework.security:spring-security-crypto         │ CVE-2025-22228 │          │        │                   │ 6.3.8, 6.4.4, 6.2.10, 6.1.14, 6.0.16, 5.8.18, 5.7.16 │ spring-security-core: Spring Security BCryptPasswordEncoder  │
│ (app.jar)                                                   │                │          │        │                   │                                                      │ does not enforce maximum password length                     │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-22228                   │
├─────────────────────────────────────────────────────────────┼────────────────┼──────────┤        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework.security:spring-security-web (app.jar)  │ CVE-2024-38821 │ CRITICAL │        │                   │ 5.7.13, 5.8.15, 6.2.7, 6.0.13, 6.1.11, 6.3.4         │ Spring-WebFlux: Authorization Bypass of Static Resources in  │
│                                                             │                │          │        │                   │                                                      │ WebFlux Applications                                         │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-38821                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2026-22732 │          │        │                   │ 6.5.9, 7.0.4                                         │ Spring Security: Spring Security: Security policy bypass and │
│                                                             │                │          │        │                   │                                                      │ information disclosure due to...                             │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2026-22732                   │
├─────────────────────────────────────────────────────────────┼────────────────┼──────────┤        ├───────────────────┼──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework:spring-core (app.jar)                   │ CVE-2025-41249 │ HIGH     │        │ 6.1.4             │ 6.2.11                                               │ org.springframework/spring-core: Spring Framework Annotation │
│                                                             │                │          │        │                   │                                                      │ Detection Vulnerability                                      │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2025-41249                   │
├─────────────────────────────────────────────────────────────┼────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework:spring-web (app.jar)                    │ CVE-2024-22259 │          │        │                   │ 6.1.5, 6.0.18, 5.3.33                                │ springframework: URL Parsing with Host Validation            │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-22259                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2024-22262 │          │        │                   │ 5.3.34, 6.0.19, 6.1.6                                │ springframework: URL Parsing with Host Validation            │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-22262                   │
├─────────────────────────────────────────────────────────────┼────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ org.springframework:spring-webmvc (app.jar)                 │ CVE-2024-38816 │          │        │                   │ 6.1.13                                               │ spring-webmvc: Path Traversal Vulnerability in Spring        │
│                                                             │                │          │        │                   │                                                      │ Applications Using RouterFunctions and FileSystemResource    │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-38816                   │
│                                                             ├────────────────┤          │        │                   ├──────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│                                                             │ CVE-2024-38819 │          │        │                   │ 6.1.14                                               │ org.springframework:spring-webmvc: Path traversal            │
│                                                             │                │          │        │                   │                                                      │ vulnerability in functional web frameworks                   │
│                                                             │                │          │        │                   │                                                      │ https://avd.aquasec.com/nvd/cve-2024-38819                   │
└─────────────────────────────────────────────────────────────┴────────────────┴──────────┴────────┴───────────────────┴──────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────────┘
```

---


## 7. MATRIZ DE MADUREZ DE SEGURIDAD

```
NIVEL 1: Inicial (Sin automatización)
  No hay CI/CD automatizado
  Secretos en código fuente
  Dependencias sin validar
  Sin pruebas de seguridad dinámicas

NIVEL 2: Repetible (Algunas herramientas)
  Gitleaks implementado
  Dependency-Check en build local
  Rate limiting básico
  Sin análisis dinámico
  Sin consolidación de alertas

NIVEL 3: Definido (Pipeline completo) ← IGNITE ESTÁ AQUÍ
  Gitleaks en CI/CD
  OWASP Dependency-Check + NVD
  Trivy de imágenes Docker
  OWASP ZAP (DAST)
  SARIF + GitHub Code Scanning
  Rate limiting dinámico
  Hexagonal architecture
  Manual triage de alertas

NIVEL 4: Gestionado (Orchestración avanzada)
  Todos los Level 3
  SIEM centralizado (Elasticsearch)
  Automatic remediation (bot fix-secrets)
  Threat intelligence integrada
  Machine learning para falsos positivos

NIVEL 5: Optimizado (DevOps completo)
  Todos los Level 4
  Policy as Code (Terraform)
  Security testing en staging
  Canary deployments con métricas de seguridad
  Incident response automatizado
```

**Project Ignite:** Nivel 3 (Definido) con tendencia a Nivel 4.

---

## 8. CONCLUSIÓN: IMPACTO DEL SHIFT-LEFT SECURITY

### 8.1 Resumen Ejecutivo

Project Ignite implementa una **postura DevSecOps madura** mediante:

1. **Arquitectura Hexagonal Robusta**
   - Separación clara de responsabilidades
   - Lógica de negocio testeable sin dependencias técnicas
   - Facilita auditoría y mantenimiento

2. **Control Dinámico de Acceso (Rate Limiting por Usuario)**
   - Mitigación efectiva contra DoS
   - Granularidad por identidad
   - Integración con Spring Security y Resilience4j

3. **Pipeline de DevSecOps Automatizado**
   - 5 capas de análisis (SAST, SCA, Container, DAST, Logging)
   - Integración nativa con GitHub (Code Scanning)
   - Ejecución automática sin intervención manual

4. **Visibilidad Centralizada**
   - GitHub Code Scanning (Pull Requests + Security Tab)
   - SARIF como formato estándar
   - Trazabilidad completa de vulnerabilidades



### 8.2 Beneficios Cualitativos

| **Stakeholder** | **Beneficio** |
|---|---|
| **CISO/Security** | Control centralizado, compliance tracking, automated reporting |
| **Dev Team** | Feedback inmediato, fixing en contexto, no sorpresas en prod |
| **DevOps** | Pipeline confiable, artefactos seguros, automatización 100% |
| **Business** | Menos incidents, mejor reputación, cumplimiento normativo |

### 8.3 Conclusión Final

**Project Ignite ha logrado implementar una arquitectura y postura de seguridad que no solo detecta vulnerabilidades tempranamente, sino que las previene mediante diseño.**

La combinación de:
- **Arquitectura Hexagonal** (Separation of Concerns)
- **Rate Limiting Dinámico** (Protection by Design)
- **Pipeline DevSecOps Automatizado** (Shift-Left Security)
- **Visibilidad Centralizada** (GitHub Code Scanning)

...permite que el equipo de desarrollo trabajar **con confianza**, sabiendo que las vulnerabilidades son detectadas en minutos, no en meses después del deployment.

**El resultado:** Una aplicación que es **segura por diseño, verificada por automatización, observable en tiempo real.**

---

## ANEXO A: Referencias Técnicas

### Herramientas Utilizadas
- **Spring Boot:** 3.5.14 (Framework principal)
- **Spring Security:** 6.5.9 (Autenticación/Autorización)
- **Resilience4j:** 2.2.0 (Rate Limiting)
- **Gitleaks:** v2 (Detección de secretos)
- **OWASP Dependency-Check:** 10.0.3 (SCA)
- **Trivy:** Latest (Container scanning)
- **OWASP ZAP:** 0.10.0 (DAST)
- **JaCoCo:** 0.8.11 (Code coverage)
- **Docker:** Latest (Containerización)
- **ELK Stack:** 8.13.0 (Observabilidad)

### Estándares Implementados
- **OpenAPI:** 3.0 (API specification)
- **OWASP Top 10:** Mitigaciones implementadas
- **SARIF:** 2.1.0 (Reporting estándar)
- **NIST Cybersecurity Framework:** Adaptado
- **JWT:** RFC 7519 (Token format)
