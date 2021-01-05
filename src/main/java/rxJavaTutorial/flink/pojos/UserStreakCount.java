package rxJavaTutorial.flink.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreakCount {
    private UserActivity userActivity;
    private Long streak;
}
