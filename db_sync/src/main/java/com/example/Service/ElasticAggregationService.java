package com.example.Service;

import com.example.Model.Campaign;
import com.example.Model.MediaPlan;
import com.example.Store.InMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;

@Slf4j
@Service
public class ElasticAggregationService {

    private final RestHighLevelClient client;
    private final int maxAggregationListSize = 1000;

    public ElasticAggregationService(RestHighLevelClient client) {
        this.client = client;
    }

    public void aggregateCampaigns(String indexPattern) throws IOException {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            TermsAggregationBuilder termsAgg = AggregationBuilders.terms("groups")
                    .field("campaignId")
                    .size(maxAggregationListSize)
                    .subAggregation(AggregationBuilders.sum("sumBids").field("bids"))
                    .subAggregation(AggregationBuilders.sum("sumWins").field("wins"))
                    .subAggregation(AggregationBuilders.sum("sumPixels").field("pixels"))
                    .subAggregation(AggregationBuilders.sum("sumClicks").field("clicks"))
                    .subAggregation(AggregationBuilders.sum("sumCost").field("cost"));

            sourceBuilder.aggregation(termsAgg);
            SearchRequest searchRequest = new SearchRequest(indexPattern + "-*");
            searchRequest.source(sourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            Terms groups = response.getAggregations().get("groups");

            for (Terms.Bucket bucket : groups.getBuckets()) {

                int campaignId = ((Number) bucket.getKey()).intValue();
                Campaign campaign = InMemoryStore.campaignMap.get(campaignId);
                if (campaign != null) {
                    Sum sumBids = bucket.getAggregations().get("sumBids");
                    Sum sumWins = bucket.getAggregations().get("sumWins");
                    Sum sumPixels = bucket.getAggregations().get("sumPixels");
                    Sum sumClicks = bucket.getAggregations().get("sumClicks");
                    Sum sumCost = bucket.getAggregations().get("sumCost");

                    if (sumBids != null) campaign.setBids(BigInteger.valueOf((long) sumBids.getValue()));
                    if (sumWins != null) campaign.setWins(BigInteger.valueOf((long) sumWins.getValue()));
                    if (sumPixels != null) campaign.setPixels(BigInteger.valueOf((long) sumPixels.getValue()));
                    if (sumClicks != null) campaign.setClicks(BigInteger.valueOf((long) sumClicks.getValue()));
                    if (sumCost != null) campaign.setCost(sumCost.getValue());

                    InMemoryStore.campaignMap.put(campaignId, campaign);
                    log.debug("Aggregated campaign {}", campaignId);
                }
            }
        } catch (IOException e) {
            log.error("Failed to aggregate campaigns from Elasticsearch", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during campaign aggregation", e);
        }
    }

    public void aggregateMediaPlans(String indexPattern) {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            TermsAggregationBuilder termsAgg = AggregationBuilders.terms("groups")
                    .field("mediaPlanId")
                    .size(maxAggregationListSize)
                    .subAggregation(AggregationBuilders.sum("sumBids").field("bids"))
                    .subAggregation(AggregationBuilders.sum("sumWins").field("wins"))
                    .subAggregation(AggregationBuilders.sum("sumPixels").field("pixels"))
                    .subAggregation(AggregationBuilders.sum("sumClicks").field("clicks"))
                    .subAggregation(AggregationBuilders.sum("sumCost").field("cost"));

            sourceBuilder.aggregation(termsAgg);

            SearchRequest searchRequest = new SearchRequest(indexPattern + "-*");
            searchRequest.source(sourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            Terms groups = response.getAggregations().get("groups");

            for (Terms.Bucket bucket : groups.getBuckets()) {

                BigInteger mediaPlanId = new BigInteger(bucket.getKey().toString());
                MediaPlan mp = InMemoryStore.mediaPlanMap.get(mediaPlanId);

                if (mp != null) {
                    Sum sumBids = bucket.getAggregations().get("sumBids");
                    Sum sumWins = bucket.getAggregations().get("sumWins");
                    Sum sumPixels = bucket.getAggregations().get("sumPixels");
                    Sum sumClicks = bucket.getAggregations().get("sumClicks");
                    Sum sumCost = bucket.getAggregations().get("sumCost");


                    if (sumBids != null) mp.setBids(BigInteger.valueOf((long) sumBids.getValue()));
                    if (sumWins != null) mp.setWins(BigInteger.valueOf((long) sumWins.getValue()));
                    if (sumPixels != null) mp.setImpressions(BigInteger.valueOf((long) sumPixels.getValue()));
                    if (sumClicks != null) mp.setClicks(BigInteger.valueOf((long) sumClicks.getValue()));
                    if (sumCost != null) mp.setSpend(sumCost.getValue());

                    InMemoryStore.mediaPlanMap.put(mediaPlanId, mp);
                    log.debug("Aggregated media plan {}", mediaPlanId);
                }
            }
        } catch (IOException e) {
            log.error("Failed to aggregate media plans from Elasticsearch", e);
        } catch (Exception e) {
            log.error("Unexpected error during media plan aggregation", e);
        }
    }
}