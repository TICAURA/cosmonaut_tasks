package mx.com.ga.cosmonaut.tasks.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.sse.Event;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import mx.com.ga.cosmonaut.tasks.dto.RespuestaEvento;
import mx.com.ga.cosmonaut.tasks.util.Eventos;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

@Controller("/asincrono")
public class AsincronoController {

    @Get(processes = MediaType.TEXT_EVENT_STREAM)
    public Publisher<Event<RespuestaEvento>> suscribirse(){
        Flowable<Event<RespuestaEvento>> respuesta = Eventos.emisor().map(Event::of);
        respuesta.subscribe((m)->{});
        return respuesta;
    }
	

    @Get(value = "/headlines", processes = MediaType.TEXT_EVENT_STREAM)
    Flowable<Event<Boolean>> streamHeadlines() {
        return Flowable.<Event<Boolean>>create((emitter) -> {
                    emitter.onNext(Event.of(true));
                    emitter.onComplete();
                }, BackpressureStrategy.BUFFER).repeat(100).delay(1, TimeUnit.SECONDS);
    }
}
