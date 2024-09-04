package pucmm.eict.proyectofinal.examguard.utils;


import lombok.Data;

import java.util.List;

@Data
public class DataTableResponse<T> {

    private final int draw;
    private final long recordsTotal;
    private final long recordsFiltered;
    private final List<T> data;

    public DataTableResponse(int draw, long recordsTotal, long recordsFiltered, List<T> data) {
        this.draw = draw;
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataTableResponse{" +
                "draw=" + draw +
                ", recordsTotal=" + recordsTotal +
                ", recordsFiltered=" + recordsFiltered +
                ", data=" + data +
                '}';
    }
}
