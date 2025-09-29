package com.example.rtbAttribution.Service;



import com.example.rtbAttribution.Model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.CriteriaQueryBuilder;


@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private final ElasticsearchOperations operations;

    /**
     * Generic search in ES index (mirrors Go's searchElastic).
     * Returns true if any document found (total hits > 0).
     */
    public boolean searchInIndex(String searchVal, String indexPattern, Class<?> clazz) {
        if (searchVal == null || searchVal.isEmpty()) {
            return false;
        }

        try {

            Criteria criteria = Criteria.where("uid").contains(searchVal).or(Criteria.where("bid_id").contains(searchVal));
            CriteriaQuery query = new CriteriaQueryBuilder(criteria)
                    .withSort(Sort.by(Sort.Direction.DESC, "timestamp"))
                    .withPageable(PageRequest.of(0, 1))
                    .build();



            SearchHits<?> hits = operations.search(query, clazz, IndexCoordinates.of(indexPattern));

            long totalHits = hits.getTotalHits();

            log.debug("ES search in '{}' for '{}': {} hits", indexPattern, searchVal, totalHits);

            if (totalHits > 0) {
                SearchHit<?> firstHit = hits.getSearchHits().get(0);
                log.debug("Found hit: source={}, timestamp={}", firstHit.getContent(), firstHit.getHighlightFields());
            }

            return totalHits > 0;
        } catch (Exception e) {
            log.error("ES search failed in '{}' for '{}': {}", indexPattern, searchVal, e.getMessage());
            return false;
        }
    }

    /**
     * Search clicks index for bid_id (ctc attribution).
     */
    public boolean searchClicks(String bidId) {
        return searchInIndex(bidId, "clicks-*", ClickFields.class);
    }

    /**
     * Search pixels index for uid (vtc attribution).
     */
    public boolean searchPixels(String uid) {
        return searchInIndex(uid, "pixels-*", PixelFields.class);
    }

    /**
     * Index a document (mirrors Go's client.Index).
     * Uses the document's @Id and index name.
     */
    public void indexDocument(Object document, String indexName) {
        if (document == null) {
            log.warn("Cannot index null document");
            return;
        }

        try {

            IndexCoordinates index = IndexCoordinates.of(indexName);
            operations.save(document, index);
            log.debug("Successfully indexed document to index: {}", indexName);
        } catch (Exception e) {
            log.error("Failed to index document to {}: {}", indexName, e.getMessage(), e);
            throw new RuntimeException("Elasticsearch indexing failed", e);
        }
    }

}