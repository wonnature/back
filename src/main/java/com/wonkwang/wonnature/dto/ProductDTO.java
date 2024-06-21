package com.wonkwang.wonnature.dto;

import com.wonkwang.wonnature.domain.Product;
import com.wonkwang.wonnature.domain.ProductType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProductDTO {

    private Long id;
    private Long hit;
    private String title;
    private String englishTitle;
    private ProductType productType;
    private String oneLineIntroduce;
    private String storeLink;
    private List<String> imageUrls;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.hit = product.getHit();
        this.title = product.getTitle();
        this.productType = product.getProductType();
        this.oneLineIntroduce = product.getOneLineIntroduce();
        this.storeLink = product.getStoreLink();
        this.imageUrls = product.getImageUrls();
    }

    // Convert attributes map to list of maps
    private List<Map<String, String>> convertAttributes(Map<String, String> attributesMap) {
        return attributesMap.entrySet().stream()
                .map(entry -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("key", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
