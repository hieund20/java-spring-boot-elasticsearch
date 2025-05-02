package com.elasticsearch.elasticsearch.search.util;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.elasticsearch.elasticsearch.search.SearchRequestDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;

import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class SearchUtil {

    private SearchUtil() {}

    public static Query buildSearchRequest(final SearchRequestDTO dto) {
        final Criteria criteria = getCriteria(dto);
        if (criteria == null) {
            return null;
        }

        CriteriaQuery query = new CriteriaQuery(criteria);

        if (dto.getSortBy() != null) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (SortOrder.Desc.equals(dto.getOrder())) {
                direction = Sort.Direction.DESC;
            }
            query.addSort(Sort.by(direction, dto.getSortBy()));
        }

        return query;
    }

    public static Criteria getCriteria(final SearchRequestDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getFields())) {
            return null;
        }

        final List<String> fields = dto.getFields();
        final String searchTerm = dto.getSearchTerm();

        Criteria criteria = null;

        if (fields.size() == 1) {
            // Tìm kiếm trên một trường duy nhất
            // Sử dụng is cho tìm kiếm chính xác, matches là like
            criteria = new Criteria(fields.get(0))
                    .expression("*" + searchTerm.toLowerCase() + "*");
        } else {
            // Tìm kiếm trên nhiều trường, sử dụng AND logic
            for (String field : fields) {
                if (criteria == null) {
                    criteria = new Criteria(field)
                            .expression("*" + searchTerm.toLowerCase() + "*");
                } else {
                    criteria = criteria.and(new Criteria(field)
                            .expression("*" + searchTerm.toLowerCase() + "*"));
                }
            }
        }

        return criteria;
    }
}
