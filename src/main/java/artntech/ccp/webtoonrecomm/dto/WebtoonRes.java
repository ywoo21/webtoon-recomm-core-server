package artntech.ccp.webtoonrecomm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebtoonRes {

    @JsonProperty("image_id")
    private String id;

    private String title;
    private String imageUrl;
    private float score;
}
