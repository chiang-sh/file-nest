package io.github.chiang_sh.file_nest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
public class ApiLogFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OncePerRequestFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 0);
        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            long end = System.currentTimeMillis();
            LOGGER.info(
                    "[{}] {}{} - status={} body={} time={}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString() == null ? "" : "?" + request.getQueryString(),
                    response.getStatus(),
                    wrappedRequest.getContentAsString(),
                    end - start);
        }
    }
}
