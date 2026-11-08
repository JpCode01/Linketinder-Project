package com.jpcode.config

import com.jpcode.api.servlet.*
import com.jpcode.api.filter.CorsFilter
import org.apache.tomcat.util.descriptor.web.FilterDef
import org.apache.tomcat.util.descriptor.web.FilterMap
import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat
import org.apache.tomcat.util.http.Rfc6265CookieProcessor

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
        contexto.useHttpOnly = true
        def cookieProcessor = new Rfc6265CookieProcessor()
        cookieProcessor.setSameSiteCookies('lax')
        contexto.cookieProcessor = cookieProcessor
        tomcat.addServlet(contexto, 'loginServlet', new LoginServlet(
                controllerConfig.candidatoController, controllerConfig.empresaController,
                controllerConfig.recrutadorController, controllerConfig.sessaoHttp))
        ['candidato', 'empresa', 'recrutador'].each { tipo ->
            contexto.addServletMapping("/login/${tipo}".toString(), 'loginServlet')
        }
        def filtro = new FilterDef(filterName: 'corsFilter', filter: new CorsFilter())
        contexto.addFilterDef(filtro)
        def mapeamento = new FilterMap(filterName: 'corsFilter')
        mapeamento.addURLPattern('/*')
        contexto.addFilterMap(mapeamento)
        registrar('recrutadorServlet', new RecrutadorServlet(controllerConfig.recrutadorController, controllerConfig.vagaController, controllerConfig.sessaoHttp), ['/recrutadores', '/recrutadores/*'])
        registrar('areaRecrutamentoServlet', new AreaRecrutamentoServlet(controllerConfig.recrutadorController), ['/areas-recrutamento'])
        registrar('referenciaServlet', new ReferenciaServlet(controllerConfig.referenciaController), ['/referencias/*'])
        registrar('curtidaServlet', new CurtidaServlet(controllerConfig.vagaController, controllerConfig.matchController, controllerConfig.sessaoHttp), ['/curtidas/*'])
        registrar('matchServlet', new MatchServlet(controllerConfig.matchController, controllerConfig.sessaoHttp), ['/matches/*'])
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
                        controllerConfig.candidatoController, controllerConfig.sessaoHttp
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
                        controllerConfig.empresaController, controllerConfig.sessaoHttp,
                        new RecrutadorServlet(controllerConfig.recrutadorController, controllerConfig.vagaController, controllerConfig.sessaoHttp)
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
                        controllerConfig.vagaController, controllerConfig.sessaoHttp
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
    private void registrar(String nome, jakarta.servlet.http.HttpServlet servlet, List<String> rotas) {
        tomcat.addServlet(contexto, nome, servlet)
        rotas.each { contexto.addServletMapping(it, nome) }
    }
}
