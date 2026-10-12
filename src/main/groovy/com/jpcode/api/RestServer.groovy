package com.jpcode.api


import com.jpcode.config.ControllerConfig
import com.jpcode.config.DaoConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.config.TomcatConfig

class RestServer {
    static void main(String[] args) {
        int porta = 8080

        DaoConfig daoConfig = new DaoConfig()
        ServiceConfig serviceConfig = new ServiceConfig(daoConfig)
        ControllerConfig controllerConfig = new ControllerConfig(serviceConfig)

        TomcatConfig tomcatConfig = new TomcatConfig(
                controllerConfig,
                porta
        )

        tomcatConfig.iniciar()

        println("Servidor iniciado em http://localhost:${porta}")

        tomcatConfig.aguardarRequisicoes()
    }
}
