package org.delcom.ifs25024pbo2026springbootcashflow.config;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheControlFilterTest {

    private final CacheControlFilter filter = new CacheControlFilter();

    @Test
    void staticAssetsGetImmutableCache() {
        assertTrue(CacheControlFilter.isStaticAsset("/VAADIN/build/a.js"));
        assertTrue(CacheControlFilter.isStaticAsset("/aura/fonts/x.bin"));
        assertTrue(CacheControlFilter.isStaticAsset("/any/font.woff2"));
        assertFalse(CacheControlFilter.isStaticAsset("/"));
    }

    @Test
    void addsHeaderAndBlocksOverrides() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/VAADIN/build/a.js");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                HttpServletResponse http = (HttpServletResponse) res;
                http.setHeader("Cache-Control", "no-cache");
                http.addHeader("cache-control", "no-store");
                http.setHeader("X-Test", "1");
                http.addHeader("X-Test2", "2");
                super.doFilter(req, res);
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals(CacheControlFilter.CACHE_VALUE, response.getHeader("Cache-Control"));
        assertEquals("1", response.getHeader("X-Test"));
        assertEquals("2", response.getHeader("X-Test2"));
    }

    @Test
    void otherPathsAreUntouched() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(null, response.getHeader("Cache-Control"));
    }
}
