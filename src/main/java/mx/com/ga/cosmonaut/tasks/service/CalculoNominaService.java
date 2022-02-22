package mx.com.ga.cosmonaut.tasks.service;

import lombok.SneakyThrows;
import mx.com.ga.cosmonaut.common.entity.calculo.NcrProcesoNomina;

public interface CalculoNominaService {

    @SneakyThrows
    void iniciar(NcrProcesoNomina procesoNomina);

}
