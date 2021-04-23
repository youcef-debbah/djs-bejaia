package dz.ngnex.control;

import dz.ngnex.util.WebKit;

import javax.servlet.*;
import java.io.IOException;
import java.nio.charset.Charset;

public class CharsetFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) {
    // no-op
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    String encoding = WebKit.CONTENT_ENCODING.name();
    request.setCharacterEncoding(encoding);
    response.setCharacterEncoding(encoding);
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // no-op
  }
}