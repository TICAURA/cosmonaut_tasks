package mx.com.ga.cosmonaut.tasks.scheduler;

import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;
import mx.com.ga.cosmonaut.common.entity.calculo.NcrProcesoNomina;
import mx.com.ga.cosmonaut.common.repository.calculo.NcrNominaXperiodoRepository;
import mx.com.ga.cosmonaut.common.repository.calculo.NcrProcesoNominaRepository;
import mx.com.ga.cosmonaut.tasks.dto.RespuestaEvento;
import mx.com.ga.cosmonaut.tasks.service.CalculoNominaService;
import mx.com.ga.cosmonaut.tasks.util.Eventos;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Singleton
public class CalculoNominaScheduler {

    @Inject
    private NcrProcesoNominaRepository ncrProcesoNominaRepository;

    @Inject
    private NcrNominaXperiodoRepository ncrNominaXperiodoRepository;

    @Inject
    private CalculoNominaService calculoNominaService;

    @Value("${programador.nomina.hilos}")
    private Integer hilos;

    /**@Scheduled(fixedDelay = "${programador.nomina.cron.fijo}", initialDelay = "${programador.nomina.cron.inicial}")*/
    void consultaCalculoNomina() {
        long profiler = System.currentTimeMillis();
        log.info("Inicia tarea de calculo de nomina {}", System.currentTimeMillis() - profiler);

        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        List<NcrProcesoNomina> lista = ncrProcesoNominaRepository.findByEstadoProcesoNominaIdEstadoProcesoNominaId(1);
        lista.forEach(procesoNomina -> {
            executor.execute(() -> {
                calculoNominaService.iniciar(procesoNomina);
            });
        });

        log.info("Finaliza tarea de calculo de nomina {}", System.currentTimeMillis() - profiler);
    }
}
