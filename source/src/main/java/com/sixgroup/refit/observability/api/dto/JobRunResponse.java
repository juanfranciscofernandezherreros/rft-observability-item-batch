package com.sixgroup.refit.observability.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de una ejecución de job")
public class JobRunResponse {

    @Schema(description = "ID de la ejecución (JobExecution)", example = "42")
    private Long executionId;

    @Schema(description = "ID de la instancia (JobInstance)", example = "17")
    private Long instanceId;

    @Schema(description = "Nombre del job", example = "t32aJob")
    private String jobName;

    @Schema(description = "Estado batch", example = "COMPLETED")
    private String status;

    @Schema(description = "Código de salida", example = "COMPLETED")
    private String exitCode;

    @Schema(description = "Mensaje informativo o de error")
    private String message;

    public JobRunResponse() { }

    public Long getExecutionId() { return executionId; }
    public void setExecutionId(Long v) { this.executionId = v; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long v) { this.instanceId = v; }
    public String getJobName() { return jobName; }
    public void setJobName(String v) { this.jobName = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getExitCode() { return exitCode; }
    public void setExitCode(String v) { this.exitCode = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
}
