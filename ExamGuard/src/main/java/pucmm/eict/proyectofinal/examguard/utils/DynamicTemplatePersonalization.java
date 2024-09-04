package pucmm.eict.proyectofinal.examguard.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DynamicTemplatePersonalization extends Personalization {

    @JsonProperty(value = "dynamic_template_data")
    private Map<String, Object> dynamic_template_data;

    @JsonProperty("dynamic_template_data")
    public Map<String, Object> getDynamicTemplateData() {
        return Objects.requireNonNullElse(dynamic_template_data, Collections.<String, Object>emptyMap());
    }

    public void addDynamicTemplateData(String key, Object value) {
        if (dynamic_template_data == null) {
            dynamic_template_data = new HashMap<String, Object>();
            dynamic_template_data.put(key, value);
        } else {
            dynamic_template_data.put(key, value);
        }
    }

}