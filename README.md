# EMIR REFIT Batch

Servicio Spring Boot que ejecuta los jobs de reporting regulatorio EMIR REFIT
(T-32a, T-32b, T-32c, T-33a) contra Impala y escribe el resultado en CSV.
Los jobs se lanzan bajo demanda vía API REST o de forma programada (scheduler).

## Arranque

    mvn spring-boot:run

La app arranca en http://localhost:8080 y se queda escuchando. El auto-arranque
de jobs está deshabilitado (spring.batch.job.enabled: false); los jobs se lanzan
vía API o cron.

## API REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET  | /api/jobs | Lista los jobs disponibles |
| POST | /api/jobs/{jobName}/run | Lanza un job con parámetros |
| GET  | /api/jobs/executions/{executionId} | Estado de una ejecución |

### Swagger UI

- Swagger UI: http://localhost:8080/swagger-ui.html
- Spec JSON (generado en runtime): http://localhost:8080/v3/api-docs
- Spec estático diseñado: src/main/resources/openapi.yaml

### Ejemplos

    # Listar jobs
    curl http://localhost:8080/api/jobs

    # T-32a
    curl -X POST http://localhost:8080/api/jobs/t32aJob/run \
      -H "Content-Type: application/json" \
      -d '{"fecha":"2026-04-30","endPeriod":"2026-03-31"}'

    # T-32c
    curl -X POST http://localhost:8080/api/jobs/t32cJob/run \
      -H "Content-Type: application/json" \
      -d '{"fecha":"2026-04-30","startDate":"2026-01-01 00:00:00.000","endDate":"2026-03-31 00:00:00.000"}'

    # T-33a con ruta de salida personalizada
    curl -X POST http://localhost:8080/api/jobs/t33aJob/run \
      -H "Content-Type: application/json" \
      -d '{"reportingDate":"2026-04-15","endDate":"2026-03-31","outputPath":"/data/out/item33a.csv"}'

    # Estado de ejecución
    curl http://localhost:8080/api/jobs/executions/42

## Parámetros por job

| Job | Parámetros (body JSON) |
|-----|------------------------|
| t32aJob | fecha, endPeriod |
| t32bJob | fecha, endPeriod |
| t32cJob | fecha, startDate, endDate |
| t33aJob | reportingDate, endDate |

outputPath es opcional en todos.

## Qué está parametrizado

Por API (runtime, cambian en cada ejecución):
- Todas las fechas (fecha, endPeriod, startDate, endDate, reportingDate)
- Ruta de salida del CSV (outputPath)

Por configuración (application.yml, cambian entre entornos int/prod):
- Nombres de schema/tabla de cada query (emir.tables.*)
- Lista de países EEA (emir.eea-countries)
- Valores estáticos TR_CODE y REGULATION_REFERENCE (emir.tr-code, emir.regulation-reference)
- Conexión Impala (app.impala.*)
- Rutas de salida por defecto (app.output.*)
- Cron y fechas por defecto del scheduler (batch.schedule.*, batch.defaults.*)

No parametrizado (lógica de negocio del reporte):
- Los códigos de estado en los CASE WHEN rcncltnsts IN (...), categorías de
  calidad de dato, etc. Son la semántica del reporte regulatorio; cambiarlos
  alteraría el significado de la salida, así que se mantienen fijos en el SQL.

## Scheduler

JobScheduler lanza los 4 jobs con las fechas por defecto de batch.defaults.*
según el cron batch.schedule.cron (por defecto 02:00 cada día). Reutiliza el
mismo JobLaunchService que la API.

## Docker

> Estado actual del código: solo el job T-32a está implementado (reader =
> consulta Impala/Kudu, processor = mapeo DTO → entidad, writer = CSV), y se
> lanza automáticamente al arrancar (`JobRunnerConfig`), no vía API — el
> resto de esta sección de README (API REST, scheduler) es el diseño
> objetivo, aún no implementado. La app escucha en el puerto **9001**, no
> 8080. Al completar el job, el proceso llama a `System.exit()`: el
> contenedor arranca, ejecuta el job y termina con código 0 — es el
> comportamiento esperado de un job batch, no un fallo.

### Prerrequisitos

- Docker y Docker Compose.

### Variables de entorno

Copia `.env.example` a `.env` y ajusta si hace falta:

| Variable | Descripción | Por defecto |
|----------|-------------|-------------|
| `APP_PORT` | Puerto expuesto en el host | `9001` |
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | Datasource único (JdbcTemplate, repositorio de Spring Batch, consultas T-32a); apúntalo a Impala/Kudu real en un entorno de verdad | H2 en memoria |
| `APP_JOBS_T32A_END_PERIOD` / `APP_JOBS_T32A_REPORTING_DATE` | Parámetros del job T-32a | `2026-03-31` / `2026-04-30` |

Ningún secreto real debe ir en `.env.example`; `.env` está en `.gitignore`.

### Comandos

    docker compose build
    docker compose up -d
    docker compose ps
    docker compose logs -f app
    docker compose down

Reset completo (borra también los volúmenes/datos locales):

    docker compose down -v

### Verificación

- El CSV generado aparece en `./output/item32a-output.csv` (montado desde el
  contenedor).
- Mientras el contenedor está arriba: `curl http://localhost:9001/actuator/health`.
- `docker compose ps` mostrará `Exited (0)` tras completar el job — así se
  confirma que terminó bien, no que algo falló.
