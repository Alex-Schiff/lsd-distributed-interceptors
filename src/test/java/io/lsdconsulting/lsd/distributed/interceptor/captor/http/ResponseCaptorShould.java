package io.lsdconsulting.lsd.distributed.interceptor.captor.http;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.HttpStatusDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NO_CONTENT;

class ResponseCaptorShould {

    private final RepositoryService repositoryService = mock(RepositoryService.class);
    private final SourceTargetDeriver sourceTargetDeriver = mock(SourceTargetDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);
    private final HttpHeaderRetriever httpHeaderRetriever = mock(HttpHeaderRetriever.class);
    private final HttpStatusDeriver httpStatusDeriver = mock(HttpStatusDeriver.class);

    private final PathDeriver pathDeriver = new PathDeriver();

    private final ResponseCaptor underTest = new ResponseCaptor(repositoryService,
            sourceTargetDeriver, pathDeriver, traceIdRetriever, httpHeaderRetriever, httpStatusDeriver, "profile");

    private final String resource = randomAlphanumeric(20);
    private final String url = "http://localhost/" + resource;
    private final String body = randomAlphanumeric(20);
    private final String traceId = randomAlphanumeric(20);
    private final String target = randomAlphanumeric(20);
    private final String serviceName = randomAlphanumeric(20);
    private final String path = randomAlphanumeric(20);

    private final Map<String, Collection<String>> requestHeaders = Map.of("b3", List.of(traceId), "Target-Name", List.of(target));

    private final Response response = Response.builder()
            .request(Request.create(GET, url, requestHeaders, body.getBytes(), defaultCharset(), new RequestTemplate()))
            .build();

    @Test
    public void takeTraceIdFromRequestHeaders() {
        given(traceIdRetriever.getTraceId(eq(requestHeaders))).willReturn(traceId);
        given(httpHeaderRetriever.retrieve(any(Request.class))).willReturn(requestHeaders);
        given(sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/" + resource))).willReturn(target);
        given(sourceTargetDeriver.deriveServiceName(requestHeaders)).willReturn(serviceName);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response, 10L);

        assertThat(interceptedInteraction.getTraceId(), is(traceId));
    }

    @Test
    public void deriveTargetFromRequestHeaders() {
        given(traceIdRetriever.getTraceId(eq(requestHeaders))).willReturn(traceId);
        given(sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/" + resource))).willReturn(target);
        given(sourceTargetDeriver.deriveServiceName(eq(requestHeaders))).willReturn(serviceName);
        given(httpHeaderRetriever.retrieve(any(Request.class))).willReturn(requestHeaders);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response, 10L);

        assertThat(interceptedInteraction.getTarget(), is(target));
    }

    @Test
    public void enqueueInterceptedInteractionOnFeignResponse() {
        given(traceIdRetriever.getTraceId(eq(requestHeaders))).willReturn(traceId);
        given(sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/" + resource))).willReturn(target);
        given(sourceTargetDeriver.deriveServiceName(eq(requestHeaders))).willReturn(serviceName);
        given(httpHeaderRetriever.retrieve(any(Request.class))).willReturn(requestHeaders);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response, 10L);

        verify(repositoryService).enqueue(interceptedInteraction);
    }

    @Test
    public void handleEmptyResponseBodyFromDeleteRequest() throws IOException {
        HttpHeaders httpHeaders = mock(HttpHeaders.class);
        given(httpRequest.getHeaders()).willReturn(httpHeaders);
        given(clientHttpResponse.getHeaders()).willReturn(httpHeaders);
        given(clientHttpResponse.getStatusCode()).willReturn(NO_CONTENT);
        given(httpHeaderRetriever.retrieve(httpRequest)).willReturn(requestHeaders);
        given(sourceTargetDeriver.deriveServiceName(requestHeaders)).willReturn(serviceName);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(httpRequest, clientHttpResponse, target, path, traceId, 10L);

        assertThat(interceptedInteraction.getBody(), is(emptyString()));
    }

    @Test
    public void enqueueInterceptedInteractionOnSpringResponse() throws IOException {
        HttpHeaders httpHeaders = mock(HttpHeaders.class);
        given(httpRequest.getHeaders()).willReturn(httpHeaders);
        given(clientHttpResponse.getHeaders()).willReturn(httpHeaders);
        given(clientHttpResponse.getStatusCode()).willReturn(NO_CONTENT);
        given(httpHeaderRetriever.retrieve(httpRequest)).willReturn(requestHeaders);
        given(sourceTargetDeriver.deriveServiceName(requestHeaders)).willReturn(serviceName);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(httpRequest, clientHttpResponse, target, path, traceId, 10L);

        verify(repositoryService).enqueue(interceptedInteraction);
    }
}