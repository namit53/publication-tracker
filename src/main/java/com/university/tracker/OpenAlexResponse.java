package com.university.tracker;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAlexResponse(OpenAlexMeta meta, List<OpenAlexWork> results) {
}

record OpenAlexMeta(int count, int page, @JsonProperty("per_page") int perPage) {
}

record OpenAlexWork(
        String title,
        List<OpenAlexAuthorship> authorships,
        @JsonProperty("publication_year") Integer publicationYear,
        String doi,
        @JsonProperty("primary_location") OpenAlexLocation primaryLocation,
        @JsonProperty("cited_by_count") Integer citedByCount,
        @JsonProperty("primary_topic") OpenAlexPrimaryTopic primaryTopic) {

    OpenAlexWork(String title, List<OpenAlexAuthorship> authorships, Integer publicationYear,
                 String doi, OpenAlexLocation primaryLocation, Integer citedByCount) {
        this(title, authorships, publicationYear, doi, primaryLocation, citedByCount, null);
    }

    int publicationYearOrZero() {
        return publicationYear == null ? 0 : publicationYear;
    }

    int citedByCountOrZero() {
        return citedByCount == null ? 0 : citedByCount;
    }

    String authorNames() {
        if (authorships == null) {
            return "";
        }
        return authorships.stream()
                .filter(authorship -> authorship != null && authorship.author() != null
                        && authorship.author().displayName() != null)
                .map(authorship -> authorship.author().displayName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    String journalName() {
        if (primaryLocation == null || primaryLocation.source() == null) {
            return "";
        }
        String displayName = primaryLocation.source().displayName();
        return displayName == null ? "" : displayName;
    }

    String primaryFieldName() {
        if (primaryTopic == null || primaryTopic.field() == null) {
            return null;
        }
        return primaryTopic.field().displayName();
    }
}

record OpenAlexAuthorship(OpenAlexAuthor author) {
}

record OpenAlexAuthor(@JsonProperty("display_name") String displayName) {
}

record OpenAlexLocation(OpenAlexSource source) {
}

record OpenAlexSource(@JsonProperty("display_name") String displayName) {
}

record OpenAlexPrimaryTopic(OpenAlexTopicField field) {
}

record OpenAlexTopicField(@JsonProperty("display_name") String displayName) {
}
