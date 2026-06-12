package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.news.NewsArticleRepository;
import com.kasicry.openclawnews.operations.OperationalMetrics;
import com.kasicry.openclawnews.operations.AlertNotificationService;
import com.kasicry.openclawnews.operations.OperationalAlertEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CollectionServiceMetricsTest {

    @Test
    void recordsSuccessAfterRepositoryFlush() {
        WorkerClient workerClient = mock(WorkerClient.class);
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        AlertNotificationService alerts = mock(AlertNotificationService.class);
        WorkerCollectResponse response = new WorkerCollectResponse();
        when(workerClient.collect(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        new CollectionService(workerClient, repository, metrics, alerts)
                .collectAndSave(new WorkerCollectRequest());

        verify(repository).saveAll(anyList());
        verify(repository).flush();
        verify(metrics).recordCollectionSuccess(eq(response), eq(0), anyLong());
    }

    @Test
    void recordsFailureWhenRepositoryFlushFails() {
        WorkerClient workerClient = mock(WorkerClient.class);
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        AlertNotificationService alerts = mock(AlertNotificationService.class);
        WorkerCollectResponse response = new WorkerCollectResponse();
        when(workerClient.collect(org.mockito.ArgumentMatchers.any())).thenReturn(response);
        doThrow(new IllegalStateException("flush failed")).when(repository).flush();
        CollectionService service = new CollectionService(workerClient, repository, metrics, alerts);

        assertThatThrownBy(() -> service.collectAndSave(new WorkerCollectRequest()))
                .isInstanceOf(IllegalStateException.class);

        verify(metrics).recordCollectionFailure(anyLong());
        verify(alerts).notify(OperationalAlertEvent.COLLECTION_FAILURE);
        verify(metrics, never()).recordCollectionSuccess(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                anyLong()
        );
    }

    @Test
    void alertsWhenAnySourceIsUnsuccessful() {
        WorkerClient workerClient = mock(WorkerClient.class);
        NewsArticleRepository repository = mock(NewsArticleRepository.class);
        OperationalMetrics metrics = mock(OperationalMetrics.class);
        AlertNotificationService alerts = mock(AlertNotificationService.class);
        WorkerCollectResponse.SourceResult source = new WorkerCollectResponse.SourceResult();
        source.setStatus("failed");
        WorkerCollectResponse response = new WorkerCollectResponse();
        response.setSources(java.util.Collections.singletonList(source));
        when(workerClient.collect(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        new CollectionService(workerClient, repository, metrics, alerts)
                .collectAndSave(new WorkerCollectRequest());

        verify(alerts).notify(OperationalAlertEvent.COLLECTION_PARTIAL);
    }
}
