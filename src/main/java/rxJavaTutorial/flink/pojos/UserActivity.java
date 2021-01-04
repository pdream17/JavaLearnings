package rxJavaTutorial.flink.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {
    private long userId;
    private long startTimestamp;
    private long endTimestamp;
}
