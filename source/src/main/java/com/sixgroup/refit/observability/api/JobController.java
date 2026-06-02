package com.sixgroup.refit.observability.api;

import com.sixgroup.refit.observability.api.dto.JobRunRequest;
import com.sixgroup.refit.observability.api.dto.JobRunResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Lanzamiento y consulta de jobs de reporting EMIR REFIT")
public class JobController {

    private final JobLaunchService jobLaunchService;

    public JobController(JobLaunchService jobLaunchService) {
        this.jobLaunchService = jobLaunchService;
    }

    @GetMapping
    @Operation(summary = "Listar jobs disponibles",
            description = "Devuelve los nombres de los jobs que pueden lanzarse.")
    public Set<String> listJobs() {
        return jobLaunchService.availableJobs();
    }

    @PostMapping("/{jobName}/run")
    @Operation(summary = "Lanzar un job",
            description = """
                    Lanza el job indicado de forma síncrona y devuelve su estado final.

                    Parámetros por job:
                    - **t32aJob**: `fecha`, `endPeriod`
                    - **t32bJob**: `fecha`, `endPeriod`
                    - **t32cJob**: `fecha`, `startDate`, `endDate`
                    - **t33aJob**: `reportingDate`, `endDate`

                    `outputPath` es opcional en todos; si se omite se usa la ruta de config.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job ejecutado (ver status en la respuesta)",
                    content = @Content(schema = @Schema(implementation = JobRunResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job desconocido")
    })
    public ResponseEntity<JobRunResponse> runJob(
            @Parameter(description = "Nombre del job", example = "t32aJob")
            @PathVariable String jobName,
            @RequestBody(required = false) JobRunRequest request) {

        JobRunRequest req = (request != null) ? request : new JobRunRequest();
        JobRunResponse resp = jobLaunchService.launch(jobName, req);

        if ("NOT_FOUND".equals(resp.getStatus())) {
            return ResponseEntity.status(404).body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Consultar estado de una ejecución",
            description = "Devuelve el estado actual de una ejecución por su executionId.")
    public ResponseEntity<JobRunResponse> getStatus(
            @Parameter(description = "ID de la ejecución", example = "42")
            @PathVariable Long executionId) {
        JobRunResponse resp = jobLaunchService.status(executionId);
        if ("NOT_FOUND".equals(resp.getStatus())) {
            return ResponseEntity.status(404).body(resp);
        }
        return ResponseEntity.ok(resp);
    }
}
