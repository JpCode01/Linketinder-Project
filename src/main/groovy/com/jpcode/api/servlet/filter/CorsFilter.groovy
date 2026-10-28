package com.jpcode.api.servlet.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class CorsFilter implements Filter {

    private static final String ORIGEM_PERMITIDA =
            "http://127.0.0.1:5500"

    @Override
    void init(FilterConfig filterConfig) {}

    @Override
    void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) {
        HttpServletRequest requisicao =
                request as HttpServletRequest

        HttpServletResponse resposta =
                response as HttpServletResponse

        String origem = requisicao.getHeader("Origin")

        if (origem == ORIGEM_PERMITIDA) {
            resposta.setHeader(
                    "Access-Control-Allow-Origin",
                    ORIGEM_PERMITIDA
            )
            resposta.setHeader(
                    "Vary",
                    "Origin"
            )
            resposta.setHeader(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS"
            )
            resposta.setHeader(
                    "Access-Control-Allow-Headers",
                    "Content-Type"
            )
        }

        if (requisicao.method == "OPTIONS") {
            resposta.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            )
            return
        }

        chain.doFilter(request, response)
    }

    @Override
    void destroy() {}
}