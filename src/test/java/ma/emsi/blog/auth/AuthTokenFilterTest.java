package ma.emsi.blog.auth;

import ma.emsi.blog.service.impl.UserDetailsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class AuthTokenFilterTest {

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserDetailsServiceImpl userDetailsService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private AuthTokenFilter authTokenFilter;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		SecurityContextHolder.clearContext();
		when(jwtUtils.getJwtFromCookies(request)).thenReturn("validJwtToken");
	}

	@Test
	public void testDoFilterInternal_ValidToken() throws Exception {
		String jwt = "validJwtToken";
		String username = "testUser";

		when(jwtUtils.validateJwtToken(jwt)).thenReturn(true);
		when(jwtUtils.getUserNameFromJwtToken(jwt)).thenReturn(username);

		UserDetails userDetails = mock(UserDetails.class);
		when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

		authTokenFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verify(jwtUtils).validateJwtToken(jwt);
		verify(jwtUtils).getUserNameFromJwtToken(jwt);
		verify(userDetailsService).loadUserByUsername(username);

		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();

		assertNotNull(authentication);
		assertEquals(userDetails, authentication.getPrincipal());
	}

	@Test
    public void testDoFilterInternal_InvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidJwtToken");
        when(jwtUtils.validateJwtToken(anyString())).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}