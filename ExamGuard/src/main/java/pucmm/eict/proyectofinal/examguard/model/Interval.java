package pucmm.eict.proyectofinal.examguard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interval {
    private int start;
    private int end;

    public int getDuration() {
        int duration = end - start + 1;
        return Math.max(duration, 1); // Si duration es 0, retornará 1
    }
}

