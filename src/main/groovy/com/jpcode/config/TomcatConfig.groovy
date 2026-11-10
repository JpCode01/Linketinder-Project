
package com.jpcode.config

import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat

class TomcatConfig {

    private final Tomcat tomcat

    TomcatConfig(ControllerConfig controllerConfig, int porta) {
        tomcat = new Tomcat()
        tomcat.setPort(porta)
        tomcat.getConnector()

        Context contexto = tomcat.addContext("", null)

        ApiServletConfig apiServletConfig = new ApiServletConfig(
                tomcat,
                contexto,
                controllerConfig
        )

        apiServletConfig.registrarServlets()
    }

    void iniciar() {
        tomcat.start()
    }

    void aguardarRequisicoes() {
        tomcat.getServer().await()
    }
}
