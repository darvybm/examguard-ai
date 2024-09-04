package pucmm.eict.proyectofinal.examguard.utils;

import org.bson.types.ObjectId;
import pucmm.eict.proyectofinal.examguard.model.Event;
import pucmm.eict.proyectofinal.examguard.model.enums.EventType;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.Timeline;
import pucmm.eict.proyectofinal.examguard.model.TimelineSegment;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TimelineGenerator {

    public static List<Timeline> generateTimeline(Recording recording) {
        List<Timeline> timelines = new ArrayList<>();

        if (recording == null || recording.getEvents() == null || recording.getEvents().isEmpty()) {
            return timelines;
        }

        // Filtrar los eventos basándose en los tipos de eventos que el usuario desea detectar
        List<EventType> eventTypesToDetect = recording.getEventsToDetect();
        List<Event> filteredEvents = recording.getEvents().stream()
                .filter(event -> eventTypesToDetect.contains(event.getEventType()))
                .toList();

        if (filteredEvents.isEmpty()) {
            return timelines;
        }

        // Obtener todos los tipos de eventos distintos presentes en los eventos filtrados
        List<EventType> eventTypes = filteredEvents.stream()
                .map(Event::getEventType)
                .distinct()
                .toList();

        // Iterar sobre cada tipo de evento para generar los segmentos de tiempo correspondientes
        for (EventType eventType : eventTypes) {
            List<Event> eventsOfType = filteredEvents.stream()
                    .filter(event -> event.getEventType() == eventType)
                    .sorted(Comparator.comparingInt(Event::getSecond))
                    .toList();

            List<TimelineSegment> segments = new ArrayList<>();
            int startSecond = -1;
            int endSecond = -1;
            List<String> images = new ArrayList<>();

            for (Event event : eventsOfType) {
                if (startSecond == -1) {
                    // Primer evento del tipo, inicializar el primer segmento
                    startSecond = event.getSecond();
                    endSecond = event.getSecond();
                    images.add(event.getImage());
                } else if (event.getSecond() == endSecond + 1) {
                    // Continuar el segmento actual
                    endSecond = event.getSecond();
                    images.add(event.getImage());
                } else {
                    // Crear un nuevo segmento
                    TimelineSegment timelineSegment = createTimelineSegment(startSecond, endSecond, images);
                    segments.add(timelineSegment);

                    // Iniciar nuevo segmento
                    startSecond = event.getSecond();
                    endSecond = event.getSecond();
                    images.clear();
                    images.add(event.getImage());
                }
            }

            // Agregar el último segmento a la lista después de salir del bucle
            if (startSecond != -1 && endSecond != -1) {
                TimelineSegment timelineSegment = createTimelineSegment(startSecond, endSecond, images);
                segments.add(timelineSegment);
            }

            // Crear y agregar el Timeline para este tipo de evento
            if (!segments.isEmpty()) {
                timelines.add(new Timeline(new ObjectId(), eventType, segments));
            }
        }

        return timelines;
    }

    private static TimelineSegment createTimelineSegment(int startSecond, int endSecond, List<String> images) {
        TimelineSegment timelineSegment = new TimelineSegment();
        timelineSegment.setId(new ObjectId());
        timelineSegment.setStartSecond(startSecond - 1);
        timelineSegment.setEndSecond(endSecond - 1);
        timelineSegment.setImages(new ArrayList<>(images));
        return timelineSegment;
    }
}
