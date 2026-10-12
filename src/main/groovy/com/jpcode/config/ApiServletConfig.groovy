package com.jpcode.config

import com.jpcode.api.servlet.CompetenciaServlet
import com.jpcode.api.servlet.CandidatoServlet
import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat
import com.jpcode.api.servlet.EmpresaServlet

class ApiServletConfig {

    private final Tomcat tomcat
    private final Context contexto
    private final ControllerConfig controllerConfig

    ApiServletConfig(Tomcat tomcat, Context contexto, ControllerConfig controllerConfig) {
        this.tomcat = tomcat
        this.contexto = contexto
        this.controllerConfig = controllerConfig
    }

    void registrarServlets() {
        registrarCompetenciaServlet()
        registrarCandidatoServlet()
        registrarEmpresaServlet()
    }

    private void registrarCompetenciaServlet() {
        tomcat.addServlet(
                contexto,
                "competenciaServlet",
                new CompetenciaServlet(controllerConfig.competenciaController)
        )

        contexto.addServletMapping(
                "/competencias",
                "competenciaServlet"
        )
    }

    private void registrarCandidatoServlet() {
        tomcat.addServlet(
                contexto,
                "candidatoServlet",
                new CandidatoServlet(
                        controllerConfig.candidatoController
                )
        )

        contexto.addServletMapping(
                "/candidatos",
                "candidatoServlet"
        )
    }

    private void registrarEmpresaServlet() {
        tomcat.addServlet(
                contexto,
                "empresaServlet",
                new EmpresaServlet(
                        controllerConfig.empresaController
                )
        )

        contexto.addServletMapping(
                "/empresas",
                "empresaServlet"
        )
    }
}