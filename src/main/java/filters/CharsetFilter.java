package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * UTF-8 support for all pages
 */
@WebFilter("/*")
public class CharsetFilter implements Filter {

	private String encoding;
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		encoding = arg0.getInitParameter("requestEncoding");
        if (encoding == null) {
        	encoding = "UTF-8";
        }
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
        if (null == arg0.getCharacterEncoding()) {
            arg0.setCharacterEncoding(encoding);
        }

        arg1.setContentType("text/html; charset=UTF-8");
        arg1.setCharacterEncoding("UTF-8");

        arg2.doFilter(arg0, arg1);
	}

	@Override
	public void destroy() {
	}

}
