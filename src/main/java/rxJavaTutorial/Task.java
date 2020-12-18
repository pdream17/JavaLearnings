package rxJavaTutorial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    private String contesttype;
    private Long id;
    private Integer combinationid;
    private Double entryfee;
    private Integer contestsize;
    private String contestcategory;
    private Double prizeamount;
    private NewTask newTask;
    private Map<Integer, Integer> map;
}
