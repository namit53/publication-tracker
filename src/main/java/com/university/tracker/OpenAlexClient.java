package com.university.tracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAlexClient {

    private final RestClient restClient;
    private final String institutionId;
    private final int perPage;

    public OpenAlexClient(@Value("${openalex.base-url:https://api.openalex.org}") String baseUrl,
                          @Value("${openalex.institution-id:I5847235}") String institutionId,
                          @Value("${openalex.per-page:100}") int perPage) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.institutionId = institutionId;
        this.perPage = Math.min(Math.max(perPage, 1), 100);
    }

    public OpenAlexResponse fetchWorks(int page) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/works")
                        .queryParam("filter", "institutions.id:" + institutionId)
                        .queryParam("page", page)
                        .queryParam("per-page", perPage)
                        .queryParam("select",
                                "title,authorships,publication_year,doi,primary_location,cited_by_count")
                        .build())
                .retrieve()
                .body(OpenAlexResponse.class);
    }
}
