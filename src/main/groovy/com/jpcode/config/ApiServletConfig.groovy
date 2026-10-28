package com.jpcode.config

import com.jpcode.api.servlet.CompetenciaServlet
import com.jpcode.api.servlet.CandidatoServlet
import com.jpcode.api.servlet.CurtidaServlet
import com.jpcode.api.servlet.LoginServlet
import com.jpcode.api.servlet.MatchServlet
import com.jpcode.api.servlet.ReferenciaServlet
import com.jpcode.api.servlet.VagaServlet
import com.jpcode.api.servlet.filter.CorsFilter
import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat
import com.jpcode.api.servlet.EmpresaServlet
import org.apache.tomcat.util.descriptor.web.FilterDef
import org.apache.tomcat.util.descriptor.web.FilterMap

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
        registrarCorsFilter()

        registrarCompetenciaServlet()
        registrarCandidatoServlet()
        registrarEmpresaServlet()
        registrarVagaServlet()
        registrarLoginServlet()
        registrarReferenciaServlet()
        registrarCurtidaServlet()
        registrarMatchServlet()
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

        contexto.addServletMapping(
                "/empresas/*",
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

        contexto.addServletMapping(
                "/vagas/*",
                "vagaServlet"
        )
    }

    private void registrarLoginServlet() {
        tomcat.addServlet(
                contexto,
                "loginServlet",
                new LoginServlet(
                        controllerConfig.candidatoController,
                        controllerConfig.empresaController
                )
        )

        contexto.addServletMapping(
                "/login/*",
                "loginServlet"
        )
    }
    
    private void registrarCorsFilter() {
        FilterDef definicao = new FilterDef()
        definicao.setFilterName("corsFilter")
        definicao.setFilterClass(CorsFilter.name)
        definicao.setFilter(new CorsFilter())

        contexto.addFilterDef(definicao)

        FilterMap mapeamento = new FilterMap()
        mapeamento.setFilterName("corsFilter")
        mapeamento.addURLPattern("/*")
        mapeamento.setDispatcher("REQUEST")

        contexto.addFilterMapBefore(mapeamento)
    }

    private void registrarReferenciaServlet() {
        tomcat.addServlet(
                contexto,
                "referenciaServlet",
                new ReferenciaServlet(
                        controllerConfig.referenciaController
                )
        )

        contexto.addServletMapping(
                "/referencias/*",
                "referenciaServlet"
        )
    }

    private void registrarCurtidaServlet() {
        tomcat.addServlet(
                contexto,
                "curtidaServlet",
                new CurtidaServlet(
                        controllerConfig.vagaController,
                        controllerConfig.empresaController,
                        controllerConfig.matchController
                )
        )

        contexto.addServletMapping(
                "/curtidas/*",
                "curtidaServlet"
        )
    }

    private void registrarMatchServlet() {
        tomcat.addServlet(
                contexto,
                "matchServlet",
                new MatchServlet(
                        controllerConfig.matchController
                )
        )

        contexto.addServletMapping(
                "/matches/*",
                "matchServlet"
        )
    }
}