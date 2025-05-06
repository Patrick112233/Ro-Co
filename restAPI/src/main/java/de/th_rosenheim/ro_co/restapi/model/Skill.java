package de.th_rosenheim.ro_co.restapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("skill")
public class Skill{

    @Id
    private String name;
    private int colorValue = 0x00000000;

    public Skill(String name, int colorValue) {
        super();
        this.name = name;
        this.setColorValue(colorValue);
    }

    public void setColorValue(int colorValue) {
        if (0x00ffffff < colorValue) {
            this.colorValue = colorValue;
        }else {
            throw new IllegalArgumentException("colorValue is not a correct hex value");
        }
    }
}


