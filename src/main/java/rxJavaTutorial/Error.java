package rxJavaTutorial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum Error {
    INVALID_ARGUMENTS("RS401", "Check you arguments");

    private String code;
    private String message;

    public Error setMessage(String message) {
        this.message = message;
        return this;
    }
}
