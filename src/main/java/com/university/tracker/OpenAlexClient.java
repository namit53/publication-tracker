package com.university.tracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OpenAlexClient {

    private final RestClient restClient;
    private final String institutionId;
    private final int perPage;
    private final int maxPages;
    private final String apiKey;

    public OpenAlexClient(@Value("${openalex.base-url:https://api.openalex.org}") String baseUrl,
                          @Value("${openalex.institution-id:I5847235}") String institutionId,
                          @Value("${openalex.per-page:100}") int perPage,
                          @Value("${openalex.max-pages:1}") int maxPages,
                          @Value("${openalex.api-key:}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.institutionId = institutionId;
        this.perPage = Math.min(Math.max(perPage, 1), 100);
        this.maxPages = Math.max(maxPages, 1);
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public OpenAlexResponse fetchWorks(int page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/works")
                                .queryParam("filter", "institutions.id:" + institutionId)
                                .queryParam("page", page)
                                .queryParam("per-page", perPage)
                                .queryParam("select",
                                        "title,authorships,publication_year,doi,primary_location,cited_by_count,primary_topic");
                        if (!apiKey.isBlank()) {
                            builder.queryParam("api_key", apiKey);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .body(OpenAlexResponse.class);
        } catch (RestClientResponseException exception) {
            String detail = exception.getResponseBodyAsString();
            if (detail == null || detail.isBlank()) {
                detail = exception.getStatusText();
            }
            throw new OpenAlexImportException(
                    "OpenAlex returned HTTP " + exception.getStatusCode().value() + ": " + detail, exception);
        } catch (RestClientException exception) {
            throw new OpenAlexImportException(
                    "OpenAlex could not be reached. Check network access and try again.", exception);
        }
    }

    public boolean shouldFetchPage(int page) {
        return page <= maxPages;
    }
}
