package com.jpcode.config

import com.jpcode.api.servlet.CompetenciaServlet
import com.jpcode.api.servlet.CandidatoServlet
import com.jpcode.api.servlet.VagaServlet
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
        registrarVagaServlet()
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

        contexto.addServletMapping(
                "/candidatos/*",
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

    private void registrarVagaServlet() {
        tomcat.addServlet(
                contexto,
                "vagaServlet",
                new VagaServlet(
                        controllerConfig.vagaController
                )
        )

        contexto.addServletMapping(
                "/vagas",
                "vagaServlet"
        )
    }
}