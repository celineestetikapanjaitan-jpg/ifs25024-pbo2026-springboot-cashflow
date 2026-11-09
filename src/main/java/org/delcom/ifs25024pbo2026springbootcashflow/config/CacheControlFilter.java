package org.delcom.ifs25024pbo2026springbootcashflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Memberi cache panjang untuk aset statis ber-hash (/VAADIN/build/*) dan font,
 * agar kunjungan ulang cepat (audit "efficient cache policy").
 */
@Component
public class CacheControlFilter extends OncePerRequestFilter {

    static final String CACHE_VALUE = "public, max-age=31536000, immutable";

    static boolean isStaticAsset(String path) {
        return path.startsWith("/VAADIN/build/")
                || path.startsWith("/aura/fonts/")
                || path.endsWith(".woff2");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!isStaticAsset(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        response.setHeader("Cache-Control", CACHE_VALUE);
        chain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                if (!"Cache-Control".equalsIgnoreCase(name)) {
                    super.setHeader(name, value);
                }
            }

            @Override
            public void addHeader(String name, String value) {
                if (!"Cache-Control".equalsIgnoreCase(name)) {
                    super.addHeader(name, value);
                }
            }
        });
    }
}
