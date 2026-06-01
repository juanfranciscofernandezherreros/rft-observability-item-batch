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
