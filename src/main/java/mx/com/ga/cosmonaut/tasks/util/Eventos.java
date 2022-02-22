package mx.com.ga.cosmonaut.tasks.util;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import mx.com.ga.cosmonaut.tasks.dto.RespuestaEvento;

public class Eventos {

    private static Subject<RespuestaEvento> sujeto = PublishSubject.create();

    public static void enviar(RespuestaEvento respuesta){
        sujeto.onNext(respuesta);
    }

    public static Flowable<RespuestaEvento> emisor(){
        return sujeto.hide().toFlowable(BackpressureStrategy.BUFFER);
    }

}
