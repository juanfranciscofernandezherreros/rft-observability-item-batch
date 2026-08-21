package com.sixgroup.refit.observability.model;

public record Item32CRow(
                         String regulatorId,
                         String reportCode,
                         String queryId,
                         String channel,
                         String reportType,
                         String deliveryDateFrom,
                         String deliveryDateTo,
                         String insertTimestamp,
                         String reportSchedule,
                         String reportFrequencyDaily,
                         String reportFrequencyMonth,
                         String lastDayOfMonth,
                         String reportFormat,
                         String reportStatus,
                         String partition) {
}
